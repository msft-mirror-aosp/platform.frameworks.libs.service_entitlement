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

package com.android.libraries.entitlement.odsa;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.StringDef;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * HTTP request parameters specific to on device service activation (ODSA). See GSMA spec TS.43
 * section 6.2.
 */
@AutoValue
public abstract class OdsaOperation {
    /**
     * ODSA operation: CheckEligibility.
     */
    public static final String OPERATION_CHECK_ELIGIBILITY = "CheckEligibility";

    /**
     * ODSA operation: ManageSubscription.
     */
    public static final String OPERATION_MANAGE_SUBSCRIPTION = "ManageSubscription";

    /**
     * ODSA operation: ManageService.
     */
    public static final String OPERATION_MANAGE_SERVICE = "ManageService";

    /**
     * ODSA operation: AcquireConfiguration.
     */
    public static final String OPERATION_ACQUIRE_CONFIGURATION = "AcquireConfiguration";

    /**
     * ODSA operation: AcquireTemporaryToken.
     */
    public static final String OPERATION_ACQUIRE_TEMPORARY_TOKEN = "AcquireTemporaryToken";

    /**
     * ODSA operation: GetPhoneNumber
     */
    public static final String OPERATION_GET_PHONE_NUMBER = "GetPhoneNumber";

    /**
     * ODSA operation: AcquirePlan
     */
    public static final String OPERATION_ACQUIRE_PLAN = "AcquirePlan";

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({
        OPERATION_CHECK_ELIGIBILITY,
        OPERATION_MANAGE_SUBSCRIPTION,
        OPERATION_MANAGE_SERVICE,
        OPERATION_ACQUIRE_CONFIGURATION,
        OPERATION_ACQUIRE_PLAN,
        OPERATION_ACQUIRE_TEMPORARY_TOKEN,
        OPERATION_GET_PHONE_NUMBER
    })
    public @interface Operation {}

    /** eSIM device’s service is unknown. */
    public static final int SERVICE_STATUS_UNKNOWN = -1;

    /** eSIM device’s service is activated. */
    public static final int SERVICE_STATUS_ACTIVATED = 1;

    /** eSIM device’s service is being activated. */
    public static final int SERVICE_STATUS_ACTIVATING = 2;

    /** eSIM device’s service is not activated. */
    public static final int SERVICE_STATUS_DEACTIVATED = 3;

    /** eSIM device’s service is not activated and the associated ICCID should not be reused. */
    public static final int SERVICE_STATUS_DEACTIVATED_NO_REUSE = 4;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({
        SERVICE_STATUS_UNKNOWN,
        SERVICE_STATUS_ACTIVATED,
        SERVICE_STATUS_ACTIVATING,
        SERVICE_STATUS_DEACTIVATED,
        SERVICE_STATUS_DEACTIVATED_NO_REUSE
    })
    public @interface ServiceStatus {}

    /**
     * Indicates that operation_type is not set.
     */
    public static final int OPERATION_TYPE_NOT_SET = -1;

    /**
     * To activate a subscription, used by {@link #OPERATION_MANAGE_SUBSCRIPTION}.
     */
    public static final int OPERATION_TYPE_SUBSCRIBE = 0;

    /**
     * To cancel a subscription, used by {@link #OPERATION_MANAGE_SUBSCRIPTION}.
     */
    public static final int OPERATION_TYPE_UNSUBSCRIBE = 1;

    /**
     * To manage an existing subscription, for {@link #OPERATION_MANAGE_SUBSCRIPTION}.
     */
    public static final int OPERATION_TYPE_CHANGE_SUBSCRIPTION = 2;

    /**
     * To transfer a subscription from an existing device, used by
     * {@link #OPERATION_MANAGE_SUBSCRIPTION}.
     */
    public static final int OPERATION_TYPE_TRANSFER_SUBSCRIPTION = 3;

    /**
     * To inform the network of a subscription update, used by
     * {@link #OPERATION_MANAGE_SUBSCRIPTION}.
     */
    public static final int OPERATION_TYPE_UPDATE_SUBSCRIPTION = 4;

    /**
     * To activate a service, used by {@link #OPERATION_MANAGE_SERVICE}.
     */
    public static final int OPERATION_TYPE_ACTIVATE_SERVICE = 10;

    /**
     * To deactivate a service, used by {@link #OPERATION_MANAGE_SERVICE}.
     */
    public static final int OPERATION_TYPE_DEACTIVATE_SERVICE = 11;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({
        OPERATION_TYPE_NOT_SET,
        OPERATION_TYPE_SUBSCRIBE,
        OPERATION_TYPE_UNSUBSCRIBE,
        OPERATION_TYPE_CHANGE_SUBSCRIPTION,
        OPERATION_TYPE_TRANSFER_SUBSCRIPTION,
        OPERATION_TYPE_UPDATE_SUBSCRIPTION,
        OPERATION_TYPE_ACTIVATE_SERVICE,
        OPERATION_TYPE_DEACTIVATE_SERVICE
    })
    public @interface OperationType {}

    /**
     * Indicates the companion device carries the same MSISDN as the primary device.
     */
    public static final String COMPANION_SERVICE_SHARED_NUMBER = "SharedNumber";

    /**
     * Indicates the companion device carries a different MSISDN as the primary device.
     */
    public static final String COMPANION_SERVICE_DIFFERENT_NUMBER = "DiffNumber";

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({
        COMPANION_SERVICE_SHARED_NUMBER,
        COMPANION_SERVICE_DIFFERENT_NUMBER
    })
    public @interface CompanionService {}

    /**
     * Returns the ODSA operation. Used by HTTP parameter {@code operation}.
     */
    public abstract String operation();

    /**
     * Returns the detailed type of the ODSA operation. Used by HTTP parameter
     * {@code operation_type}.
     */
    public abstract int operationType();

    /**
     * Returns the comma separated list of operation targets used with temporary token from
     * AcquireTemporaryToken operation. Used by HTTP parameter {@code operation_targets}.
     */
    public abstract ImmutableList<String> operationTargets();

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
     * Returns the service type of the companion device, e.g. if the MSISDN is same as the primary
     * device. Used by HTTP parameter {@code companion_terminal_service}.
     */
    public abstract String companionTerminalService();

    /**
     * Returns the ICCID of the companion device. Used by HTTP parameter
     * {@code companion_terminal_iccid}.
     */
    public abstract String companionTerminalIccid();

    /**
     * Returns the EID of the companion device. Used by HTTP parameter
     * {@code companion_terminal_eid}.
     */
    public abstract String companionTerminalEid();

    /**
     * Returns the ICCID of the primary device eSIM. Used by HTTP parameter {@code terminal_iccid}.
     */
    public abstract String terminalIccid();

    /**
     * Returns the eUICC identifier (EID) of the primary device eSIM. Used by HTTP parameter
     * {@code terminal_eid}.
     */
    public abstract String terminalEid();

    /**
     * Returns the unique identifier of the primary device eSIM, like the IMEI associated with the
     * eSIM. Used by HTTP parameter {@code target_terminal_id}.
     */
    public abstract String targetTerminalId();

    /**
     * Returns the ICCID primary device eSIM. Used by HTTP parameter {@code target_terminal_iccid}.
     */
    public abstract String targetTerminalIccid();

    /**
     * Returns the eUICC identifier (EID) of the primary device eSIM. Used by HTTP parameter
     * {@code target_terminal_eid}.
     */
    public abstract String targetTerminalEid();


    /**
     * Returns the unique identifier of the old device eSIM, like the IMEI associated with the
     * eSIM. Used by HTTP parameter {@code old_terminal_id}.
     */
    public abstract String oldTerminalId();

    /**
     * Returns the ICCID of old device eSIM. Used by HTTP parameter {@code old_terminal_iccid}.
     */
    public abstract String oldTerminalIccid();

    /**
     * Returns a new {@link Builder} object.
     */
    public static Builder builder() {
        return new AutoValue_OdsaOperation.Builder()
            .setOperation("")
            .setOperationType(OPERATION_TYPE_NOT_SET)
            .setOperationTargets(ImmutableList.of())
            .setCompanionTerminalId("")
            .setCompanionTerminalVendor("")
            .setCompanionTerminalModel("")
            .setCompanionTerminalSoftwareVersion("")
            .setCompanionTerminalFriendlyName("")
            .setCompanionTerminalService("")
            .setCompanionTerminalIccid("")
            .setCompanionTerminalEid("")
            .setTerminalIccid("")
            .setTerminalEid("")
            .setTargetTerminalId("")
            .setTargetTerminalIccid("")
            .setTargetTerminalEid("")
            .setOldTerminalId("")
            .setOldTerminalIccid("");
    }

    /**
     * Builder.
     *
     * <p>For ODSA, the rule of which parameters are required varies or each
     * operation/operation_type. The Javadoc below gives high-level description, but please refer to
     * GSMA spec TS.43 section 6.2 for details.
     */
    @AutoValue.Builder
    public abstract static class Builder {
        /**
         * Sets the eSIM ODSA operation. Used by HTTP parameter {@code operation}.
         *
         * @param operation ODSA operation.
         *
         * @return The builder.
         *
         * @see #OPERATION_CHECK_ELIGIBILITY
         * @see #OPERATION_MANAGE_SUBSCRIPTION
         * @see #OPERATION_MANAGE_SERVICE
         * @see #OPERATION_ACQUIRE_CONFIGURATION
         * @see #OPERATION_ACQUIRE_TEMPORARY_TOKEN
         * @see #OPERATION_GET_PHONE_NUMBER
         * @see #OPERATION_ACQUIRE_PLAN
         */
        @NonNull
        public abstract Builder setOperation(@NonNull @Operation String operation);

        /**
         * Sets the detailed type of the eSIM ODSA operation. Used by HTTP parameter
         * "operation_type" if set.
         *
         * <p>Required by some operation.
         *
         * @see #OPERATION_TYPE_SUBSCRIBE
         * @see #OPERATION_TYPE_UNSUBSCRIBE
         * @see #OPERATION_TYPE_CHANGE_SUBSCRIPTION
         * @see #OPERATION_TYPE_TRANSFER_SUBSCRIPTION
         * @see #OPERATION_TYPE_UPDATE_SUBSCRIPTION
         * @see #OPERATION_TYPE_ACTIVATE_SERVICE
         * @see #OPERATION_TYPE_DEACTIVATE_SERVICE
         */
        @NonNull
        public abstract Builder setOperationType(@OperationType int operationType);

        /**
         * Sets the operation targets to be used with temporary token from AcquireTemporaryToken
         * operation. Used by HTTP parameter {@code operation_targets} if set.
         */
        @NonNull
        public abstract Builder setOperationTargets(
                @NonNull @Operation ImmutableList<String> operationTargets);

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
         * {@code companion_terminal_vendor} if set.
         *
         * Used by companion device ODSA operation.
         *
         * @param companionTerminalVendor The OEM of the companion device.
         *
         * @return The builder.
         */
        @NonNull
        public abstract Builder setCompanionTerminalVendor(@NonNull String companionTerminalVendor);

        /**
         * Sets the model of the companion device. Used by HTTP parameter
         * {@code companion_terminal_model} if set.
         *
         * Used by companion device ODSA operation.
         *
         * @param companionTerminalModel The model of the companion device.
         *
         * @return The builder.
         */
        @NonNull
        public abstract Builder setCompanionTerminalModel(@NonNull String companionTerminalModel);

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
         * @param companionTerminalFriendlyName The user-friendly version of the companion device.
         *
         * @return The builder.
         */
        @NonNull
        public abstract Builder setCompanionTerminalFriendlyName(
                @NonNull String companionTerminalFriendlyName);

        /**
         * Sets the service type of the companion device, e.g. if the MSISDN is same as the primary
         * device. Used by HTTP parameter {@code companion_terminal_service} if set.
         *
         * Used by companion device ODSA operation.
         *
         * @see #COMPANION_SERVICE_SHARED_NUMBER
         * @see #COMPANION_SERVICE_DIFFERENT_NUMBER
         *
         * @param companionTerminalService The service type of the companion device.
         *
         * @return The builder.
         */
        @NonNull
        public abstract Builder setCompanionTerminalService(
                @NonNull @CompanionService String companionTerminalService);

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
        public abstract Builder setCompanionTerminalIccid(@NonNull String companionTerminalIccid);

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
         * Sets the unique identifier of the primary device eSIM in case of multiple SIM, like the
         * IMEI associated with the eSIM. Used by HTTP parameter {@code target_terminal_id} if set.
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
         * Sets the eUICC identifier (EID) of the primary device eSIM in case of multiple SIM. Used
         * by HTTP parameter {@code target_terminal_eid} if set.
         *
         * Used by primary device ODSA operation.
         *
         * @param terminalEid The eUICC identifier (EID) of the primary device eSIM in case of
         * multiple SIM.
         *
         * @return The builder.
         */
        @NonNull
        public abstract Builder setTargetTerminalEid(@NonNull String terminalEid);

        /**
         * Sets the unique identifier of the old device eSIM, like the IMEI associated with the
         * eSIM. Used by HTTP parameter {@code old_terminal_id} if set.
         *
         * Used by primary device ODSA operation.
         *
         * @param oldTerminalId The unique identifier of the old device eSIM.
         *
         * @return The builder.
         */
        @NonNull
        public abstract Builder setOldTerminalId(@NonNull String oldTerminalId);

        /**
         * Sets the ICCID old device eSIM. Used by HTTP parameter {@code old_terminal_iccid} if set.
         *
         * Used by primary device ODSA operation.
         *
         * @param oldTerminalIccid The ICCID old device eSIM.
         *
         * @return The builder.
         */
        @NonNull
        public abstract Builder setOldTerminalIccid(@NonNull String oldTerminalIccid);

        /**
         * @return The {@link OdsaOperation} object.
         */
        @NonNull
        public abstract OdsaOperation build();
    }
}
