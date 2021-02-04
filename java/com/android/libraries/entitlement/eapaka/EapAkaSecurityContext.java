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

import static com.android.libraries.entitlement.ServiceEntitlementException.ERROR_ICC_AUTHENTICATION_NOT_AVAILABLE;

import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.android.libraries.entitlement.ServiceEntitlementException;

/**
 * Provides format to handle request/response SIM Authentication with GSM/3G security context.
 *
 * <p>Reference ETSI TS 131 102, Section 7.1.2.1 GSM/3G security context.
 */
class EapAkaSecurityContext {
    private static final String TAG = "ServiceEntitlement";

    private static final byte RESPONSE_TAG_SUCCESS = (byte) 0xDB;

    private boolean mValid;

    /* Authentication result from SIM */
    private byte[] mRes;
    /* Cipher Key */
    private byte[] mCk;
    /* Integrity Key */
    private byte[] mIk;

    private EapAkaSecurityContext() {
    }

    /**
     * Provide {@link EapAkaSecurityContext} from response data.
     */
    public static EapAkaSecurityContext from(String response)
            throws ServiceEntitlementException {
        EapAkaSecurityContext securityContext = new EapAkaSecurityContext();
        securityContext.parseResponseData(response);
        if (!securityContext.isValid()) {
            throw new ServiceEntitlementException(
                ERROR_ICC_AUTHENTICATION_NOT_AVAILABLE,
                "Invalid SIM EAP-AKA authentication response!");
        }
        return securityContext;
    }

    /**
     * Parses SIM EAP-AKA Authentication responded data.
     */
    private void parseResponseData(String response) {
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
            mRes = parseTag(index, data);
            if (mRes == null) {
                Log.d(TAG, "Invalid data! can't parse RES!");
                return;
            }
            // Parse CK
            index += mRes.length + 1; // move to CK length byte
            mCk = parseTag(index, data);
            if (mCk == null) {
                Log.d(TAG, "Invalid data! can't parse CK!");
                return;
            }
            // Parse IK
            index += mCk.length + 1; // move to IK length byte
            mIk = parseTag(index, data);
            if (mIk == null) {
                Log.d(TAG, "Invalid data! can't parse IK!");
                return;
            }

            mValid = true;
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

    /**
     * Returns {@code valid}.
     */
    boolean isValid() {
        return mValid;
    }

    /**
     * Returns {@code res}.
     */
    public byte[] getRes() {
        return mRes;
    }

    /**
     * Returns {@code ck}.
     */
    public byte[] getCk() {
        return mCk;
    }

    /**
     * Returns {@code ik}.
     */
    public byte[] getIk() {
        return mIk;
    }
}
