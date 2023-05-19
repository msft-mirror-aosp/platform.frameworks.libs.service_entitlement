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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.libraries.entitlement.odsa.OdsaOperation.CompanionService;
import com.android.libraries.entitlement.odsa.OdsaOperation.ServiceStatus;
import com.android.libraries.entitlement.utils.Ts43Constants;
import com.android.libraries.entitlement.utils.Ts43Constants.AppId;

import com.google.auto.value.AutoValue;

/**
 * Acquire configuration operation described in GSMA Service Entitlement Configuration section 6.
 */
public class AcquireConfigurationOperation {
    /**
     * HTTP request parameters specific to on device service activation (ODSA) acquire configuration
     * operation. See GSMA spec TS.43 section 6.2.
     */
    @AutoValue
    public abstract static class AcquireConfigurationRequest {
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
        @Nullable
        public abstract String companionTerminalId();

        /**
         * Returns the ICCID of the companion device. Used by HTTP parameter
         * {@code companion_terminal_iccid}.
         */
        @Nullable
        public abstract String companionTerminalIccid();

        /**
         * Returns the EID of the companion device. Used by HTTP parameter
         * {@code companion_terminal_eid}.
         */
        @Nullable
        public abstract String companionTerminalEid();

        /**
         * Returns the ICCID of the primary device eSIM. Used by HTTP parameter
         * {@code terminal_iccid}.
         */
        @Nullable
        public abstract String terminalIccid();

        /**
         * Returns the eUICC identifier (EID) of the primary device eSIM. Used by HTTP parameter
         * {@code terminal_eid}.
         */
        @Nullable
        public abstract String terminalEid();

        /**
         * Returns the unique identifier of the primary device eSIM, like the IMEI associated with
         * the eSIM. Used by HTTP parameter {@code target_terminal_id}.
         */
        @Nullable
        public abstract String targetTerminalId();

        /**
         * Returns the ICCID primary device eSIM. Used by HTTP parameter
         * {@code target_terminal_iccid}.
         */
        @Nullable
        public abstract String targetTerminalIccid();

        /**
         * Returns the eUICC identifier (EID) of the primary device eSIM. Used by HTTP parameter
         * {@code target_terminal_eid}.
         */
        @Nullable
        public abstract String targetTerminalEid();

        /**
         * Returns a new {@link Builder} object.
         */
        @NonNull
        public static Builder builder() {
            return new AutoValue_AcquireConfigurationOperation_AcquireConfigurationRequest.Builder()
                    .setCompanionTerminalId("")
                    .setCompanionTerminalIccid("")
                    .setCompanionTerminalEid("")
                    .setTerminalIccid("")
                    .setTerminalEid("")
                    .setTargetTerminalId("")
                    .setTargetTerminalIccid("")
                    .setTargetTerminalEid("");
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
             * Sets the ICCID of the companion device. Used by HTTP parameter
             * {@code companion_terminal_iccid} if set.
             *
             * Used by companion device ODSA operation.
             *
             * @param companionTerminalIccid The ICCID of the companion device.
             *
             * @return The builder.
             */
            @NonNull
            public abstract Builder setCompanionTerminalIccid(
                    @NonNull String companionTerminalIccid);

            /**
             * Sets the eUICC identifier (EID) of the companion device. Used by HTTP parameter
             * {@code companion_terminal_eid} if set.
             *
             * Used by companion device ODSA operation.
             *
             * @param companionTerminalEid The eUICC identifier (EID) of the companion device.
             *
             * @return The builder.
             */
            @NonNull
            public abstract Builder setCompanionTerminalEid(@NonNull String companionTerminalEid);

            /**
             * Sets the ICCID of the primary device eSIM in case of primary SIM not present. Used by
             * HTTP parameter {@code terminal_eid} if set.
             *
             * Used by primary device ODSA operation.
             *
             * @param terminalIccid The ICCID of the primary device eSIM in case of primary SIM not
             * present.
             *
             * @return The builder.
             */
            @NonNull
            public abstract Builder setTerminalIccid(@NonNull String terminalIccid);

            /**
             * Sets the eUICC identifier (EID) of the primary device eSIM in case of primary SIM not
             * present. Used by HTTP parameter {@code terminal_eid} if set.
             *
             * Used by primary device ODSA operation.
             *
             * @param terminalEid The eUICC identifier (EID) of the primary device eSIM in case of
             * primary SIM not present.
             *
             * @return The builder.
             */
            @NonNull
            public abstract Builder setTerminalEid(@NonNull String terminalEid);

            /**
             * Sets the unique identifier of the primary device eSIM in case of multiple SIM, like
             * the IMEI associated with the eSIM. Used by HTTP parameter {@code target_terminal_id}
             * if set.
             *
             * Used by primary device ODSA operation.
             *
             * @param targetTerminalId The unique identifier of the primary device eSIM in case of
             * multiple SIM.
             *
             * @return The builder.
             */
            @NonNull
            public abstract Builder setTargetTerminalId(@NonNull String targetTerminalId);

            /**
             * Sets the ICCID primary device eSIM in case of multiple SIM. Used by HTTP parameter
             * {@code target_terminal_iccid} if set.
             *
             * Used by primary device ODSA operation.
             *
             * @param targetTerminalIccid The ICCID primary device eSIM in case of multiple SIM.
             *
             * @return The builder.
             */
            @NonNull
            public abstract Builder setTargetTerminalIccid(@NonNull String targetTerminalIccid);

            /**
             * Sets the eUICC identifier (EID) of the primary device eSIM in case of multiple SIM.
             * Used by HTTP parameter {@code target_terminal_eid} if set.
             *
             * Used by primary device ODSA operation.
             *
             * @param targetTerminalEid The eUICC identifier (EID) of the primary device eSIM in
             * case of multiple SIM.
             *
             * @return The builder.
             */
            @NonNull
            public abstract Builder setTargetTerminalEid(@NonNull String targetTerminalEid);

            /**
             * @return Build the {@link AcquireConfigurationRequest} object.
             */
            @NonNull
            public abstract AcquireConfigurationRequest build();
        }
    }

    /**
     * Acquire configuration response described in GSMA Service Entitlement Configuration section
     * section 6.5.5 table 40.
     */
    @AutoValue
    public abstract static class AcquireConfigurationResponse {
        /**
         * Integrated Circuit Card Identification - Identifier of the eSIM profile on the device’s
         * eSIM. {@code null} if an eSIM profile does not exist for the device.
         */
        @Nullable
        public abstract String iccid();

        /**
         * Indicates the applicable companion device service. {@code null} if not for companion
         * configuration.
         */
        @Nullable
        @CompanionService
        public abstract String companionDeviceService();

        /**
         * Service status.
         *
         * @see OdsaOperation#SERVICE_STATUS_UNKNOWN
         * @see OdsaOperation#SERVICE_STATUS_ACTIVATED
         * @see OdsaOperation#SERVICE_STATUS_ACTIVATING
         * @see OdsaOperation#SERVICE_STATUS_DEACTIVATED
         * @see OdsaOperation#SERVICE_STATUS_DEACTIVATED_NO_REUSE
         */
        @ServiceStatus
        public abstract int serviceStatus();

        /**
         * Specifies the minimum interval (in minutes) with which the device application may poll
         * the ECS to refresh the current {@link #serviceStatus()} using
         * {@link AcquireConfigurationRequest}. This parameter will be present only when
         * {@link #serviceStatus()} is {@link OdsaOperation#SERVICE_STATUS_ACTIVATING}. If parameter
         * is not present or value is 0, this polling procedure is not triggered and ODSA
         * app will keep waiting for any external action to continue the flow.
         *
         * The maximum number of {@link AcquireConfigurationRequest} before sending a
         * {@link #serviceStatus()} with {@link OdsaOperation#SERVICE_STATUS_DEACTIVATED_NO_REUSE}
         * will be defined as an ECS configuration variable (MaxRefreshRequest).
         */
        public abstract int pollingInterval();

        /**
         * Specifies how and where to download the eSIM profile associated with the device.
         * Present in case the profile is to be downloaded at this stage.
         */
        @Nullable
        public abstract DownloadInfo downloadInfo();

        /**
         * Includes all information collected by the ES of the companion device.
         */
        @Nullable
        public abstract CompanionDeviceInfo companionDeviceInfo();

        /**
         * @return The builder.
         */
        public static Builder builder() {
            return new AutoValue_AcquireConfigurationOperation_AcquireConfigurationResponse
                    .Builder();
        }

        /**
         * The builder of {@link AcquireConfigurationResponse}
         */
        @AutoValue.Builder
        public abstract static class Builder {
            /**
             * Set the iccid.
             *
             * @param iccid Integrated Circuit Card Identification - Identifier of the eSIM
             * profile on the device’s eSIM.
             *
             * @return The builder.
             */
            @NonNull
            public abstract Builder setIccid(@NonNull String iccid);

            /**
             * Set the applicable companion device service.
             *
             * @param companionDeviceService Indicates the applicable companion device service.
             *
             * @return The builder.
             */
            @NonNull
            public abstract Builder setCompanionDeviceService(
                    @NonNull @CompanionService String companionDeviceService);

            /**
             * Set the service status.
             *
             * @param serviceStatus Service status.
             *
             * @return The builder.
             */
            @NonNull
            public abstract Builder setServiceStatus(@ServiceStatus int serviceStatus);

            /**
             * Set the polling interval.
             *
             * @param pollingInterval The minimum interval (in minutes) with which the device
             * application may poll the ECS to refresh the current {@link #serviceStatus()} using
             * {@link AcquireConfigurationRequest}.
             *
             * @return The builder.
             */
            @NonNull
            public abstract Builder setPollingInterval(int pollingInterval);

            /**
             * Set the download information.
             *
             * @param downloadInfo Specifies how and where to download the eSIM profile associated
             * with the device.
             *
             * @return The builder.
             */
            @NonNull
            public abstract Builder setDownloadInfo(@NonNull DownloadInfo downloadInfo);

            /**
             * Set the companion device info.
             *
             * @param companionDeviceInfo Includes all information collected by the ES of the
             * companion device.
             *
             * @return The builder.
             */
            @NonNull
            public abstract Builder setCompanionDeviceInfo(
                    @NonNull CompanionDeviceInfo companionDeviceInfo);

            /**
             * @return Build the {@link AcquireConfigurationResponse} object.
             */
            @NonNull
            public abstract AcquireConfigurationResponse build();
        }
    }
}
