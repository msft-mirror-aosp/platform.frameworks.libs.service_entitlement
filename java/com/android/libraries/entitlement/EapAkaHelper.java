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

import android.content.Context;
import android.telephony.TelephonyManager;

import androidx.annotation.Nullable;

import com.android.libraries.entitlement.eapaka.EapAkaApi;
import com.android.libraries.entitlement.eapaka.EapAkaResponse;

/**
 * Some utility methods used in EAP-AKA authentication in service entitlement, and could be
 * helpful to other apps.
 */
public class EapAkaHelper {
    private final Context mContext;
    private final int mSimSubscriptionId;

    EapAkaHelper(Context context, int simSubscriptionId) {
        mContext = context;
        mSimSubscriptionId = simSubscriptionId;
    }

    /**
     * Factory method.
     *
     * @param context           context of application
     * @param simSubscriptionId the subscroption ID of the carrier's SIM on device. This indicates
     *                          which SIM to retrieve IMEI/IMSI from and perform EAP-AKA
     *                          authentication with. See
     *                          {@link android.telephony.SubscriptionManager}
     *                          for how to get the subscroption ID.
     */
    public static EapAkaHelper getInstance(Context context, int simSubscriptionId) {
        return new EapAkaHelper(context, simSubscriptionId);
    }

    /**
     * Returns the root NAI for EAP-AKA authentication as per 3GPP TS 23.003 19.3.2, or
     * {@code null} if failed. The result will be in the form:
     *
     * <p>{@code 0<IMSI>@nai.epc.mnc<MNC>.mcc<MCC>.3gppnetwork.org}
     */
    @Nullable
    public String getEapAkaRootNai() {
        TelephonyManager telephonyManager =
                mContext.getSystemService(TelephonyManager.class)
                        .createForSubscriptionId(mSimSubscriptionId);
        return EapAkaApi.getImsiEap(
                telephonyManager.getSimOperator(), telephonyManager.getSubscriberId());
    }

    /**
     * Returns the EAP-AKA challenge response to the given EAP-AKA {@code challenge}, or
     * {@code null} if failed.
     *
     * <p>Both the challange and response are base-64 encoded EAP-AKA message: refer to
     * RFC 4187 Section 8.1 Message Format/RFC 3748 Session 4 EAP Packet Format.
     */
    @Nullable
    public String getEapAkaChallengeResponse(String challenge) {
        try {
            return new EapAkaResponse(challenge)
                    .getEapAkaChallengeResponse(mContext, mSimSubscriptionId);
        } catch (ServiceEntitlementException e) {
            return null;
        }
    }
}
