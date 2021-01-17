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

import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.android.libraries.entitlement.ServiceEntitlementException;

/**
 * Provide format to handle request/response SIM Authentication with GSM/3G security context.
 *
 * <p>Reference ETSI TS 131 102, Section 7.1.2.1 GSM/3G security context.
 */
class EapAkaSecurityContext {
    private static final String TAG = "ServiceEntitlement";

    private static final byte RESPONSE_TAG_SUCCESS = (byte) 0xDB;

    private boolean valid;

    /* Authentication result from SIM */
    private byte[] res;
    /* Cipher Key */
    private byte[] ck;
    /* Integrity Key */
    private byte[] ik;

    private EapAkaSecurityContext() {
    }

    /** Provide {@link EapAkaSecurityContext} from response data. */
    public static EapAkaSecurityContext from(String response)
            throws ServiceEntitlementException {
        EapAkaSecurityContext securityContext = new EapAkaSecurityContext();
        securityContext.parseResponseData(response);
        if (!securityContext.isValid()) {
            throw new ServiceEntitlementException("Invalid SIM EAP-AKA authentication response!");
        }
        return securityContext;
    }

    /**
     * Parses SIM EAP-AKA Authentication responsed data and returns valid {@link
     * EapAkaSecurityContext}
     * for successful data; otherwise, returns invalid.
     */
    void parseResponseData(String response) {
        if (TextUtils.isEmpty(response)) {
            Log.d(TAG, "parseResponseData but input empty data!");
            return;
        }

        try {
            byte[] data = Base64.decode(response, Base64.DEFAULT);
            Log.d(TAG, "decoded data length=" + data.length);

            if (data.length <= 2) {
                return;
            }

            int index = 0;

            // check tag
            if (data[index] != RESPONSE_TAG_SUCCESS) {
                Log.d(TAG, "Not successful data, tag=" + data[index]);
                return;
            }

            // Parse RES
            index++; // move to RES length byte
            res = parseTag(index, data);
            if (res == null) {
                Log.d(TAG, "Invalid data! can't parse RES!");
                return;
            }
            // Parse CK
            index += res.length + 1; // move to CK length byte
            ck = parseTag(index, data);
            if (ck == null) {
                Log.d(TAG, "Invalid data! can't parse CK!");
                return;
            }
            // Parse IK
            index += ck.length + 1; // move to IK length byte
            ik = parseTag(index, data);
            if (ik == null) {
                Log.d(TAG, "Invalid data! can't parse IK!");
                return;
            }

            valid = true;
        } catch (IllegalArgumentException illegalArgumentException) {
            Log.e(TAG, "Invalid base-64 content");
        }
    }


    private byte[] parseTag(int index, byte[] src) {
        // index at the length byte
        if (index >= src.length) {
            Log.d(TAG, "No length byte!");
            return null;
        }
        int length = src[index] & 0xff;
        if (index + length >= src.length) {
            Log.d(TAG, "Invalid data length!");
            return null;
        }
        index++; // move to first byte of tag value
        byte[] dest = new byte[length];
        System.arraycopy(src, index, dest, 0, length);

        return dest;
    }

    /** Returns {@code valid}. */
    boolean isValid() {
        return valid;
    }

    /** Returns {@code res}. */
    public byte[] getRes() {
        return res;
    }

    /** Returns {@code ck}. */
    public byte[] getCk() {
        return ck;
    }

    /** Returns {@code ik}. */
    public byte[] getIk() {
        return ik;
    }
}
