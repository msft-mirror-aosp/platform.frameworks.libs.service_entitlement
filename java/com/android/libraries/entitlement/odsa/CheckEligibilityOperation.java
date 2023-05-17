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

package com.android.libraries.entitlement.odsa;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.libraries.entitlement.odsa.OdsaOperation.CompanionService;
import com.android.libraries.entitlement.utils.HttpConstants;
import com.android.libraries.entitlement.utils.HttpConstants.ContentType;
import com.android.libraries.entitlement.utils.Ts43Constants;
import com.android.libraries.entitlement.utils.Ts43Constants.AppId;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.net.URL;

/**
 * Check eligibility operation described in GSMA Service Entitlement Configuration section 6.
 */
public class CheckEligibilityOperation {
    /**
     * ODSA app check eligibility result unknown.
     */
    public static final int ELIGIBILITY_RESULT_UNKNOWN = -1;
    /**
     * ODSA app cannot be offered and invoked by the end-user.
     */
    public static final int ELIGIBILITY_RESULT_DISABLED = 0;

    /**
     * ODSA app can be invoked by end-user or to activate a new subscription.
     */
    public static final int ELIGIBILITY_RESULT_ENABLED = 1;

    /**
     * ODSA app is not compatible with the device or server.
     */
    public static final int ELIGIBILITY_RESULT_INCOMPATIBLE = 2;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({
            ELIGIBILITY_RESULT_UNKNOWN,
            ELIGIBILITY_RESULT_DISABLED,
            ELIGIBILITY_RESULT_ENABLED,
            ELIGIBILITY_RESULT_INCOMPATIBLE
    })
    public @interface EligibilityResult {}

    /**
     * HTTP request parameters specific to on device service activation (ODSA). See GSMA spec TS.43
     * section 6.2.
     */
    @AutoValue
    public abstract static class CheckEligibilityRequest {
        /**
         * Returns the application id. Can only be {@link Ts43Constants#APP_ODSA_COMPANION},
         * {@link Ts43Constants#APP_ODSA_PRIMARY}, or
         * {@link Ts43Constants#APP_ODSA_SERVER_INITIATED_REQUESTS}.
         */
        @AppId
        public abstract String appId();

        /**
         * Returns the unique identifier of the companion device, like IMEI. Used by HTTP parameter
         * {@code companion_terminal_id}.
         */
        public abstract String companionTerminalId();

        /**
         * Returns the OEM of the companion device. Used by HTTP parameter
         * {@code companion_terminal_vendor}.
         */
        public abstract String companionTerminalVendor();

        /**
         * Returns the model of the companion device. Used by HTTP parameter
         * {@code companion_terminal_model}.
         */
        public abstract String companionTerminalModel();

        /**
         * Returns the software version of the companion device. Used by HTTP parameter
         * {@code companion_terminal_sw_version}.
         */
        public abstract String companionTerminalSoftwareVersion();

        /**
         * Returns the user-friendly version of the companion device. Used by HTTP parameter
         * {@code companion_terminal_friendly_name}.
         */
        public abstract String companionTerminalFriendlyName();

        /**
         * Returns a new {@link Builder} object.
         */
        public static Builder builder() {
            return new AutoValue_CheckEligibilityOperation_CheckEligibilityRequest.Builder()
                    .setCompanionTerminalId("")
                    .setCompanionTerminalVendor("")
                    .setCompanionTerminalModel("")
                    .setCompanionTerminalSoftwareVersion("")
                    .setCompanionTerminalFriendlyName("");
        }

        /**
         * Builder
         */
        @AutoValue.Builder
        public abstract static class Builder {
            /**
             * Sets the application id.
             *
             * @param appId The application id. Can only be
             * {@link Ts43Constants#APP_ODSA_COMPANION}, {@link Ts43Constants#APP_ODSA_PRIMARY}, or
             * {@link Ts43Constants#APP_ODSA_SERVER_INITIATED_REQUESTS}.
             *
             * @return The builder.
             */
            @NonNull
            public abstract Builder setAppId(@NonNull @AppId String appId);

            /**
             * Sets the unique identifier of the companion device, like IMEI. Used by HTTP parameter
             * {@code companion_terminal_id} if set.
             *
             * Used by companion device ODSA operation.
             *
             * @param companionTerminalId The unique identifier of the companion device.
             *
             * @return The builder.
             */
            @NonNull
            public abstract Builder setCompanionTerminalId(@NonNull String companionTerminalId);

            /**
             * Sets the OEM of the companion device. Used by HTTP parameter
             * {@code companion_terminal_vendor}.
             * if set.
             *
             * Used by companion device ODSA operation.
             *
             * @param companionTerminalVendor The OEM of the companion device.
             *
             * @return The builder.
             */
            @NonNull
            public abstract Builder setCompanionTerminalVendor(
                    @NonNull String companionTerminalVendor);

            /**
             * Sets the model of the companion device. Used by HTTP parameter
             * {@code companion_terminal_model}.
             * if set.
             *
             * Used by companion device ODSA operation.
             *
             * @param companionTerminalModel The model of the companion device.
             *
             * @return The builder.
             */
            @NonNull
            public abstract Builder setCompanionTerminalModel(
                    @NonNull String companionTerminalModel);

            /**
             * Sets the software version of the companion device. Used by HTTP parameter
             * {@code companion_terminal_sw_version} if set.
             *
             * Used by companion device ODSA operation.
             *
             * @param companionTerminalSoftwareVersion The software version of the companion device.
             *
             * @return The builder.
             */
            @NonNull
            public abstract Builder setCompanionTerminalSoftwareVersion(
                    @NonNull String companionTerminalSoftwareVersion);

            /**
             * Sets the user-friendly version of the companion device. Used by HTTP parameter
             * {@code companion_terminal_friendly_name} if set.
             *
             * Used by companion device ODSA operation.
             *
             * @param companionTerminalFriendlyName The user-friendly version of the companion
             * device.
             *
             * @return The builder.
             */
            @NonNull
            public abstract Builder setCompanionTerminalFriendlyName(
                    @NonNull String companionTerminalFriendlyName);

            /**
             * @return The {@link CheckEligibilityRequest} object.
             */
            @NonNull
            public abstract CheckEligibilityRequest build();
        }
    }

    /**
     * Check eligibility response described in GSMA Service Entitlement Configuration section 6.5.2.
     */
    @AutoValue
    public abstract static class CheckEligibilityResponse {
        /**
         * @return The result of check eligibility request.
         */
        @EligibilityResult
        public abstract int appEligibility();

        /**
         * Indicates the applicable companion device services.
         */
        @NonNull
        @CompanionService
        public abstract ImmutableList<String> companionDeviceServices();

        /**
         * The provided URL shall present a web view to user on the reason(s) why the ODSA app
         * cannot be used/invoked.
         */
        @Nullable
        public abstract URL notEnabledURL();

        /**
         * User data sent to the Service Provider when requesting the {@link #notEnabledURL()} web
         * view. It should contain user-specific attributes to improve user experience. The format
         * must follow the {@link #notEnabledContentsType()} parameter. For content types of
         * {@code JSON} and {@code XML}, it is possible to provide the base64 encoding of the
         * value by preceding it with {@code encodedValue=}.
         */
        @Nullable
        public abstract String notEnabledUserData();

        /**
         * Specifies content and HTTP method to use when reaching out to the web server specified in
         * {@link #notEnabledURL()}.
         */
        @ContentType
        public abstract int notEnabledContentsType();

        /**
         * @return The builder.
         */
        public static Builder builder() {
            return new AutoValue_CheckEligibilityOperation_CheckEligibilityResponse.Builder();
        }

        /**
         * The builder.
         */
        @AutoValue.Builder
        public abstract static class Builder {
            /**
             * Set the eligibility.
             *
             * @param eligibility The result of check eligibility request.
             *
             * @return The builder.
             */
            @NonNull
            public abstract Builder setAppEligibility(@EligibilityResult int eligibility);

            /**
             * Set the companion device services.
             *
             * @param companionDeviceServices The applicable companion device services.
             *
             * @return The builder.
             */
            @NonNull
            public abstract Builder setCompanionDeviceServices(
                    @NonNull @CompanionService ImmutableList<String> companionDeviceServices);

            /**
             * Set the URL presenting a web view to user on the reason(s) why the ODSA app cannot be
             * used/invoked.
             *
             * @param url The provided URL shall present a web view to user on the reason(s) why
             * the ODSA app cannot be used/invoked.
             *
             * @return The builder.
             */
            @NonNull
            public abstract Builder setNotEnabledURL(@NonNull URL url);

            /**
             * Set the user data sent to the Service Provider when requesting the
             * {@link #notEnabledURL()} web view.
             *
             * @param notEnabledUserData User data sent to the Service Provider when requesting
             * the {@link #notEnabledURL()} web view. It should contain user-specific attributes
             * to improve user experience. The format must follow the
             * {@link #notEnabledContentsType()} parameter. For content types of
             * {@link HttpConstants#JSON} and {@link HttpConstants#XML}, it is possible to provide
             * the base64 encoding of the value by preceding it with {@code encodedValue=}.
             *
             * @return The builder.
             */
            @NonNull
            public abstract Builder setNotEnabledUserData(
                    @NonNull String notEnabledUserData);

            /**
             * Set the content and HTTP method to use when reaching out to the web server specified
             * in {@link #notEnabledURL()}.
             *
             * @param notEnabledContentsType Specifies content and HTTP method to use when
             * reaching out to the web server specified in {@link #notEnabledURL()}.
             *
             * @return The builder.
             */
            @NonNull
            public abstract Builder setNotEnabledContentsType(
                    @ContentType int notEnabledContentsType);

            /**
             * Build the {@link CheckEligibilityResponse} object.
             */
            public abstract CheckEligibilityResponse build();
        }
    }
}
