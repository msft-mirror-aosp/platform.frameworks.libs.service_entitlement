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

package com.android.libraries.entitlement;

import androidx.annotation.NonNull;
import androidx.annotation.StringDef;

import com.android.libraries.entitlement.odsa.OdsaOperation;
import com.android.libraries.entitlement.odsa.OdsaOperation.CompanionService;
import com.android.libraries.entitlement.odsa.OdsaOperation.OperationType;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * HTTP request parameters specific to on device service activation (ODSA). See GSMA spec TS.43
 * section 6.2.
 */
@AutoValue
public abstract class EsimOdsaOperation {
    /** ODSA operation unknown. For initialization only. */
    public static final String OPERATION_UNKNOWN = "";

    /** ODSA operation: CheckEligibility. */
    public static final String OPERATION_CHECK_ELIGIBILITY =
            OdsaOperation.OPERATION_CHECK_ELIGIBILITY;

    /** ODSA operation: ManageSubscription. */
    public static final String OPERATION_MANAGE_SUBSCRIPTION =
            OdsaOperation.OPERATION_MANAGE_SUBSCRIPTION;

    /** ODSA operation: ManageService. */
    public static final String OPERATION_MANAGE_SERVICE =
            OdsaOperation.OPERATION_MANAGE_SERVICE;

    /** ODSA operation: AcquireConfiguration. */
    public static final String OPERATION_ACQUIRE_CONFIGURATION =
            OdsaOperation.OPERATION_ACQUIRE_CONFIGURATION;

    /** ODSA operation: AcquireTemporaryToken. */
    public static final String OPERATION_ACQUIRE_TEMPORARY_TOKEN =
            OdsaOperation.OPERATION_ACQUIRE_TEMPORARY_TOKEN;

    /** ODSA operation: GetPhoneNumber */
    public static final String OPERATION_GET_PHONE_NUMBER =
            OdsaOperation.OPERATION_GET_PHONE_NUMBER;

    /** ODSA operation: AcquirePlan */
    public static final String OPERATION_ACQUIRE_PLAN = OdsaOperation.OPERATION_ACQUIRE_PLAN;

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({
            OPERATION_UNKNOWN,
            OPERATION_CHECK_ELIGIBILITY,
            OPERATION_MANAGE_SUBSCRIPTION,
            OPERATION_MANAGE_SERVICE,
            OPERATION_ACQUIRE_CONFIGURATION,
            OPERATION_ACQUIRE_PLAN,
            OPERATION_ACQUIRE_TEMPORARY_TOKEN,
            OPERATION_GET_PHONE_NUMBER
    })
    public @interface Operation {
    }

    /** eSIM device’s service is unknown. */
    public static final int SERVICE_STATUS_UNKNOWN = OdsaOperation.SERVICE_STATUS_UNKNOWN;

    /** eSIM device’s service is activated. */
    public static final int SERVICE_STATUS_ACTIVATED = OdsaOperation.SERVICE_STATUS_ACTIVATED;

    /** eSIM device’s service is being activated. */
    public static final int SERVICE_STATUS_ACTIVATING = OdsaOperation.SERVICE_STATUS_ACTIVATING;

    /** eSIM device’s service is not activated. */
    public static final int SERVICE_STATUS_DEACTIVATED = OdsaOperation.SERVICE_STATUS_DEACTIVATED;

    /** eSIM device’s service is not activated and the associated ICCID should not be reused. */
    public static final int SERVICE_STATUS_DEACTIVATED_NO_REUSE =
            OdsaOperation.SERVICE_STATUS_DEACTIVATED_NO_REUSE;

    /** Indicates that operation_type is not set. */
    public static final int OPERATION_TYPE_NOT_SET = OdsaOperation.OPERATION_TYPE_NOT_SET;

    /** To activate a subscription, used by {@link #OPERATION_MANAGE_SUBSCRIPTION}. */
    public static final int OPERATION_TYPE_SUBSCRIBE = OdsaOperation.OPERATION_TYPE_SUBSCRIBE;

    /** To cancel a subscription, used by {@link #OPERATION_MANAGE_SUBSCRIPTION}. */
    public static final int OPERATION_TYPE_UNSUBSCRIBE = OdsaOperation.OPERATION_TYPE_UNSUBSCRIBE;

    /** To manage an existing subscription, for {@link #OPERATION_MANAGE_SUBSCRIPTION}. */
    public static final int OPERATION_TYPE_CHANGE_SUBSCRIPTION =
            OdsaOperation.OPERATION_TYPE_CHANGE_SUBSCRIPTION;

    /**
     * To transfer a subscription from an existing device, used by {@link
     * #OPERATION_MANAGE_SUBSCRIPTION}.
     */
    public static final int OPERATION_TYPE_TRANSFER_SUBSCRIPTION =
            OdsaOperation.OPERATION_TYPE_TRANSFER_SUBSCRIPTION;

    /**
     * To inform the network of a subscription update, used by
     * {@link #OPERATION_MANAGE_SUBSCRIPTION}.
     */
    public static final int OPERATION_TYPE_UPDATE_SUBSCRIPTION =
            OdsaOperation.OPERATION_TYPE_UPDATE_SUBSCRIPTION;

    /** To activate a service, used by {@link #OPERATION_MANAGE_SERVICE}. */
    public static final int OPERATION_TYPE_ACTIVATE_SERVICE =
            OdsaOperation.OPERATION_TYPE_ACTIVATE_SERVICE;

    /** To deactivate a service, used by {@link #OPERATION_MANAGE_SERVICE}. */
    public static final int OPERATION_TYPE_DEACTIVATE_SERVICE =
            OdsaOperation.OPERATION_TYPE_DEACTIVATE_SERVICE;

    /** Operation result unknown. */
    public static final int OPERATION_RESULT_UNKNOWN = OdsaOperation.OPERATION_RESULT_UNKNOWN;

    /** Operation was a success. */
    public static final int OPERATION_RESULT_SUCCESS = OdsaOperation.OPERATION_RESULT_SUCCESS;

    /** There was a general error during processing. */
    public static final int OPERATION_RESULT_ERROR_GENERAL =
            OdsaOperation.OPERATION_RESULT_ERROR_GENERAL;

    /** An invalid operation value was provided in request. */
    public static final int OPERATION_RESULT_ERROR_INVALID_OPERATION =
            OdsaOperation.OPERATION_RESULT_ERROR_INVALID_OPERATION;

    /** An invalid parameter name or value was provided in request. */
    public static final int OPERATION_RESULT_ERROR_INVALID_PARAMETER =
            OdsaOperation.OPERATION_RESULT_ERROR_INVALID_PARAMETER;

    /**
     * The optional operation is not supported by the carrier. Device should continue with the flow.
     * This error only applies to optional operations (for example ManageService).
     */
    public static final int OPERATION_RESULT_WARNING_NOT_SUPPORTED_OPERATION =
            OdsaOperation.OPERATION_RESULT_WARNING_NOT_SUPPORTED_OPERATION;

    /** Companion service unknown. For initialization only. */
    public static final String COMPANION_SERVICE_UNKNOWN = OdsaOperation.COMPANION_SERVICE_UNKNOWN;

    /** Indicates the companion device carries the same MSISDN as the primary device. */
    public static final String COMPANION_SERVICE_SHARED_NUMBER =
            OdsaOperation.COMPANION_SERVICE_SHARED_NUMBER;

    /** Indicates the companion device carries a different MSISDN as the primary device. */
    public static final String COMPANION_SERVICE_DIFFERENT_NUMBER =
            OdsaOperation.COMPANION_SERVICE_DIFFERENT_NUMBER;


    /** Returns the ODSA operation. Used by HTTP parameter {@code operation}. */
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
     * {@code
     * companion_terminal_id}.
     */
    public abstract String companionTerminalId();

    /**
     * Returns the OEM of the companion device. Used by HTTP parameter {@code
     * companion_terminal_vendor}.
     */
    public abstract String companionTerminalVendor();

    /**
     * Returns the model of the companion device. Used by HTTP parameter {@code
     * companion_terminal_model}.
     */
    public abstract String companionTerminalModel();

    /**
     * Returns the software version of the companion device. Used by HTTP parameter {@code
     * companion_terminal_sw_version}.
     */
    public abstract String companionTerminalSoftwareVersion();

    /**
     * Returns the user-friendly version of the companion device. Used by HTTP parameter {@code
     * companion_terminal_friendly_name}.
     */
    public abstract String companionTerminalFriendlyName();

    /**
     * Returns the service type of the companion device, e.g. if the MSISDN is same as the primary
     * device. Used by HTTP parameter {@code companion_terminal_service}.
     */
    public abstract String companionTerminalService();

    /**
     * Returns the ICCID of the companion device. Used by HTTP parameter {@code
     * companion_terminal_iccid}.
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
     * Returns the eUICC identifier (EID) of the primary device eSIM. Used by HTTP parameter {@code
     * terminal_eid}.
     */
    public abstract String terminalEid();

    /**
     * Returns the unique identifier of the primary device eSIM, like the IMEI associated with the
     * eSIM. Used by HTTP parameter {@code target_terminal_id}.
     */
    public abstract String targetTerminalId();

    /**
     * Returns the unique identifiers of the primary device eSIM if more than one, like the IMEIs on
     * dual-SIM devices. Used by HTTP parameter {@code target_terminal_imeis}.
     *
     * <p>This is a non-standard params required by some carriers.
     */
    @NonNull
    public abstract ImmutableList<String> targetTerminalIds();

    /**
     * Returns the ICCID primary device eSIM. Used by HTTP parameter {@code target_terminal_iccid}.
     */
    public abstract String targetTerminalIccid();

    /**
     * Returns the eUICC identifier (EID) of the primary device eSIM. Used by HTTP parameter {@code
     * target_terminal_eid}.
     */
    public abstract String targetTerminalEid();

    /**
     * Returns the serial number of primary device. Used by HTTP parameter
     * {@code target_terminal_sn}.
     *
     * <p>This is a non-standard params required by some carriers.
     */
    @NonNull
    public abstract String targetTerminalSerialNumber();

    /**
     * Returns the model of primary device. Used by HTTP parameter {@code target_terminal_model}.
     *
     * <p>This is a non-standard params required by some carriers.
     */
    @NonNull
    public abstract String targetTerminalModel();

    /**
     * Returns the unique identifier of the old device eSIM, like the IMEI associated with the eSIM.
     * Used by HTTP parameter {@code old_terminal_id}.
     */
    public abstract String oldTerminalId();

    /** Returns the ICCID of old device eSIM. Used by HTTP parameter {@code old_terminal_iccid}. */
    public abstract String oldTerminalIccid();

    /** Returns a new {@link Builder} object. */
    public static Builder builder() {
        return new AutoValue_EsimOdsaOperation.Builder()
                .setOperation(OPERATION_UNKNOWN)
                .setOperationType(OPERATION_TYPE_NOT_SET)
                .setOperationTargets(ImmutableList.of())
                .setCompanionTerminalId("")
                .setCompanionTerminalVendor("")
                .setCompanionTerminalModel("")
                .setCompanionTerminalSoftwareVersion("")
                .setCompanionTerminalFriendlyName("")
                .setCompanionTerminalService(COMPANION_SERVICE_UNKNOWN)
                .setCompanionTerminalIccid("")
                .setCompanionTerminalEid("")
                .setTerminalIccid("")
                .setTerminalEid("")
                .setTargetTerminalId("")
                .setTargetTerminalIds(ImmutableList.of())
                .setTargetTerminalIccid("")
                .setTargetTerminalEid("")
                .setTargetTerminalSerialNumber("")
                .setTargetTerminalModel("")
                .setOldTerminalId("")
                .setOldTerminalIccid("");
    }

    /**
     * Builder.
     *
     * <p>For ODSA, the rule of which parameters are required varies or each
     * operation/operation_type.
     * The Javadoc below gives high-level description, but please refer to GSMA spec TS.43 section
     * 6.2
     * for details.
     */
    @AutoValue.Builder
    public abstract static class Builder {
        /**
         * Sets the eSIM ODSA operation. Used by HTTP parameter {@code operation}.
         *
         * @param operation ODSA operation.
         * @return The builder.
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
         * "operation_type" if
         * set.
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
         * {@code
         * companion_terminal_id} if set.
         *
         * <p>Used by companion device ODSA operation.
         *
         * @param companionTerminalId The unique identifier of the companion device.
         * @return The builder.
         */
        @NonNull
        public abstract Builder setCompanionTerminalId(@NonNull String companionTerminalId);

        /**
         * Sets the OEM of the companion device. Used by HTTP parameter {@code
         * companion_terminal_vendor} if set.
         *
         * <p>Used by companion device ODSA operation.
         *
         * @param companionTerminalVendor The OEM of the companion device.
         * @return The builder.
         */
        @NonNull
        public abstract Builder setCompanionTerminalVendor(@NonNull String companionTerminalVendor);

        /**
         * Sets the model of the companion device. Used by HTTP parameter {@code
         * companion_terminal_model} if set.
         *
         * <p>Used by companion device ODSA operation.
         *
         * @param companionTerminalModel The model of the companion device.
         * @return The builder.
         */
        @NonNull
        public abstract Builder setCompanionTerminalModel(@NonNull String companionTerminalModel);

        /**
         * Sets the software version of the companion device. Used by HTTP parameter {@code
         * companion_terminal_sw_version} if set.
         *
         * <p>Used by companion device ODSA operation.
         *
         * @param companionTerminalSoftwareVersion The software version of the companion device.
         * @return The builder.
         */
        @NonNull
        public abstract Builder setCompanionTerminalSoftwareVersion(
                @NonNull String companionTerminalSoftwareVersion);

        /**
         * Sets the user-friendly version of the companion device. Used by HTTP parameter {@code
         * companion_terminal_friendly_name} if set.
         *
         * <p>Used by companion device ODSA operation.
         *
         * @param companionTerminalFriendlyName The user-friendly version of the companion device.
         * @return The builder.
         */
        @NonNull
        public abstract Builder setCompanionTerminalFriendlyName(
                @NonNull String companionTerminalFriendlyName);

        /**
         * Sets the service type of the companion device, e.g. if the MSISDN is same as the primary
         * device. Used by HTTP parameter {@code companion_terminal_service} if set.
         *
         * <p>Used by companion device ODSA operation.
         *
         * @param companionTerminalService The service type of the companion device.
         * @return The builder.
         * @see #COMPANION_SERVICE_SHARED_NUMBER
         * @see #COMPANION_SERVICE_DIFFERENT_NUMBER
         */
        @NonNull
        public abstract Builder setCompanionTerminalService(
                @NonNull @CompanionService String companionTerminalService);

        /**
         * Sets the ICCID of the companion device. Used by HTTP parameter {@code
         * companion_terminal_iccid} if set.
         *
         * <p>Used by companion device ODSA operation.
         *
         * @param companionTerminalIccid The ICCID of the companion device.
         * @return The builder.
         */
        @NonNull
        public abstract Builder setCompanionTerminalIccid(@NonNull String companionTerminalIccid);

        /**
         * Sets the eUICC identifier (EID) of the companion device. Used by HTTP parameter {@code
         * companion_terminal_eid} if set.
         *
         * <p>Used by companion device ODSA operation.
         *
         * @param companionTerminalEid The eUICC identifier (EID) of the companion device.
         * @return The builder.
         */
        @NonNull
        public abstract Builder setCompanionTerminalEid(@NonNull String companionTerminalEid);

        /**
         * Sets the ICCID of the primary device eSIM in case of primary SIM not present. Used by
         * HTTP
         * parameter {@code terminal_eid} if set.
         *
         * <p>Used by primary device ODSA operation.
         *
         * @param terminalIccid The ICCID of the primary device eSIM in case of primary SIM not
         *                      present.
         * @return The builder.
         */
        @NonNull
        public abstract Builder setTerminalIccid(@NonNull String terminalIccid);

        /**
         * Sets the eUICC identifier (EID) of the primary device eSIM in case of primary SIM not
         * present. Used by HTTP parameter {@code terminal_eid} if set.
         *
         * <p>Used by primary device ODSA operation.
         *
         * @param terminalEid The eUICC identifier (EID) of the primary device eSIM in case of
         *                    primary
         *                    SIM not present.
         * @return The builder.
         */
        @NonNull
        public abstract Builder setTerminalEid(@NonNull String terminalEid);

        /**
         * Sets the unique identifier of the primary device eSIM in case of multiple SIM, like the
         * IMEI
         * associated with the eSIM. Used by HTTP parameter {@code target_terminal_id} if set.
         *
         * <p>Used by primary device ODSA operation.
         *
         * @param targetTerminalId The unique identifier of the primary device eSIM in case of
         *                         multiple
         *                         SIM.
         * @return The builder.
         */
        @NonNull
        public abstract Builder setTargetTerminalId(@NonNull String targetTerminalId);

        /**
         * Sets the unique identifiers of the primary device eSIM if more than one, like the IMEIs
         * on
         * dual-SIM devices. Used by HTTP parameter {@code target_terminal_imeis}.
         *
         * <p>This is a non-standard params required by some carriers.
         *
         * @param targetTerminalIds The unique identifiers of the primary device eSIM if more than
         *                          one.
         * @return The builder.
         */
        public abstract Builder setTargetTerminalIds(
                @NonNull ImmutableList<String> targetTerminalIds);

        /**
         * Sets the ICCID primary device eSIM in case of multiple SIM. Used by HTTP parameter {@code
         * target_terminal_iccid} if set.
         *
         * <p>Used by primary device ODSA operation.
         *
         * @param targetTerminalIccid The ICCID primary device eSIM in case of multiple SIM.
         * @return The builder.
         */
        @NonNull
        public abstract Builder setTargetTerminalIccid(@NonNull String targetTerminalIccid);

        /**
         * Sets the eUICC identifier (EID) of the primary device eSIM in case of multiple SIM. Used
         * by
         * HTTP parameter {@code target_terminal_eid} if set.
         *
         * <p>Used by primary device ODSA operation.
         *
         * @param terminalEid The eUICC identifier (EID) of the primary device eSIM in case of
         *                    multiple
         *                    SIM.
         * @return The builder.
         */
        @NonNull
        public abstract Builder setTargetTerminalEid(@NonNull String terminalEid);

        /**
         * Sets the serial number of primary device. Used by HTTP parameter
         * {@code target_terminal_sn}.
         *
         * @param targetTerminalSerialNumber The serial number of primary device.
         *                                   <p>This is a non-standard params required by some
         *                                   carriers.
         * @return The builder.
         */
        @NonNull
        public abstract Builder setTargetTerminalSerialNumber(
                @NonNull String targetTerminalSerialNumber);

        /**
         * Sets the model of primary device. Used by HTTP parameter {@code target_terminal_model}.
         *
         * @param targetTerminalModel The model of primary device.
         *                            <p>This is a non-standard params required by some carriers.
         * @return The builder.
         */
        @NonNull
        public abstract Builder setTargetTerminalModel(@NonNull String targetTerminalModel);

        /**
         * Sets the unique identifier of the old device eSIM, like the IMEI associated with the
         * eSIM.
         * Used by HTTP parameter {@code old_terminal_id} if set.
         *
         * <p>Used by primary device ODSA operation.
         *
         * @param oldTerminalId The unique identifier of the old device eSIM.
         * @return The builder.
         */
        @NonNull
        public abstract Builder setOldTerminalId(@NonNull String oldTerminalId);

        /**
         * Sets the ICCID old device eSIM. Used by HTTP parameter {@code old_terminal_iccid} if set.
         *
         * <p>Used by primary device ODSA operation.
         *
         * @param oldTerminalIccid The ICCID old device eSIM.
         * @return The builder.
         */
        @NonNull
        public abstract Builder setOldTerminalIccid(@NonNull String oldTerminalIccid);

        /** Returns the {@link EsimOdsaOperation} object. */
        @NonNull
        public abstract EsimOdsaOperation build();
    }
}
