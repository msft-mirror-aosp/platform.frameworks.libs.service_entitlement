/*
 * Copyright (C) 2023 The Android Open Source Project
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

package com.android.libraries.entitlement;

import static com.google.common.truth.Truth.assertThat;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.testing.AndroidTestingRunner;

import com.android.libraries.entitlement.eapaka.EapAkaApi;
import com.android.libraries.entitlement.odsa.AcquireConfigurationOperation.AcquireConfigurationRequest;
import com.android.libraries.entitlement.odsa.AcquireConfigurationOperation.AcquireConfigurationResponse;
import com.android.libraries.entitlement.odsa.AcquireTemporaryTokenOperation.AcquireTemporaryTokenRequest;
import com.android.libraries.entitlement.odsa.AcquireTemporaryTokenOperation.AcquireTemporaryTokenResponse;
import com.android.libraries.entitlement.odsa.CheckEligibilityOperation;
import com.android.libraries.entitlement.odsa.CheckEligibilityOperation.CheckEligibilityRequest;
import com.android.libraries.entitlement.odsa.CheckEligibilityOperation.CheckEligibilityResponse;
import com.android.libraries.entitlement.odsa.ManageServiceOperation.ManageServiceRequest;
import com.android.libraries.entitlement.odsa.ManageServiceOperation.ManageServiceResponse;
import com.android.libraries.entitlement.odsa.ManageSubscriptionOperation.ManageSubscriptionRequest;
import com.android.libraries.entitlement.odsa.ManageSubscriptionOperation.ManageSubscriptionResponse;
import com.android.libraries.entitlement.odsa.OdsaOperation;
import com.android.libraries.entitlement.utils.Ts43Constants;

import com.google.common.collect.ImmutableList;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;
import java.net.URL;

@RunWith(AndroidTestingRunner.class)
public class Ts43OperationTest {
    private static final String TEST_URL = "https://test.url";
    private static final String ENTITLEMENT_VERSION = "9.0";
    private static final String TOKEN = "ASH127AHHA88SF";
    private static final String NEW_TOKEN = "ES7WLERXJH";
    private static final String SUBSCRIPTION_SERVICE_URL = "http://www.MNO.org/CDSubs";
    private static final String SUBSCRIPTION_SERVICE_USER_DATA = "imsi=XX";
    private static final String IMEI = "861536030196001";
    private static final String COMPANION_TERMINAL_ID = "98112687006099944";
    private static final String COMPANION_TERMINAL_EID = "JHSDHljhsdfy763hh";
    private static final String ICCID = "123456789";
    private static final String PROFILE_SMDP_ADDRESS = "SMDP+ ADDR";

    private static final String TEMPORARY_TOKEN = "A8daAd8ads7fau34789947kjhsfad;kjfh";

    private static final String TEMPORARY_TOKEN_EXPIRY = "2019-01-29T13:15:31Z";

    private static final String NOT_ENABLED_URL = "http://www.MNO.org/AppNotAllowed";

    private static final String NOT_ENABLED_USER_DATA = "msisdn=XX";

    private static final String MANAGE_SUBSCRIPTION_RESPONSE_CONTINUE_TO_WEBSHEET =
            "<?xml version=\"1.0\"?>"
                    + "<wap-provisioningdoc version=\"1.1\">"
                    + "<characteristic type=\"VERS\">"
                    + "    <parm name=\"version\" value=\"1\"/>"
                    + "    <parm name=\"validity\" value=\"172800\"/>"
                    + "</characteristic>"
                    + "<characteristic type=\"TOKEN\">"
                    + "    <parm name=\"token\" value=\"" + NEW_TOKEN + "\"/>"
                    + "</characteristic>"
                    + "<characteristic type=\"APPLICATION\">"
                    + "    <parm name=\"AppID\" value=\"ap2006\"/>"
                    + "    <parm name=\"SubscriptionServiceURL\""
                    + "        value=\"" + SUBSCRIPTION_SERVICE_URL + "\"/>"
                    + "    <parm name=\"SubscriptionServiceUserData\""
                    + "        value=\"" + SUBSCRIPTION_SERVICE_USER_DATA + "\"/>"
                    + "    <parm name=\"SubscriptionResult\" value=\"1\"/>"
                    + "    <parm name=\"OperationResult\" value=\"1\"/>"
                    + "</characteristic>"
                    + "</wap-provisioningdoc>";

    private static final String MANAGE_SUBSCRIPTION_RESPONSE_DOWNLOAD_PROFILE =
            "<?xml version=\"1.0\"?>\n"
                    + "<wap-provisioningdoc version=\"1.1\">\n"
                    + "    <characteristic type=\"VERS\">\n"
                    + "        <parm name=\"version\" value=\"1\"/>\n"
                    + "        <parm name=\"validity\" value=\"172800\"/>\n"
                    + "    </characteristic>\n"
                    + "    <characteristic type=\"TOKEN\">\n"
                    + "        <parm name=\"token\" value=\"ASH127AHHA88SF\"/>\n"
                    + "    </characteristic>\n"
                    + "    <characteristic type=\"APPLICATION\">\n"
                    + "        <parm name=\"AppID\" value=\"ap2006\"/>\n"
                    + "        <characteristic type=\"DownloadInfo\">\n"
                    + "            <parm name=\"ProfileIccid\" value=\"" + ICCID + "\"/>\n"
                    + "            <parm name=\"ProfileSmdpAddress\" value=\""
                    + PROFILE_SMDP_ADDRESS + "\"/>\n"
                    + "        </characteristic>\n"
                    + "        <parm name=\"SubscriptionResult\" value=\"2\"/>\n"
                    + "        <parm name=\"OperationResult\" value=\"1\"/>\n"
                    + "    </characteristic>\n"
                    + "</wap-provisioningdoc>";

    private static final String ACQUIRE_TEMPORARY_TOKEN_RESPONSE =
            "<?xml version=\"1.0\"?>\n"
                    + "<wap-provisioningdoc version=\"1.1\">\n"
                    + "<characteristic type=\"VERS\">\n"
                    + "    <parm name=\"version\" value=\"1\"/>\n"
                    + "    <parm name=\"validity\" value=\"172800\"/>\n"
                    + "</characteristic>\n"
                    + "<characteristic type=\"TOKEN\">\n"
                    + "    <parm name=\"token\" value=\"ASH127AHHA88SF\"/>\n"
                    + "</characteristic>\n"
                    + "<characteristic type=\"APPLICATION\">\n"
                    + "    <parm name=\"AppID\" value=\"ap2009\"/>\n"
                    + "    <parm name=\"TemporaryToken\" value=\"" + TEMPORARY_TOKEN + "\"/>\n"
                    + "    <parm name=\"TemporaryTokenExpiry\" "
                    + "        value=\"" + TEMPORARY_TOKEN_EXPIRY + "\"/>\n"
                    + "    <parm name=\"OperationTargets\"\n"
                    + "        value=\"ManageSubscription,AcquireConfiguration\"/>\n"
                    + "    <parm name=\"OperationResult\" value=\"1\"/>\n"
                    + "</characteristic>\n"
                    + "</wap-provisioningdoc>";

    private static final String ACQUIRE_CONFIGURATION_RESPONSE =
            "<?xml version=\"1.0\"?>\n"
                    + "<wap-provisioningdoc version=\"1.1\">\n"
                    + "<characteristic type=\"VERS\">\n"
                    + "    <parm name=\"version\" value=\"1\"/>\n"
                    + "    <parm name=\"validity\" value=\"172800\"/>\n"
                    + "</characteristic>\n"
                    + "<characteristic type=\"TOKEN\">\n"
                    + "    <parm name=\"token\" value=\"ASH127AHHA88SF\"/>\n"
                    + "</characteristic>\n"
                    + "<characteristic type=\"APPLICATION\">\n"
                    + "    <parm name=\"AppID\" value=\"ap2006\"/>\n"
                    + "        <characteristic type=\"PrimaryConfiguration\">\n"
                    + "            <parm name=\"ICCID\" value=\"" + ICCID + "\"/>\n"
                    + "            <characteristic type=\"DownloadInfo\">\n"
                    + "                <parm name=\"ProfileIccid\" value=\"" + ICCID + "\"/>\n"
                    + "                <parm name=\"ProfileSmdpAddress\" value=\""
                    + PROFILE_SMDP_ADDRESS + "\"/>\n"
                    + "            </characteristic>\n"
                    + "            <parm name=\"ServiceStatus\" value=\"1\"/>\n"
                    + "        </characteristic>\n"
                    + "    <parm name=\"OperationResult\" value=\"1\"/>\n"
                    + "</characteristic>\n"
                    + "</wap-provisioningdoc>\n";

    private static final String CHECK_ELIGIBILITY_RESPONSE =
            "<?xml version=\"1.0\"?>\n"
                    + "<wap-provisioningdoc version=\"1.1\">\n"
                    + "<characteristic type=\"VERS\">\n"
                    + "    <parm name=\"version\" value=\"1\"/>\n"
                    + "    <parm name=\"validity\" value=\"172800\"/>\n"
                    + "</characteristic>\n"
                    + "<characteristic type=\"TOKEN\">\n"
                    + "    <parm name=\"token\" value=\"ASH127AHHA88SF\"/>\n"
                    + "</characteristic>\n"
                    + "<characteristic type=\"APPLICATION\">\n"
                    + "    <parm name=\"AppID\" value=\"ap2006\"/>\n"
                    + "    <parm name=\"CompanionAppEligibility\" value=\"1\"/>\n"
                    + "    <parm name=\"CompanionDeviceServices\" value=\"SharedNumber\"/>\n"
                    + "    <parm name=\"NotEnabledURL\" value=\"" + NOT_ENABLED_URL + "\"/>\n"
                    + "    <parm name=\"NotEnabledUserData\" value=\"" + NOT_ENABLED_USER_DATA
                    + "\"/>\n"
                    + "    <parm name=\"OperationResult\" value=\"1\"/>\n"
                    + "</characteristic>\n"
                    + "</wap-provisioningdoc>";

    public String MANAGE_SERVICE_RESPONSE =
            "<?xml version=\"1.0\"?>\n"
                    + "<wap-provisioningdoc version=\"1.1\">\n"
                    + "<characteristic type=\"VERS\">\n"
                    + "    <parm name=\"version\" value=\"1\"/>\n"
                    + "    <parm name=\"validity\" value=\"172800\"/>\n"
                    + "</characteristic>\n"
                    + "<characteristic type=\"TOKEN\">\n"
                    + "    <parm name=\"token\" value=\"ASH127AHHA88SF\"/>\n"
                    + "</characteristic>\n"
                    + "<characteristic type=\"APPLICATION\">\n"
                    + "    <parm name=\"AppID\" value=\"ap2006\"/>\n"
                    + "    <parm name=\"ServiceStatus\" value=\"3\"/>\n"
                    + "    <parm name=\"OperationResult\" value=\"1\"/>\n"
                    + "</characteristic>\n"
                    + "</wap-provisioningdoc>";

    private CarrierConfig mCarrierConfig;

    private ServiceEntitlement mServiceEntitlement;

    @Mock
    private EapAkaApi mMockEapAkaApi;

    @Mock
    private Context mContext;

    @Mock
    private TelephonyManager mTelephonyManager;

    private Ts43Operation mTs43Operation;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        mCarrierConfig = CarrierConfig.builder().setServerUrl(TEST_URL).build();
        mServiceEntitlement = new ServiceEntitlement(mCarrierConfig, mMockEapAkaApi);

        doReturn(2).when(mTelephonyManager).getActiveModemCount();
        doReturn(IMEI).when(mTelephonyManager).getImei(0);
        doReturn(mTelephonyManager).when(mTelephonyManager).createForSubscriptionId(anyInt());
        doReturn(Context.TELEPHONY_SERVICE).when(mContext)
                .getSystemServiceName(TelephonyManager.class);
        doReturn(mTelephonyManager).when(mContext).getSystemService(Context.TELEPHONY_SERVICE);

        mTs43Operation = new Ts43Operation(mContext, 0, new URL(TEST_URL),
                ENTITLEMENT_VERSION, TOKEN, Ts43Operation.TOKEN_TYPE_NORMAL);

        Field field = Ts43Operation.class.getDeclaredField("mServiceEntitlement");
        field.setAccessible(true);
        field.set(mTs43Operation, mServiceEntitlement);
    }

    @Test
    public void testManageSubscription_continueToWebsheet() throws Exception {
        doReturn(MANAGE_SUBSCRIPTION_RESPONSE_CONTINUE_TO_WEBSHEET)
                .when(mMockEapAkaApi).performEsimOdsaOperation(
                        anyString(), any(CarrierConfig.class),
                        any(ServiceEntitlementRequest.class), any(OdsaOperation.class));

        ManageSubscriptionRequest request = ManageSubscriptionRequest.builder()
                .setAppId(Ts43Constants.APP_ODSA_PRIMARY)
                .setOperationType(OdsaOperation.OPERATION_TYPE_SUBSCRIBE)
                .setCompanionTerminalId(COMPANION_TERMINAL_ID)
                .setCompanionTerminalEid(COMPANION_TERMINAL_EID)
                .build();

        ManageSubscriptionResponse response = mTs43Operation.manageSubscription(request);
        assertThat(response.operationResult()).isEqualTo(OdsaOperation.OPERATION_RESULT_SUCCESS);
        assertThat(response.subscriptionResult()).isEqualTo(
                ManageSubscriptionResponse.SUBSCRIPTION_RESULT_CONTINUE_TO_WEBSHEET);
        assertThat(response.subscriptionServiceURL()).isEqualTo(new URL(SUBSCRIPTION_SERVICE_URL));
        assertThat(response.subscriptionServiceUserData())
                .isEqualTo(SUBSCRIPTION_SERVICE_USER_DATA);
    }

    @Test
    public void testManageSubscription_downloadProfile() throws Exception {
        doReturn(MANAGE_SUBSCRIPTION_RESPONSE_DOWNLOAD_PROFILE)
                .when(mMockEapAkaApi).performEsimOdsaOperation(
                        anyString(), any(CarrierConfig.class),
                        any(ServiceEntitlementRequest.class), any(OdsaOperation.class));

        ManageSubscriptionRequest request = ManageSubscriptionRequest.builder()
                .setAppId(Ts43Constants.APP_ODSA_PRIMARY)
                .setOperationType(OdsaOperation.OPERATION_TYPE_SUBSCRIBE)
                .setCompanionTerminalId(COMPANION_TERMINAL_ID)
                .setCompanionTerminalEid(COMPANION_TERMINAL_EID)
                .build();

        ManageSubscriptionResponse response = mTs43Operation.manageSubscription(request);
        assertThat(response.operationResult()).isEqualTo(OdsaOperation.OPERATION_RESULT_SUCCESS);
        assertThat(response.subscriptionResult()).isEqualTo(
                ManageSubscriptionResponse.SUBSCRIPTION_RESULT_DOWNLOAD_PROFILE);
        assertThat(response.downloadInfo().profileIccid()).isEqualTo(ICCID);
        assertThat(response.downloadInfo().profileSmdpAddresses())
                .isEqualTo(ImmutableList.of(PROFILE_SMDP_ADDRESS));
    }

    @Test
    public void testAcquireTemporaryToken() throws Exception {
        doReturn(ACQUIRE_TEMPORARY_TOKEN_RESPONSE)
                .when(mMockEapAkaApi).performEsimOdsaOperation(
                anyString(), any(CarrierConfig.class),
                any(ServiceEntitlementRequest.class), any(OdsaOperation.class));

        AcquireTemporaryTokenRequest request = AcquireTemporaryTokenRequest.builder()
                .setAppId(Ts43Constants.APP_ODSA_PRIMARY)
                .setOperationTargets(ImmutableList.of(OdsaOperation.OPERATION_MANAGE_SUBSCRIPTION,
                        OdsaOperation.OPERATION_ACQUIRE_CONFIGURATION))
                .build();
        AcquireTemporaryTokenResponse response = mTs43Operation.acquireTemporaryToken(request);
        assertThat(response.operationResult()).isEqualTo(OdsaOperation.OPERATION_RESULT_SUCCESS);
        assertThat(response.temporaryToken()).isEqualTo(TEMPORARY_TOKEN);
        assertThat(response.temporaryTokenExpiry().toString()).isEqualTo(TEMPORARY_TOKEN_EXPIRY);
        assertThat(response.operationTargets()).isEqualTo(ImmutableList.of(
                OdsaOperation.OPERATION_MANAGE_SUBSCRIPTION,
                OdsaOperation.OPERATION_ACQUIRE_CONFIGURATION));
    }

    @Test
    public void testAcquireConfiguration() throws Exception {
        doReturn(ACQUIRE_CONFIGURATION_RESPONSE).when(mMockEapAkaApi).performEsimOdsaOperation(
                anyString(), any(CarrierConfig.class),
                any(ServiceEntitlementRequest.class), any(OdsaOperation.class));
        AcquireConfigurationRequest request = AcquireConfigurationRequest.builder()
                .setAppId(Ts43Constants.APP_ODSA_PRIMARY)
                .build();

        AcquireConfigurationResponse response = mTs43Operation.acquireConfiguration(request);
        assertThat(response.operationResult()).isEqualTo(OdsaOperation.OPERATION_RESULT_SUCCESS);
        assertThat(response.configurations()).hasSize(1);
        AcquireConfigurationResponse.Configuration config = response.configurations().get(0);
        assertThat(config.iccid()).isEqualTo(ICCID);
        assertThat(config.downloadInfo().profileIccid()).isEqualTo(ICCID);
        assertThat(config.downloadInfo().profileSmdpAddresses()).isEqualTo(
                ImmutableList.of(PROFILE_SMDP_ADDRESS));
        assertThat(config.serviceStatus()).isEqualTo(OdsaOperation.SERVICE_STATUS_ACTIVATED);
    }

    @Test
    public void testCheckEligibility() throws Exception {
        doReturn(CHECK_ELIGIBILITY_RESPONSE).when(mMockEapAkaApi).performEsimOdsaOperation(
                anyString(), any(CarrierConfig.class),
                any(ServiceEntitlementRequest.class), any(OdsaOperation.class));
        CheckEligibilityRequest request = CheckEligibilityRequest.builder()
                .setAppId(Ts43Constants.APP_ODSA_PRIMARY)
                .build();

        CheckEligibilityResponse response = mTs43Operation.checkEligibility(request);
        assertThat(response.operationResult()).isEqualTo(OdsaOperation.OPERATION_RESULT_SUCCESS);
        assertThat(response.appEligibility()).isEqualTo(
                CheckEligibilityOperation.ELIGIBILITY_RESULT_ENABLED);
        assertThat(response.companionDeviceServices()).containsExactly(
                OdsaOperation.COMPANION_SERVICE_SHARED_NUMBER);
        assertThat(response.notEnabledURL()).isEqualTo(new URL(NOT_ENABLED_URL));
        assertThat(response.notEnabledUserData()).isEqualTo(NOT_ENABLED_USER_DATA);
    }

    @Test
    public void testManageService() throws Exception {
        doReturn(MANAGE_SERVICE_RESPONSE).when(mMockEapAkaApi).performEsimOdsaOperation(
                anyString(), any(CarrierConfig.class),
                any(ServiceEntitlementRequest.class), any(OdsaOperation.class));
        ManageServiceRequest request = ManageServiceRequest.builder()
                .setAppId(Ts43Constants.APP_ODSA_PRIMARY)
                .build();

        ManageServiceResponse response = mTs43Operation.manageService(request);
        assertThat(response.operationResult()).isEqualTo(OdsaOperation.OPERATION_RESULT_SUCCESS);
        assertThat(response.serviceStatus()).isEqualTo(OdsaOperation.SERVICE_STATUS_DEACTIVATED);
    }
}
