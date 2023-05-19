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

import android.telephony.SubscriptionInfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.libraries.entitlement.utils.Ts43Constants;
import com.android.libraries.entitlement.utils.Ts43Constants.AppId;

import com.google.auto.value.AutoValue;

import java.net.URL;

/**
 * The class responsible for TS.43 authentication process.
 */
public class Ts43Authentication {
    /**
     * The authentication token for TS.43 operation.
     */
    @AutoValue
    public abstract static class Ts43AuthToken {
        /**
         * The authentication token for TS.43 operations.
         */
        @NonNull
        public abstract String token();

        /**
         * Indicates the validity of the token. Note this value is server dependent. The client is
         * expected to interpret this value itself.
         */
        @NonNull
        public abstract long validity();

        /**
         * Create the {@link Ts43AuthToken} object.
         *
         * @param token The authentication token for TS.43 operations.
         * @param validity Indicates the validity of the token in seconds defined in GSMA Service
         * Provider Device Configuration section 4.2. The validity is counted from the time it is
         * received by the client in the configuration XML/JSON document.
         *
         * @return The {@link Ts43AuthToken} object.
         */
        public static Ts43AuthToken create(@NonNull String token, long validity) {
            return new AutoValue_Ts43Authentication_Ts43AuthToken(token, validity);
        }
    }

    /**
     * Get the authentication token for TS.43 operations with EAP-AKA described in TS.43
     * Service Entitlement Configuration section 2.8.1.
     *
     * @param slotIndex The logical SIM slot index involved in ODSA operation.
     * See {@link SubscriptionInfo#getSubscriptionId()}.
     * @param entitlementServerAddress The entitlement server address.
     * @param entitlementVersion The TS.43 entitlement version to use. For example, {@code "9.0"}.
     * If {@code null}, version {@code "2.0"} will be used by default.
     * @param appId Application id. For example, {@link Ts43Constants#APP_VOWIFI} for VoWifi,
     * {@link Ts43Constants#APP_ODSA_PRIMARY } for ODSA primary device. Refer GSMA to Service
     * Entitlement Configuration section 2.3.
     *
     * @return The authentication token.
     *
     * @throws ServiceEntitlementException The exception for error case. If it's an HTTP response
     * error from the server, the error code can be retrieved by
     * {@link ServiceEntitlementException#getHttpStatus()}
     */
    @NonNull
    public static Ts43AuthToken getAuthToken(int slotIndex,
            @NonNull URL entitlementServerAddress, @Nullable String entitlementVersion,
            @NonNull @AppId String appId) throws ServiceEntitlementException {
        return null;
    }

    /**
     * Get the URL of OIDC (OpenID Connect) server as described in TS.43 Service Entitlement
     * Configuration section 2.8.2.
     *
     * The caller is expected to present the content of the URL to the user to proceed the
     * authentication process. After that the caller can call {@link #getAuthToken(URL)}
     * to get the authentication token.
     *
     * @param slotIndex The logical SIM slot index involved in ODSA operation.
     * @param entitlementServerAddress The entitlement server address.
     * @param entitlementVersion The TS.43 entitlement version to use. For example, {@code "9.0"}.
     * @param appId Application id. For example, {@link Ts43Constants#APP_VOWIFI} for VoWifi,
     * {@link Ts43Constants#APP_ODSA_PRIMARY } for ODSA primary device. Refer GSMA to Service
     * Entitlement Configuration section 2.3.
     *
     * @return The URL of OIDC server with all the required parameters for client to launch a
     * user interface for users to interact with the authentication process. The parameters in URL
     * include {@code client_id}, {@code redirect_uri}, {@code state}, and {@code nonce}.
     *
     * @throws ServiceEntitlementException The exception for error case. If it's an HTTP response
     * error from the server, the error code can be retrieved by
     * {@link ServiceEntitlementException#getHttpStatus()}
     */
    @NonNull
    public static URL getOidcAuthServer(int slotIndex, @NonNull URL entitlementServerAddress,
            @Nullable String entitlementVersion, @NonNull @AppId String appId)
            throws ServiceEntitlementException {
        return null;
    }

    /**
     * Get the authentication token for TS.43 operations with OIDC (OpenID Connect) described in
     * TS.43 Service Entitlement Configuration section 2.8.2.
     *
     * @param aesUrl The AES URL used to retrieve auth token. The parameters in the URL include
     * the OIDC auth code {@code code} and {@code state}.
     *
     * @return The authentication token.
     *
     * @throws ServiceEntitlementException The exception for error case. If it's an HTTP response
     * error from the server, the error code can be retrieved by
     * {@link ServiceEntitlementException#getHttpStatus()}
     */
    @NonNull
    public static Ts43AuthToken getAuthToken(@NonNull URL aesUrl)
            throws ServiceEntitlementException {
        return null;
    }
}
