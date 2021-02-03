/*
 * Copyright (C) 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.libraries.entitlement.eapaka;

import static com.google.common.truth.Truth.assertThat;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.telephony.TelephonyManager;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.runner.AndroidJUnit4;

import com.android.libraries.entitlement.ServiceEntitlement;
import com.android.libraries.entitlement.ServiceEntitlementRequest;
import com.android.libraries.entitlement.http.HttpClient;
import com.android.libraries.entitlement.http.HttpConstants.ContentType;
import com.android.libraries.entitlement.http.HttpResponse;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

@RunWith(AndroidJUnit4.class)
public class EapAkaApiTest {
    private static final String TEST_URL = "https://test.url/test-path";
    private static final String EAP_AKA_CHALLENGE =
            "{\"eap-relay-packet\":\""
                    + "AQIAfBcBAAABBQAAXOZSkCjxysgE4"
                    + "3GWqHJvgQIFAABrikWGrekAALNU4TxmCDPoCwUAAJT0nqXeAYlqzT0UGXINENWBBQAA7z3fhImk"
                    + "q+vcCKWIZBdvuYIJAAAPRUFp7KWFo+Thr78Qj9hEkB2zA0i6KakODsufBC+BJQ==\"}";
    private static final String GSM_SECURITY_CONTEXT_REQUEST =
            "EFzmUpAo8crIBONxlqhyb4EQa4pFhq3pAACzVOE8Zggz6A==";
    private static final String GSM_SECURITY_CONTEXT_RESPONSE =
            "2wjHnwKln8mjjxDzMKJvLBzMHtm0X9SNBsUWEAbEiAdD7xeqqZ7nsXzukRkIhd6SDZ4bj7s=";
    private static final String RESPONSE_XML =
            "<wap-provisioningdoc version=\"1.1\">\n"
                    + "    <characteristic type=\"TOKEN\">\n"
                    + "        <parm name=\"token\" value=\"kZYfCEpSsMr88KZVmab5UsZVzl+nWSsX\"/>\n"
                    + "        <parm name=\"validity\" value=\"3600\"/>\n"
                    + "    </characteristic>\n"
                    + "    <characteristic type=\"APPLICATION\">\n"
                    + "        <parm name=\"EntitlementStatus\" value=\"0\"/>\n"
                    + "        <parm name=\"AddrStatus\" value=\"0\"/>\n"
                    + "        <parm name=\"TC_Status\" value=\"2\"/>\n"
                    + "        <parm name=\"ProvStatus\" value=\"2\"/>\n"
                    + "        <parm name=\"ServiceFlow_URL\""
                    + " value=\"http://vm-host:8180/self-prov-websheet/rcs\"/>\n"
                    + "        <parm name=\"ServiceFlow_UserData\""
                    + " value=\"token=test_token\"/>\n"
                    + "    </characteristic>\n"
                    + "</wap-provisioningdoc>\n";
    private static final String TOKEN = "kZYfCEpSsMr88KZVmab5UsZVzl+nWSsX";
    private static final String IMSI = "234107813240779";
    private static final String MCCMNC = "23410";
    private static final int SUB_ID = 1;

    @Rule
    public final MockitoRule rule = MockitoJUnit.rule();

    @Mock
    private HttpClient mMockHttpClient;
    @Mock
    private TelephonyManager mMockTelephonyManager;
    @Mock
    private TelephonyManager mMockTelephonyManagerForSubId;

    private Context mContext;
    private EapAkaApi mEapAkaApi;

    @Before
    public void setUp() {
        mContext = spy(ApplicationProvider.getApplicationContext());
        mEapAkaApi = new EapAkaApi(mContext, SUB_ID, mMockHttpClient);
        when(mContext.getSystemService(TelephonyManager.class))
                .thenReturn(mMockTelephonyManager);
        when(mMockTelephonyManager.createForSubscriptionId(SUB_ID))
                .thenReturn(mMockTelephonyManagerForSubId);
    }

    @Test
    public void queryEntitlementStatus_hasAuthenticationToken_fastAuthN() throws Exception {
        HttpResponse response =
                HttpResponse.builder().setContentType(ContentType.XML).setBody(RESPONSE_XML)
                        .build();
        when(mMockHttpClient.request(any())).thenReturn(response);

        ServiceEntitlementRequest request =
                ServiceEntitlementRequest.builder().setAuthenticationToken(TOKEN).build();

        assertThat(mEapAkaApi.queryEntitlementStatus(
                ServiceEntitlement.APP_VOWIFI, TEST_URL, request))
                .isEqualTo(RESPONSE_XML);
    }

    @Test
    public void queryEntitlementStatus_noAuthenticationToken_initialAuthN() throws Exception {
        when(mMockTelephonyManagerForSubId.getSubscriberId()).thenReturn(IMSI);
        when(mMockTelephonyManagerForSubId.getSimOperator()).thenReturn(MCCMNC);
        when(mMockTelephonyManagerForSubId.getIccAuthentication(
                TelephonyManager.APPTYPE_USIM,
                TelephonyManager.AUTHTYPE_EAP_AKA,
                GSM_SECURITY_CONTEXT_REQUEST))
                .thenReturn(GSM_SECURITY_CONTEXT_RESPONSE);

        HttpResponse eapChallengeResponse =
                HttpResponse
                        .builder().setContentType(ContentType.JSON).setBody(EAP_AKA_CHALLENGE)
                        .build();
        HttpResponse xmlResponse =
                HttpResponse.builder().setContentType(ContentType.XML).setBody(RESPONSE_XML)
                        .build();
        when(mMockHttpClient.request(any()))
                .thenReturn(eapChallengeResponse).thenReturn(xmlResponse);

        ServiceEntitlementRequest request = ServiceEntitlementRequest.builder().build();

        assertThat(
                mEapAkaApi.queryEntitlementStatus(ServiceEntitlement.APP_VOWIFI, TEST_URL, request))
                .isEqualTo(RESPONSE_XML);
    }
}