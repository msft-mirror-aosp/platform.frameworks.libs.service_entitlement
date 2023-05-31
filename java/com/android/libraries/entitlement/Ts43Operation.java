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

import android.content.Context;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.libraries.entitlement.odsa.AcquireConfigurationOperation.AcquireConfigurationRequest;
import com.android.libraries.entitlement.odsa.AcquireConfigurationOperation.AcquireConfigurationResponse;
import com.android.libraries.entitlement.odsa.AcquireTemporaryTokenOperation.AcquireTemporaryTokenRequest;
import com.android.libraries.entitlement.odsa.AcquireTemporaryTokenOperation.AcquireTemporaryTokenResponse;
import com.android.libraries.entitlement.odsa.CheckEligibilityOperation.CheckEligibilityRequest;
import com.android.libraries.entitlement.odsa.CheckEligibilityOperation.CheckEligibilityResponse;
import com.android.libraries.entitlement.odsa.ManageServiceOperation.ManageServiceRequest;
import com.android.libraries.entitlement.odsa.ManageSubscriptionOperation.ManageSubscriptionRequest;
import com.android.libraries.entitlement.odsa.ManageSubscriptionOperation.ManageSubscriptionResponse;
import com.android.libraries.entitlement.odsa.OdsaOperation;
import com.android.libraries.entitlement.odsa.OdsaOperation.ServiceStatus;
import com.android.libraries.entitlement.odsa.PlanOffer;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.net.URL;
import java.util.Collections;
import java.util.List;

/**
 * TS43 operations described in GSMA Service Entitlement Configuration section.
 */
public class Ts43Operation {
    /**
     * The normal token retrieved via
     * {@link Ts43Authentication#getAuthToken(Context, int, URL, String, String, String, String)}
     * or {@link Ts43Authentication#getAuthToken(URL)}.
     */
    public static final int TOKEN_TYPE_NORMAL = 1;

    /**
     * The temporary token retrieved via
     * {@link Ts43Operation#acquireTemporaryToken(AcquireTemporaryTokenRequest)}.
     */
    public static final int TOKEN_TYPE_TEMPORARY = 2;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({
            TOKEN_TYPE_NORMAL,
            TOKEN_TYPE_TEMPORARY
    })
    public @interface TokenType {}

    /**
     * The authentication token used for TS.43 operation. This token could be automatically updated
     * after each TS.43 operation if the server provides the new token in the operation's HTTP
     * response.
     */
    @NonNull
    private String mAuthToken;

    /**
     * Constructor of Ts43Operation.
     *
     * @param slotIndex The logical SIM slot index involved in ODSA operation.
     * @param entitlementServerAddress The entitlement server address.
     * @param entitlementVersion The TS.43 entitlement version to use. For example, {@code "9.0"}.
     * If {@code null}, version {@code "2.0"} will be used by default.
     * @param authToken The authentication token.
     * @param tokenType The token type. Can be {@link #TOKEN_TYPE_NORMAL} or
     * {@link #TOKEN_TYPE_TEMPORARY}.
     */
    public Ts43Operation(int slotIndex, @NonNull URL entitlementServerAddress,
            @Nullable String entitlementVersion, @NonNull String authToken,
            @TokenType int tokenType) {
        mAuthToken = authToken;
    }

    /**
     * To verify if end-user is allowed to invoke the ODSA application as described in GSMA Service
     * Entitlement Configuration section 6.2 and 6.5.2.
     *
     * @return {@code true} if the end-user is allowed to perform ODSA operation.
     *
     * @throws ServiceEntitlementException The exception for error case. If it's an HTTP response
     * error from the server, the error code can be retrieved by
     * {@link ServiceEntitlementException#getHttpStatus()}
     */
    @NonNull
    public CheckEligibilityResponse checkEligibility(
            @NonNull CheckEligibilityRequest checkEligibilityRequest)
            throws ServiceEntitlementException {
        return null;
    }

    /**
     * To request for subscription-related action on a primary or companion device as described
     * in GSMA Service Entitlement Configuration section 6.2 and 6.5.3.
     *
     * @param manageSubscriptionOperation The manage subscription request.
     * @return The response of manage subscription request.
     *
     * @throws ServiceEntitlementException The exception for error case. If it's an HTTP response
     * error from the server, the error code can be retrieved by
     * {@link ServiceEntitlementException#getHttpStatus()}
     */
    @NonNull
    public ManageSubscriptionResponse manageSubscription(
            @NonNull ManageSubscriptionRequest manageSubscriptionOperation)
            throws ServiceEntitlementException {
        return null;
    }

    /**
     * To activate/deactivate the service on the primary or companion device as described in GSMA
     * Service Entitlement Configuration section 6.2 and 6.5.4. This is an optional operation.
     *
     * @param manageServiceRequest The manage service request.
     * @return The response of manage service request.
     *
     * @throws ServiceEntitlementException The exception for error case. If it's an HTTP response
     * error from the server, the error code can be retrieved by
     * {@link ServiceEntitlementException#getHttpStatus()}
     */
    @ServiceStatus
    public int manageService(@NonNull ManageServiceRequest manageServiceRequest)
            throws ServiceEntitlementException {
        return OdsaOperation.SERVICE_STATUS_UNKNOWN;
    }

    /**
     * To provide service related data about a primary or companion device as described in GSMA
     * Service Entitlement Configuration section 6.2 and 6.5.5.
     *
     * @param acquireConfigurationRequest The acquire configuration request.
     * @return The response of acquire configuration request.
     *
     * @throws ServiceEntitlementException The exception for error case. If it's an HTTP response
     * error from the server, the error code can be retrieved by
     * {@link ServiceEntitlementException#getHttpStatus()}
     */
    @NonNull
    public AcquireConfigurationResponse acquireConfiguration(
            @NonNull AcquireConfigurationRequest acquireConfigurationRequest)
            throws ServiceEntitlementException {
        return null;
    }

    /**
     * Acquire available mobile plans to be offered by the MNO to a specific user or MDM as
     * described in GSMA Service Entitlement Configuration section 6.2 and 6.5.6.
     *
     * @return List of mobile plans. Empty list if not available.
     *
     * @throws ServiceEntitlementException The exception for error case. If it's an HTTP response
     * error from the server, the error code can be retrieved by
     * {@link ServiceEntitlementException#getHttpStatus()}
     */
    @NonNull
    public List<PlanOffer> acquirePlans() throws ServiceEntitlementException {
        return Collections.emptyList();
    }

    /**
     * To request a temporary token used to establish trust between ECS and the client as described
     * in GSMA Service Entitlement Configuration section 6.2 and 6.5.7.
     *
     * @param acquireTemporaryTokenRequest The acquire temporary token request.
     *
     * @return The temporary token response.
     *
     * @throws ServiceEntitlementException The exception for error case. If it's an HTTP response
     * error from the server, the error code can be retrieved by
     * {@link ServiceEntitlementException#getHttpStatus()}
     */
    @NonNull
    public AcquireTemporaryTokenResponse acquireTemporaryToken(
            @NonNull AcquireTemporaryTokenRequest acquireTemporaryTokenRequest)
            throws ServiceEntitlementException {
        return null;
    }

    /**
     * Get the phone number as described in GSMA Service Entitlement Configuration section
     * 6.2 and 6.5.8.
     *
     * @return The phone number in E.164 format.
     *
     * @throws ServiceEntitlementException The exception for error case. If it's an HTTP response
     * error from the server, the error code can be retrieved by
     * {@link ServiceEntitlementException#getHttpStatus()}
     */
    @NonNull
    public String getPhoneNumber() throws ServiceEntitlementException {
        return "";
    }
}
