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

import static com.android.libraries.entitlement.ServiceEntitlementException.ERROR_EAP_AKA_FAILURE;
import static com.android.libraries.entitlement.ServiceEntitlementException.ERROR_EAP_AKA_SYNCHRONIZATION_FAILURE;
import static com.android.libraries.entitlement.ServiceEntitlementException.ERROR_MALFORMED_HTTP_RESPONSE;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import com.android.libraries.entitlement.CarrierConfig;
import com.android.libraries.entitlement.EsimOdsaOperation;
import com.android.libraries.entitlement.ServiceEntitlementException;
import com.android.libraries.entitlement.ServiceEntitlementRequest;
import com.android.libraries.entitlement.http.HttpClient;
import com.android.libraries.entitlement.http.HttpConstants.ContentType;
import com.android.libraries.entitlement.http.HttpConstants.RequestMethod;
import com.android.libraries.entitlement.http.HttpRequest;
import com.android.libraries.entitlement.http.HttpResponse;

import com.google.common.collect.ImmutableList;
import com.google.common.net.HttpHeaders;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class EapAkaApi {
    private static final String TAG = "ServiceEntitlement";

    public static final String EAP_CHALLENGE_RESPONSE = "eap-relay-packet";

    private static final String VERS = "vers";
    private static final String ENTITLEMENT_VERSION = "entitlement_version";
    private static final String TERMINAL_ID = "terminal_id";
    private static final String TERMINAL_VENDOR = "terminal_vendor";
    private static final String TERMINAL_MODEL = "terminal_model";
    private static final String TERMIAL_SW_VERSION = "terminal_sw_version";
    private static final String APP = "app";
    private static final String EAP_ID = "EAP_ID";
    private static final String IMSI = "IMSI";
    private static final String TOKEN = "token";
    private static final String TEMPORARY_TOKEN = "temporary_token";
    private static final String NOTIF_ACTION = "notif_action";
    private static final String NOTIF_TOKEN = "notif_token";
    private static final String APP_VERSION = "app_version";
    private static final String APP_NAME = "app_name";

    private static final String OPERATION = "operation";
    private static final String OPERATION_TYPE = "operation_type";
    private static final String OPERATION_TARGETS = "operation_targets";
    private static final String COMPANION_TERMINAL_ID = "companion_terminal_id";
    private static final String COMPANION_TERMINAL_VENDOR = "companion_terminal_vendor";
    private static final String COMPANION_TERMINAL_MODEL = "companion_terminal_model";
    private static final String COMPANION_TERMINAL_SW_VERSION = "companion_terminal_sw_version";
    private static final String COMPANION_TERMINAL_FRIENDLY_NAME =
            "companion_terminal_friendly_name";
    private static final String COMPANION_TERMINAL_SERVICE = "companion_terminal_service";
    private static final String COMPANION_TERMINAL_ICCID = "companion_terminal_iccid";
    private static final String COMPANION_TERMINAL_EID = "companion_terminal_eid";

    private static final String TERMINAL_ICCID = "terminal_iccid";
    private static final String TERMINAL_EID = "terminal_eid";

    private static final String TARGET_TERMINAL_ID = "target_terminal_id";
    // Non-standard params for Korean carriers
    private static final String TARGET_TERMINAL_IDS = "target_terminal_imeis";
    private static final String TARGET_TERMINAL_ICCID = "target_terminal_iccid";
    private static final String TARGET_TERMINAL_EID = "target_terminal_eid";
    // Non-standard params for Korean carriers
    private static final String TARGET_TERMINAL_SERIAL_NUMBER = "target_terminal_sn";
    // Non-standard params for Korean carriers
    private static final String TARGET_TERMINAL_MODEL = "target_terminal_model";

    private static final String OLD_TERMINAL_ID = "old_terminal_id";
    private static final String OLD_TERMINAL_ICCID = "old_terminal_iccid";

    private static final String BOOST_TYPE = "boost_type";

    // In case of EAP-AKA synchronization failure or another challenge, we try to authenticate for
    // at most three times.
    private static final int MAX_EAP_AKA_ATTEMPTS = 3;

    // Max TERMINAL_* string length according to GSMA RCC.14 section 2.4
    private static final int MAX_TERMINAL_VENDOR_LENGTH = 4;
    private static final int MAX_TERMINAL_MODEL_LENGTH = 10;
    private static final int MAX_TERMINAL_SOFTWARE_VERSION_LENGTH = 20;

    private final Context mContext;
    private final int mSimSubscriptionId;
    private final HttpClient mHttpClient;
    private final String mBypassEapAkaResponse;
    private final String mAppVersion;

    public EapAkaApi(
            Context context,
            int simSubscriptionId,
            boolean saveHistory,
            String bypassEapAkaResponse) {
        this(context, simSubscriptionId, new HttpClient(saveHistory), bypassEapAkaResponse);
    }

    @VisibleForTesting
    EapAkaApi(
            Context context,
            int simSubscriptionId,
            HttpClient httpClient,
            String bypassEapAkaResponse) {
        this.mContext = context;
        this.mSimSubscriptionId = simSubscriptionId;
        this.mHttpClient = httpClient;
        this.mBypassEapAkaResponse = bypassEapAkaResponse;
        this.mAppVersion = getAppVersion(context);
    }

    /**
     * Retrieves HTTP response with the entitlement configuration doc though EAP-AKA authentication.
     *
     * <p>Implementation based on GSMA TS.43-v5.0 2.6.1.
     *
     * @throws ServiceEntitlementException when getting an unexpected http response.
     */
    @NonNull
    public HttpResponse queryEntitlementStatus(
            ImmutableList<String> appIds,
            CarrierConfig carrierConfig,
            ServiceEntitlementRequest request)
            throws ServiceEntitlementException {
        Uri.Builder urlBuilder = Uri.parse(carrierConfig.serverUrl()).buildUpon();
        appendParametersForAuthentication(urlBuilder, request);
        appendParametersForServiceEntitlementRequest(urlBuilder, appIds, request);
        if (!TextUtils.isEmpty(request.authenticationToken())) {
            // Fast Re-Authentication flow with pre-existing auth token
            Log.d(TAG, "Fast Re-Authentication");
            return httpGet(
                    urlBuilder.toString(),
                    carrierConfig,
                    request.acceptContentType(),
                    request.terminalVendor(),
                    request.terminalModel(),
                    request.terminalSoftwareVersion());
        } else {
            // Full Authentication flow
            Log.d(TAG, "Full Authentication");
            HttpResponse challengeResponse =
                    httpGet(
                            urlBuilder.toString(),
                            carrierConfig,
                            ServiceEntitlementRequest.ACCEPT_CONTENT_TYPE_JSON,
                            request.terminalVendor(),
                            request.terminalModel(),
                            request.terminalSoftwareVersion());
            String eapAkaChallenge = getEapAkaChallenge(challengeResponse);
            if (eapAkaChallenge == null) {
                throw new ServiceEntitlementException(
                        ERROR_MALFORMED_HTTP_RESPONSE,
                        "Failed to parse EAP-AKA challenge: " + challengeResponse.body());
            }
            return respondToEapAkaChallenge(
                    carrierConfig,
                    eapAkaChallenge,
                    challengeResponse.cookies(),
                    MAX_EAP_AKA_ATTEMPTS,
                    request.acceptContentType(),
                    request.terminalVendor(),
                    request.terminalModel(),
                    request.terminalSoftwareVersion());
        }
    }

    /**
     * Sends a follow-up HTTP request to the HTTP {@code response} using the same cookie, and
     * returns the follow-up HTTP response.
     *
     * <p>The {@code eapAkaChallenge} should be the EAP-AKA challenge from server, and the follow-up
     * request could contain:
     *
     * <ul>
     *   <li>The EAP-AKA response message, and the follow-up response should contain the service
     *       entitlement configuration, or another EAP-AKA challenge in which case the method calls
     *       if {@code remainingAttempts} is greater than zero (If {@code remainingAttempts} reaches
     *       0, the method will throw ServiceEntitlementException) ; or
     *   <li>The EAP-AKA synchronization failure message, and the follow-up response should contain
     *       the new EAP-AKA challenge. Then this method calls itself to follow-up the new challenge
     *       and return a new response, as long as {@code remainingAttempts} is greater than zero.
     * </ul>
     *
     * @return Challenge response from server whose content type is JSON
     */
    @NonNull
    private HttpResponse respondToEapAkaChallenge(
            CarrierConfig carrierConfig,
            String eapAkaChallenge,
            ImmutableList<String> cookies,
            int remainingAttempts,
            String contentType,
            String terminalVendor,
            String terminalModel,
            String terminalSoftwareVersion)
            throws ServiceEntitlementException {
        if (!mBypassEapAkaResponse.isEmpty()) {
            return challengeResponse(
                    mBypassEapAkaResponse,
                    carrierConfig,
                    cookies,
                    contentType,
                    terminalVendor,
                    terminalModel,
                    terminalSoftwareVersion);
        }

        EapAkaChallenge challenge = EapAkaChallenge.parseEapAkaChallenge(eapAkaChallenge);
        EapAkaResponse eapAkaResponse =
                EapAkaResponse.respondToEapAkaChallenge(mContext, mSimSubscriptionId, challenge);
        // This could be a successful authentication, another challenge, or synchronization failure.
        if (eapAkaResponse.response() != null) {
            HttpResponse response =
                    challengeResponse(
                            eapAkaResponse.response(),
                            carrierConfig,
                            cookies,
                            contentType,
                            terminalVendor,
                            terminalModel,
                            terminalSoftwareVersion);
            String nextEapAkaChallenge = getEapAkaChallenge(response);
            // successful authentication
            if (nextEapAkaChallenge == null) {
                return response;
            }
            // another challenge
            Log.d(TAG, "Received another challenge");
            if (remainingAttempts > 0) {
                return respondToEapAkaChallenge(
                        carrierConfig,
                        nextEapAkaChallenge,
                        cookies,
                        remainingAttempts - 1,
                        contentType,
                        terminalVendor,
                        terminalModel,
                        terminalSoftwareVersion);
            } else {
                throw new ServiceEntitlementException(
                        ERROR_EAP_AKA_FAILURE, "Unable to EAP-AKA authenticate");
            }
        } else if (eapAkaResponse.synchronizationFailureResponse() != null) {
            Log.d(TAG, "synchronization failure");
            HttpResponse newChallenge =
                    challengeResponse(
                            eapAkaResponse.synchronizationFailureResponse(),
                            carrierConfig,
                            cookies,
                            ServiceEntitlementRequest.ACCEPT_CONTENT_TYPE_JSON,
                            terminalVendor,
                            terminalModel,
                            terminalSoftwareVersion);
            String nextEapAkaChallenge = getEapAkaChallenge(newChallenge);
            if (nextEapAkaChallenge == null) {
                throw new ServiceEntitlementException(
                        ERROR_MALFORMED_HTTP_RESPONSE,
                        "Failed to parse EAP-AKA challenge: " + newChallenge.body());
            }
            if (remainingAttempts > 0) {
                return respondToEapAkaChallenge(
                        carrierConfig,
                        nextEapAkaChallenge,
                        cookies,
                        remainingAttempts - 1,
                        contentType,
                        terminalVendor,
                        terminalModel,
                        terminalSoftwareVersion);
            } else {
                throw new ServiceEntitlementException(
                        ERROR_EAP_AKA_SYNCHRONIZATION_FAILURE,
                        "Unable to recover from EAP-AKA synchroinization failure");
            }
        } else { // not possible
            throw new AssertionError("EapAkaResponse invalid.");
        }
    }

    @NonNull
    private HttpResponse challengeResponse(
            String eapAkaChallengeResponse,
            CarrierConfig carrierConfig,
            ImmutableList<String> cookies,
            String contentType,
            String terminalVendor,
            String terminalModel,
            String terminalSoftwareVersion)
            throws ServiceEntitlementException {
        Log.d(TAG, "challengeResponse");
        JSONObject postData = new JSONObject();
        try {
            postData.put(EAP_CHALLENGE_RESPONSE, eapAkaChallengeResponse);
        } catch (JSONException jsonException) {
            throw new ServiceEntitlementException(
                    ERROR_MALFORMED_HTTP_RESPONSE, "Failed to put post data", jsonException);
        }
        HttpRequest.Builder builder =
                HttpRequest.builder()
                        .setUrl(carrierConfig.serverUrl())
                        .setRequestMethod(RequestMethod.POST)
                        .setPostData(postData)
                        .addRequestProperty(HttpHeaders.ACCEPT, contentType)
                        .addRequestProperty(
                                HttpHeaders.CONTENT_TYPE,
                                ServiceEntitlementRequest.ACCEPT_CONTENT_TYPE_JSON)
                        .addRequestProperty(HttpHeaders.COOKIE, cookies)
                        .setTimeoutInSec(carrierConfig.timeoutInSec())
                        .setNetwork(carrierConfig.network());
            String userAgent =
                    getUserAgent(
                            carrierConfig.clientTs43(),
                            terminalVendor,
                            terminalModel,
                            terminalSoftwareVersion);
        if (!TextUtils.isEmpty(userAgent)) {
            builder.addRequestProperty(HttpHeaders.USER_AGENT, userAgent);
        }
        return mHttpClient.request(builder.build());
    }

    /**
     * Retrieves HTTP response from performing ODSA operations.
     * For operation type, see {@link EsimOdsaOperation}.
     *
     * <p>Implementation based on GSMA TS.43-v5.0 6.1.
     */
    @NonNull
    public HttpResponse performEsimOdsaOperation(
            String appId,
            CarrierConfig carrierConfig,
            ServiceEntitlementRequest request,
            EsimOdsaOperation odsaOperation)
            throws ServiceEntitlementException {
        Uri.Builder urlBuilder = Uri.parse(carrierConfig.serverUrl()).buildUpon();
        appendParametersForAuthentication(urlBuilder, request);
        appendParametersForServiceEntitlementRequest(urlBuilder, ImmutableList.of(appId), request);
        appendParametersForEsimOdsaOperation(urlBuilder, odsaOperation);

        if (!TextUtils.isEmpty(request.authenticationToken())
                || !TextUtils.isEmpty(request.temporaryToken())) {
            // Fast Re-Authentication flow with pre-existing auth token
            Log.d(TAG, "Fast Re-Authentication");
            return httpGet(
                    urlBuilder.toString(),
                    carrierConfig,
                    request.acceptContentType(),
                    request.terminalVendor(),
                    request.terminalModel(),
                    request.terminalSoftwareVersion());
        } else {
            // Full Authentication flow
            Log.d(TAG, "Full Authentication");
            HttpResponse challengeResponse =
                    httpGet(
                            urlBuilder.toString(),
                            carrierConfig,
                            ServiceEntitlementRequest.ACCEPT_CONTENT_TYPE_JSON,
                            request.terminalVendor(),
                            request.terminalModel(),
                            request.terminalSoftwareVersion());
            String eapAkaChallenge = getEapAkaChallenge(challengeResponse);
            if (eapAkaChallenge == null) {
                throw new ServiceEntitlementException(
                        ERROR_MALFORMED_HTTP_RESPONSE,
                        "Failed to parse EAP-AKA challenge: " + challengeResponse.body());
            }
            return respondToEapAkaChallenge(
                    carrierConfig,
                    eapAkaChallenge,
                    challengeResponse.cookies(),
                    MAX_EAP_AKA_ATTEMPTS,
                    request.acceptContentType(),
                    request.terminalVendor(),
                    request.terminalModel(),
                    request.terminalSoftwareVersion());
        }
    }

    /**
     * Retrieves the endpoint for OpenID Connect(OIDC) authentication.
     *
     * <p>Implementation based on section 2.8.2 of TS.43
     *
     * <p>The user should call {@link #queryEntitlementStatusFromOidc(String, CarrierConfig,
     * String)} with the authentication result to retrieve the service entitlement configuration.
     */
    @NonNull
    public String acquireOidcAuthenticationEndpoint(
            String appId, CarrierConfig carrierConfig, ServiceEntitlementRequest request)
            throws ServiceEntitlementException {
        Uri.Builder urlBuilder = Uri.parse(carrierConfig.serverUrl()).buildUpon();
        appendParametersForServiceEntitlementRequest(urlBuilder, ImmutableList.of(appId), request);
        HttpResponse response =
                httpGet(
                        urlBuilder.toString(),
                        carrierConfig,
                        request.acceptContentType(),
                        request.terminalVendor(),
                        request.terminalModel(),
                        request.terminalSoftwareVersion());
        return response.location();
    }

    /**
     * Retrieves the HTTP response with the service entitlement configuration from OIDC
     * authentication result.
     *
     * <p>Implementation based on section 2.8.2 of TS.43.
     *
     * <p>{@link #acquireOidcAuthenticationEndpoint} must be called before calling this method.
     */
    @NonNull
    public HttpResponse queryEntitlementStatusFromOidc(
            String url, CarrierConfig carrierConfig, ServiceEntitlementRequest request)
            throws ServiceEntitlementException {
        Uri.Builder urlBuilder = Uri.parse(url).buildUpon();
        return httpGet(
                urlBuilder.toString(),
                carrierConfig,
                request.acceptContentType(),
                request.terminalVendor(),
                request.terminalModel(),
                request.terminalSoftwareVersion());
    }

    private void appendParametersForAuthentication(
            Uri.Builder urlBuilder, ServiceEntitlementRequest request) {
        TelephonyManager telephonyManager =
                mContext.getSystemService(TelephonyManager.class)
                        .createForSubscriptionId(mSimSubscriptionId);
        if (!TextUtils.isEmpty(request.authenticationToken())) {
            // IMSI and token required for fast AuthN.
            urlBuilder
                    .appendQueryParameter(IMSI, telephonyManager.getSubscriberId())
                    .appendQueryParameter(TOKEN, request.authenticationToken());
        } else if (!TextUtils.isEmpty(request.temporaryToken())) {
            // temporary_token required for fast AuthN.
            urlBuilder.appendQueryParameter(TEMPORARY_TOKEN, request.temporaryToken());
        } else {
            // EAP_ID required for initial AuthN
            urlBuilder.appendQueryParameter(
                    EAP_ID,
                    getImsiEap(
                            telephonyManager.getSimOperator(), telephonyManager.getSubscriberId()));
        }
    }

    private void appendParametersForServiceEntitlementRequest(
            Uri.Builder urlBuilder,
            ImmutableList<String> appIds,
            ServiceEntitlementRequest request) {
        if (!TextUtils.isEmpty(request.notificationToken())) {
            urlBuilder
                    .appendQueryParameter(
                            NOTIF_ACTION, Integer.toString(request.notificationAction()))
                    .appendQueryParameter(NOTIF_TOKEN, request.notificationToken());
        }

        TelephonyManager telephonyManager =
                mContext.getSystemService(TelephonyManager.class)
                        .createForSubscriptionId(mSimSubscriptionId);
        // Assign terminal ID with device IMEI if not set.
        if (TextUtils.isEmpty(request.terminalId())) {
            urlBuilder.appendQueryParameter(TERMINAL_ID, telephonyManager.getImei());
        } else {
            urlBuilder.appendQueryParameter(TERMINAL_ID, request.terminalId());
        }

        // Optional query parameters, append them if not empty
        appendOptionalQueryParameter(urlBuilder, APP_VERSION, request.appVersion());
        appendOptionalQueryParameter(urlBuilder, APP_NAME, request.appName());
        appendOptionalQueryParameter(urlBuilder, BOOST_TYPE, request.boostType());

        for (String appId : appIds) {
            urlBuilder.appendQueryParameter(APP, appId);
        }

        urlBuilder
                // Identity and Authentication parameters
                .appendQueryParameter(
                        TERMINAL_VENDOR,
                        trimString(request.terminalVendor(), MAX_TERMINAL_VENDOR_LENGTH))
                .appendQueryParameter(
                        TERMINAL_MODEL,
                        trimString(request.terminalModel(), MAX_TERMINAL_MODEL_LENGTH))
                .appendQueryParameter(
                        TERMIAL_SW_VERSION,
                        trimString(
                                request.terminalSoftwareVersion(),
                                MAX_TERMINAL_SOFTWARE_VERSION_LENGTH))
                // General Service parameters
                .appendQueryParameter(VERS, Integer.toString(request.configurationVersion()))
                .appendQueryParameter(ENTITLEMENT_VERSION, request.entitlementVersion());
    }

    private void appendParametersForEsimOdsaOperation(
            Uri.Builder urlBuilder, EsimOdsaOperation odsaOperation) {
        urlBuilder.appendQueryParameter(OPERATION, odsaOperation.operation());
        if (odsaOperation.operationType() != EsimOdsaOperation.OPERATION_TYPE_NOT_SET) {
            urlBuilder.appendQueryParameter(OPERATION_TYPE,
                    Integer.toString(odsaOperation.operationType()));
        }
        appendOptionalQueryParameter(
                urlBuilder,
                OPERATION_TARGETS,
                TextUtils.join(",", odsaOperation.operationTargets()));
        appendOptionalQueryParameter(
                urlBuilder, COMPANION_TERMINAL_ID, odsaOperation.companionTerminalId());
        appendOptionalQueryParameter(
                urlBuilder, COMPANION_TERMINAL_VENDOR, odsaOperation.companionTerminalVendor());
        appendOptionalQueryParameter(
                urlBuilder, COMPANION_TERMINAL_MODEL, odsaOperation.companionTerminalModel());
        appendOptionalQueryParameter(
                urlBuilder,
                COMPANION_TERMINAL_SW_VERSION,
                odsaOperation.companionTerminalSoftwareVersion());
        appendOptionalQueryParameter(
                urlBuilder,
                COMPANION_TERMINAL_FRIENDLY_NAME,
                odsaOperation.companionTerminalFriendlyName());
        appendOptionalQueryParameter(
                urlBuilder, COMPANION_TERMINAL_SERVICE, odsaOperation.companionTerminalService());
        appendOptionalQueryParameter(
                urlBuilder, COMPANION_TERMINAL_ICCID, odsaOperation.companionTerminalIccid());
        appendOptionalQueryParameter(
                urlBuilder, COMPANION_TERMINAL_EID, odsaOperation.companionTerminalEid());
        appendOptionalQueryParameter(urlBuilder, TERMINAL_ICCID, odsaOperation.terminalIccid());
        appendOptionalQueryParameter(urlBuilder, TERMINAL_EID, odsaOperation.terminalEid());
        appendOptionalQueryParameter(
                urlBuilder, TARGET_TERMINAL_ID, odsaOperation.targetTerminalId());
        appendOptionalQueryParameter(
                urlBuilder, TARGET_TERMINAL_IDS, odsaOperation.targetTerminalIds());
        appendOptionalQueryParameter(
                urlBuilder, TARGET_TERMINAL_ICCID, odsaOperation.targetTerminalIccid());
        appendOptionalQueryParameter(
                urlBuilder, TARGET_TERMINAL_EID, odsaOperation.targetTerminalEid());
        appendOptionalQueryParameter(
                urlBuilder,
                TARGET_TERMINAL_SERIAL_NUMBER,
                odsaOperation.targetTerminalSerialNumber());
        appendOptionalQueryParameter(
                urlBuilder, TARGET_TERMINAL_MODEL, odsaOperation.targetTerminalModel());
        appendOptionalQueryParameter(
                urlBuilder, OLD_TERMINAL_ICCID, odsaOperation.oldTerminalIccid());
        appendOptionalQueryParameter(urlBuilder, OLD_TERMINAL_ID, odsaOperation.oldTerminalId());
    }

    @NonNull
    private HttpResponse httpGet(
            String url,
            CarrierConfig carrierConfig,
            String contentType,
            String terminalVendor,
            String terminalModel,
            String terminalSoftwareVersion)
            throws ServiceEntitlementException {
        HttpRequest.Builder builder =
                HttpRequest.builder()
                        .setUrl(url)
                        .setRequestMethod(RequestMethod.GET)
                        .addRequestProperty(HttpHeaders.ACCEPT, contentType)
                        .setTimeoutInSec(carrierConfig.timeoutInSec())
                        .setNetwork(carrierConfig.network());
        String userAgent =
                getUserAgent(
                        carrierConfig.clientTs43(),
                        terminalVendor,
                        terminalModel,
                        terminalSoftwareVersion);
        if (!TextUtils.isEmpty(userAgent)) {
            builder.addRequestProperty(HttpHeaders.USER_AGENT, userAgent);
        }
        return mHttpClient.request(builder.build());
    }

    private void appendOptionalQueryParameter(Uri.Builder urlBuilder, String key, String value) {
        if (!TextUtils.isEmpty(value)) {
            urlBuilder.appendQueryParameter(key, value);
        }
    }

    private void appendOptionalQueryParameter(
            Uri.Builder urlBuilder, String key, ImmutableList<String> values) {
        if (values != null) {
            for (String value : values) {
                if (!TextUtils.isEmpty(value)) {
                    urlBuilder.appendQueryParameter(key, value);
                }
            }
        }
    }

    @Nullable
    private String getEapAkaChallenge(HttpResponse response) throws ServiceEntitlementException {
        String eapAkaChallenge = null;
        String responseBody = response.body();
        if (response.contentType() == ContentType.JSON) {
            try {
                eapAkaChallenge =
                        new JSONObject(responseBody).optString(EAP_CHALLENGE_RESPONSE, null);
            } catch (JSONException jsonException) {
                throw new ServiceEntitlementException(
                        ERROR_MALFORMED_HTTP_RESPONSE,
                        "Failed to parse json object",
                        jsonException);
            }
        } else if (response.contentType() == ContentType.XML) {
            // TODO: possibly support parsing eap-relay-packet in XML format
            return null;
        } else {
            throw new ServiceEntitlementException(
                    ERROR_MALFORMED_HTTP_RESPONSE, "Unknown HTTP content type");
        }
        return eapAkaChallenge;
    }

    private String getAppVersion(Context context) {
        try {
            PackageInfo packageInfo =
                    context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionName;
        } catch (Exception e) {
            // should be impossible
        }
        return "";
    }

    private String getUserAgent(
            String clientTs43,
            String terminalVendor,
            String terminalModel,
            String terminalSoftwareVersion) {
        if (!TextUtils.isEmpty(clientTs43)
                && !TextUtils.isEmpty(terminalVendor)
                && !TextUtils.isEmpty(terminalModel)
                && !TextUtils.isEmpty(terminalSoftwareVersion)) {
            return String.format(
                    "PRD-TS43 term-%s/%s %s/%s OS-Android/%s",
                    trimString(terminalVendor, MAX_TERMINAL_VENDOR_LENGTH),
                    trimString(terminalModel, MAX_TERMINAL_MODEL_LENGTH),
                    clientTs43,
                    mAppVersion,
                    trimString(terminalSoftwareVersion, MAX_TERMINAL_SOFTWARE_VERSION_LENGTH));
        }
        return "";
    }

    private String trimString(String s, int maxLength) {
        return s.substring(0, Math.min(s.length(), maxLength));
    }

    /**
     * Returns the IMSI EAP value. The resulting realm part of the Root NAI in 3GPP TS 23.003 clause
     * 19.3.2 will be in the form:
     *
     * <p>{@code 0<IMSI>@nai.epc.mnc<MNC>.mcc<MCC>.3gppnetwork.org}
     */
    @Nullable
    public static String getImsiEap(@Nullable String mccmnc, @Nullable String imsi) {
        if (mccmnc == null || mccmnc.length() < 5 || imsi == null) {
            return null;
        }

        String mcc = mccmnc.substring(0, 3);
        String mnc = mccmnc.substring(3);
        if (mnc.length() == 2) {
            mnc = "0" + mnc;
        }
        return "0" + imsi + "@nai.epc.mnc" + mnc + ".mcc" + mcc + ".3gppnetwork.org";
    }

    /** Retrieves the history of past HTTP request and responses. */
    @NonNull
    public List<String> getHistory() {
        return mHttpClient.getHistory();
    }

    /** Clears the history of past HTTP request and responses. */
    public void clearHistory() {
        mHttpClient.clearHistory();
    }
}
