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
import androidx.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * HTTP request parameters specific to on device service activation (ODSA). See GSMA spec TS.43
 * section 6.2.
 */
public interface OdsaOperation {
    /** ODSA operation unknown. For initialization only. */
    String OPERATION_UNKNOWN = "";

    /** ODSA operation: CheckEligibility. */
    String OPERATION_CHECK_ELIGIBILITY = "CheckEligibility";

    /** ODSA operation: ManageSubscription. */
    String OPERATION_MANAGE_SUBSCRIPTION = "ManageSubscription";

    /** ODSA operation: ManageService. */
    String OPERATION_MANAGE_SERVICE = "ManageService";

    /** ODSA operation: AcquireConfiguration. */
    String OPERATION_ACQUIRE_CONFIGURATION = "AcquireConfiguration";

    /** ODSA operation: AcquireTemporaryToken. */
    String OPERATION_ACQUIRE_TEMPORARY_TOKEN = "AcquireTemporaryToken";

    /** ODSA operation: GetPhoneNumber */
    String OPERATION_GET_PHONE_NUMBER = "GetPhoneNumber";

    /** ODSA operation: AcquirePlan */
    String OPERATION_ACQUIRE_PLAN = "AcquirePlan";

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
    int SERVICE_STATUS_UNKNOWN = -1;

    /** eSIM device’s service is activated. */
    int SERVICE_STATUS_ACTIVATED = 1;

    /** eSIM device’s service is being activated. */
    int SERVICE_STATUS_ACTIVATING = 2;

    /** eSIM device’s service is not activated. */
    int SERVICE_STATUS_DEACTIVATED = 3;

    /** eSIM device’s service is not activated and the associated ICCID should not be reused. */
    int SERVICE_STATUS_DEACTIVATED_NO_REUSE = 4;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({
            SERVICE_STATUS_UNKNOWN,
            SERVICE_STATUS_ACTIVATED,
            SERVICE_STATUS_ACTIVATING,
            SERVICE_STATUS_DEACTIVATED,
            SERVICE_STATUS_DEACTIVATED_NO_REUSE
    })
    @interface ServiceStatus {
    }

    /** Indicates that operation_type is not set. */
    int OPERATION_TYPE_NOT_SET = -1;

    /** To activate a subscription, used by {@link #OPERATION_MANAGE_SUBSCRIPTION}. */
    int OPERATION_TYPE_SUBSCRIBE = 0;

    /** To cancel a subscription, used by {@link #OPERATION_MANAGE_SUBSCRIPTION}. */
    int OPERATION_TYPE_UNSUBSCRIBE = 1;

    /** To manage an existing subscription, for {@link #OPERATION_MANAGE_SUBSCRIPTION}. */
    int OPERATION_TYPE_CHANGE_SUBSCRIPTION = 2;

    /**
     * To transfer a subscription from an existing device, used by {@link
     * #OPERATION_MANAGE_SUBSCRIPTION}.
     */
    int OPERATION_TYPE_TRANSFER_SUBSCRIPTION = 3;

    /**
     * To inform the network of a subscription update, used by
     * {@link #OPERATION_MANAGE_SUBSCRIPTION}.
     */
    int OPERATION_TYPE_UPDATE_SUBSCRIPTION = 4;

    /** To activate a service, used by {@link #OPERATION_MANAGE_SERVICE}. */
    int OPERATION_TYPE_ACTIVATE_SERVICE = 10;

    /** To deactivate a service, used by {@link #OPERATION_MANAGE_SERVICE}. */
    int OPERATION_TYPE_DEACTIVATE_SERVICE = 11;

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
    @interface OperationType {
    }

    /** Operation result unknown. */
    int OPERATION_RESULT_UNKNOWN = -1;

    /** Operation was a success. */
    int OPERATION_RESULT_SUCCESS = 1;

    /** There was a general error during processing. */
    int OPERATION_RESULT_ERROR_GENERAL = 100;

    /** An invalid operation value was provided in request. */
    int OPERATION_RESULT_ERROR_INVALID_OPERATION = 101;

    /** An invalid parameter name or value was provided in request. */
    int OPERATION_RESULT_ERROR_INVALID_PARAMETER = 102;

    /**
     * The optional operation is not supported by the carrier. Device should continue with the flow.
     * This error only applies to optional operations (for example ManageService).
     */
    int OPERATION_RESULT_WARNING_NOT_SUPPORTED_OPERATION = 103;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({
            OPERATION_RESULT_UNKNOWN,
            OPERATION_RESULT_SUCCESS,
            OPERATION_RESULT_ERROR_GENERAL,
            OPERATION_RESULT_ERROR_INVALID_OPERATION,
            OPERATION_RESULT_ERROR_INVALID_PARAMETER,
            OPERATION_RESULT_WARNING_NOT_SUPPORTED_OPERATION
    })
    @interface OperationResult {
    }

    /** Companion service unknown. For initialization only. */
    String COMPANION_SERVICE_UNKNOWN = "";

    /** Indicates the companion device carries the same MSISDN as the primary device. */
    String COMPANION_SERVICE_SHARED_NUMBER = "SharedNumber";

    /** Indicates the companion device carries a different MSISDN as the primary device. */
    String COMPANION_SERVICE_DIFFERENT_NUMBER = "DiffNumber";

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({
            COMPANION_SERVICE_UNKNOWN,
            COMPANION_SERVICE_SHARED_NUMBER,
            COMPANION_SERVICE_DIFFERENT_NUMBER
    })
    @interface CompanionService {
    }
}
