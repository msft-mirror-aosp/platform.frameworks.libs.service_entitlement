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

import android.content.Context;
import android.net.Uri;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;

import com.android.libraries.entitlement.ServiceEntitlementException;
import com.android.libraries.entitlement.ServiceEntitlementRequest;
import com.android.libraries.entitlement.http.HttpClient;
import com.android.libraries.entitlement.http.HttpConstants.ContentType;
import com.android.libraries.entitlement.http.HttpConstants.RequestMethod;
import com.android.libraries.entitlement.http.HttpRequest;
import com.android.libraries.entitlement.http.HttpResponse;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.net.HttpHeaders;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.CookieHandler;
import java.net.CookieManager;

public class EapAkaApi {
    private static final String TAG = "ServiceEntitlement";

    public static final String EAP_CHALLENGE_RESPONSE = "eap-relay-packet";

    /**
     * Current version of the entitlement configuration.
     */
    private static final String VERS = "vers";
    /**
     * Version of the entitlement configuration.
     */
    private static final String ENTITLEMENT_VERSION = "entitlement_version";
    /**
     * Unique identifier for the device. Refer to
     * {@link android.telephony.TelephonyManager#getImei()}.
     */
    private static final String TERMINAL_ID = "terminal_id";
    /**
     * Device manufacturer.
     */
    private static final String TERMINAL_VENDOR = "terminal_vendor";
    /**
     * Device model.
     */
    private static final String TERMINAL_MODEL = "terminal_model";
    /**
     * Device software version.
     */
    private static final String TERMIAL_SW_VERSION = "terminal_sw_version";
    /**
     * Identifier for the requested entitlement.
     */
    private static final String APP = "app";
    /**
     * NAI needed for EAP-AKA authentication.
     */
    private static final String EAP_ID = "EAP_ID";

    private static final String IMSI = "IMSI";
    private static final String TOKEN = "token";
    /**
     * Action for the notification registration token.
     */
    private static final String NOTIF_ACTION = "notif_action";
    /**
     * Attribute name of the notification registration token.
     */
    private static final String NOTIF_TOKEN = "notif_token";
    /**
     * Attribute name of the app version.
     */
    private static final String APP_VERSION = "app_version";
    /**
     * Attribute name of the app name.
     */
    private static final String APP_NAME = "app_name";

    private final Context mContext;
    private final int mSimSubscriptionId;
    private final HttpClient mHttpClient;

    public EapAkaApi(Context context, int simSubscriptionId) {
        this(context, simSubscriptionId, new HttpClient());
    }

    @VisibleForTesting
    EapAkaApi(Context context, int simSubscriptionId, HttpClient httpClient) {
        this.mContext = context;
        this.mSimSubscriptionId = simSubscriptionId;
        this.mHttpClient = httpClient;
    }

    /**
     * Retrieves raw entitlement configuration doc though EAP-AKA authentication.
     *
     * <p>Implementation based on GSMA TS.43-v5.0 2.6.1.
     *
     * @throws ServiceEntitlementException when getting an unexpected http response.
     */
    @Nullable
    public String queryEntitlementStatus(
            String appId, String serverUrl, ServiceEntitlementRequest request)
            throws ServiceEntitlementException {
        // TODO(b/177562073): localize cookie management instead of VM global CookieHandler
        CookieHandler.setDefault(new CookieManager());

        HttpRequest httpRequest =
                HttpRequest.builder()
                        .setUrl(entitlementStatusUrl(appId, serverUrl, request))
                        .setRequestMethod(RequestMethod.GET)
                        .addRequestProperty(
                                HttpHeaders.ACCEPT,
                                "application/vnd.gsma.eap-relay.v1.0+json, text/vnd.wap"
                                        + ".connectivity-xml")
                        .build();
        HttpResponse response = mHttpClient.request(httpRequest);
        if (response == null) {
            throw new ServiceEntitlementException("Null http response");
        }
        if (response.contentType() == ContentType.JSON) {
            try {
                // EapAka token challenge for initial AuthN
                Log.d(TAG, "initial AuthN");
                String akaChallengeResponse =
                        new EapAkaResponse(
                                new JSONObject(response.body()).getString(EAP_CHALLENGE_RESPONSE))
                                .getEapAkaChallengeResponse(mContext, mSimSubscriptionId);
                JSONObject postData = new JSONObject();
                postData.put(EAP_CHALLENGE_RESPONSE, akaChallengeResponse);
                return challengeResponse(postData, serverUrl);
            } catch (JSONException jsonException) {
                Log.e(TAG, "queryEntitlementStatus failed. jsonException: " + jsonException);
                return null;
            }
        } else if (response.contentType() == ContentType.XML) {
            // Result of fast AuthN
            Log.d(TAG, "fast AuthN");
            return response.body();
        }
        throw new ServiceEntitlementException("Unexpected http ContentType");
    }

    private String challengeResponse(JSONObject postData, String serverUrl)
            throws ServiceEntitlementException {
        Log.d(TAG, "challengeResponse");
        HttpRequest request =
                HttpRequest.builder()
                        .setUrl(serverUrl)
                        .setRequestMethod(RequestMethod.POST)
                        .setPostData(postData)
                        .addRequestProperty(
                                HttpHeaders.ACCEPT,
                                "application/vnd.gsma.eap-relay.v1.0+json, text/vnd.wap"
                                        + ".connectivity-xml")
                        .addRequestProperty(HttpHeaders.CONTENT_TYPE,
                                "application/vnd.gsma.eap-relay.v1.0+json")
                        .build();

        HttpResponse response = mHttpClient.request(request);
        if (response == null || response.contentType() != ContentType.XML) {
            throw new ServiceEntitlementException("Unexpected http response.");
        }

        return response.body();
    }

    @VisibleForTesting
    String entitlementStatusUrl(
            String appId, String serverUrl, ServiceEntitlementRequest request) {
        TelephonyManager telephonyManager = mContext.getSystemService(
                TelephonyManager.class).createForSubscriptionId(mSimSubscriptionId);
        Uri.Builder urlBuilder = Uri.parse(serverUrl).buildUpon();
        if (TextUtils.isEmpty(request.authenticationToken())) {
            // EAP_ID required for initial AuthN
            urlBuilder.appendQueryParameter(
                    EAP_ID,
                    getImsiEap(telephonyManager.getSimOperator(),
                            telephonyManager.getSubscriberId()));
        } else {
            // IMSI and token required for fast AuthN.
            urlBuilder
                    .appendQueryParameter(IMSI, telephonyManager.getSubscriberId())
                    .appendQueryParameter(TOKEN, request.authenticationToken());
        }

        if (!TextUtils.isEmpty(request.notificationToken())) {
            urlBuilder
                    .appendQueryParameter(NOTIF_ACTION,
                            Integer.toString(request.notificationAction()))
                    .appendQueryParameter(NOTIF_TOKEN, request.notificationToken());
        }

        // Assign terminal ID with device IMEI if not set.
        if (TextUtils.isEmpty(request.terminalId())) {
            urlBuilder.appendQueryParameter(TERMINAL_ID, telephonyManager.getImei());
        } else {
            urlBuilder.appendQueryParameter(TERMINAL_ID, request.terminalId());
        }

        // Optional query parameters, append them if not empty
        if (!TextUtils.isEmpty(request.appVersion())) {
            urlBuilder.appendQueryParameter(APP_VERSION, request.appVersion());
        }

        if (!TextUtils.isEmpty(request.appName())) {
            urlBuilder.appendQueryParameter(APP_NAME, request.appName());
        }

        return urlBuilder
                // Identity and Authentication parameters
                .appendQueryParameter(TERMINAL_VENDOR, request.terminalVendor())
                .appendQueryParameter(TERMINAL_MODEL, request.terminalModel())
                .appendQueryParameter(TERMIAL_SW_VERSION, request.terminalSoftwareVersion())
                // General Service parameters
                .appendQueryParameter(APP, appId)
                .appendQueryParameter(VERS, Integer.toString(request.configurationVersion()))
                .appendQueryParameter(ENTITLEMENT_VERSION, request.entitlementVersion())
                .toString();
    }

    /**
     * Returns the IMSI EAP value. The resulting realm part of the Root NAI in 3GPP TS 23.003 clause
     * 19.3.2 will be in the form:
     *
     * <p>{@code 0<IMSI>@nai.epc.mnc<MNC>.mcc<MCC>.3gppnetwork.org}
     */
    @Nullable
    static String getImsiEap(@Nullable String mccmnc, @Nullable String imsi) {
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
}
