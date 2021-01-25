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
import static org.mockito.Mockito.when;

import com.android.libraries.entitlement.ServiceEntitlement;
import com.android.libraries.entitlement.ServiceEntitlementRequest;
import com.android.libraries.entitlement.http.HttpClient;
import com.android.libraries.entitlement.http.HttpResponse;
import com.android.libraries.entitlement.http.HttpConstants.ContentType;

import android.content.Context;
import android.os.Build;
import android.os.Build.VERSION;

import android.telephony.TelephonyManager;

import androidx.test.runner.AndroidJUnit4;

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
    private static final String EAP_AKA_CHALLENGE = "{\"eap-relay-packet\":\"EAP_AKA_CHALLENGE\"}";
    private static final String EAP_AKA_CHALLENGE_RESPONSE = "EAP_AKA_CHALLENGE_RESPONSE";
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
    private static final String IMSI = "TEST_IMSI";
    private static final String IMEI = "TEST_IMEI";
    private static final String MCCMNC = "10010";
    private static final String ENTITLEMENT_URL_WITH_TOKEN =
        TEST_URL
            + "?IMSI="
            + IMSI
            + "&token="
            + TOKEN
            + "&terminal_id="
            + IMEI
            + "&terminal_vendor="
            + Build.MANUFACTURER
            + "&terminal_model="
            + Build.MODEL
            + "&terminal_sw_version="
            + VERSION.BASE_OS
            + "&app="
            + ServiceEntitlement.APP_VOWIFI
            + "&vers=0"
            + "&entitlement_version=2.0";
    private static final int SUB_ID = 0;

    @Rule public final MockitoRule rule = MockitoJUnit.rule();
    @Mock Context mockContext;
    @Mock HttpClient mockHttpClient;
    @Mock TelephonyManager mockTelephonyManager;
    @Mock TelephonyManager mockTelephonyManagerForSubId;

    private EapAkaApi eapAkaApi;

    @Before
    public void setUp() {
        eapAkaApi = new EapAkaApi(mockContext, SUB_ID, mockHttpClient);
        when(mockContext.getSystemService(Context.TELEPHONY_SERVICE))
            .thenReturn(mockTelephonyManager);
        when(mockTelephonyManager.createForSubscriptionId(SUB_ID))
            .thenReturn(mockTelephonyManagerForSubId);
    }

    @Test
    public void queryEntitlementStatus_hasAuthenticationToken_fastAuthN() throws Exception {
        HttpResponse response =
            HttpResponse.builder().setContentType(ContentType.XML).setBody(RESPONSE_XML).build();
        when(mockHttpClient.request(any())).thenReturn(response);

        ServiceEntitlementRequest request =
            ServiceEntitlementRequest.builder().setAuthenticationToken(TOKEN).build();

        assertThat(eapAkaApi.queryEntitlementStatus(
                ServiceEntitlement.APP_VOWIFI, TEST_URL, request))
            .isEqualTo(RESPONSE_XML);
    }

    @Test
    public void queryEntitlementStatus_noAuthenticationToken_initialAuthN() throws Exception {
        HttpResponse eapChallengeResponse =
            HttpResponse
                .builder().setContentType(ContentType.JSON).setBody(EAP_AKA_CHALLENGE).build();
        HttpResponse xmlResponse =
            HttpResponse.builder().setContentType(ContentType.XML).setBody(RESPONSE_XML).build();
        when(mockHttpClient.request(any()))
            .thenReturn(eapChallengeResponse).thenReturn(xmlResponse);
        EapAkaResponse.challengeResponseForTesting = EAP_AKA_CHALLENGE_RESPONSE;

        ServiceEntitlementRequest request = ServiceEntitlementRequest.builder().build();

        assertThat(
            eapAkaApi.queryEntitlementStatus(ServiceEntitlement.APP_VOWIFI, TEST_URL, request))
            .isEqualTo(RESPONSE_XML);
    }
}