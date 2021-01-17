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

import android.content.Context;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.android.libraries.entitlement.ServiceEntitlementException;
import com.android.libraries.entitlement.eapaka.utils.BytesConverter;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import androidx.annotation.Nullable;

/** Generate the response of EAP-AKA token challenge for initial AUTN. */
class EapAkaResponse {
    private static final String TAG = "ServiceEntitlement";

    private static final int EAP_AKA_HEADER_LENGTH = 8;
    private static final byte CODE_REQUEST = 0x01;
    private static final byte CODE_RESPONSE = 0x02;
    private static final byte TYPE_EAP_AKA = 0x17;
    private static final byte SUBTYPE_AKA_CHALLENGE = 0x01;
    private static final byte ATTRIBUTE_RAND = 0x01;
    private static final byte ATTRIBUTE_AUTN = 0x02;
    private static final byte ATTRIBUTE_RES = 0x03;
    private static final byte ATTRIBUTE_MAC = 0x0B;
    private static final String ALGORITHM_HMAC_SHA1 = "HmacSHA1";
    private static final int RAND_LENGTH = 20;
    private static final int AUTN_LENGTH = 20;
    private static final int SHA1_OUTPUT_LENGTH = 20;

    /** RAND length 16. */
    private static final byte RAND_LEN = 0x10;
    /** AUTN length 16. */
    private static final byte AUTN_LEN = 0x10;

    /* 1 for Request, 2 for Response*/
    private byte code = -1;
    /* The identifier of Response must same as Request */
    private byte identifier = -1;
    /* The total length of full EAP-AKA message, include code, identifier, ... */
    private int length = -1;
    /* In EAP-AKA, the Type field is set to 23 */
    private byte type = -1;
    /* SubType for AKA-Challenge should be 1 */
    private byte subType = -1;
    /* The value of AT_AUTN, network authentication token */
    private byte[] autn;
    /* The value of AT_RAND, RAND random number*/
    private byte[] rand;

    private boolean valid;

    public EapAkaResponse(String eapAkaChallenge) {
        try {
            parseEapAkaChallengeRequest(eapAkaChallenge);
        } catch (Exception e) {
            Log.e(TAG, "parseEapAkaChallengeRequest Exception:", e);
            valid = false;
        }
    }

    /** Refer to RFC 4187 Section 8.1 Message Format/RFC 3748 Session 4 EAP Packet Format. */
    private void parseEapAkaChallengeRequest(String request) {
        if (TextUtils.isEmpty(request)) {
            return;
        }

        try {
            byte[] data = Base64.decode(request, Base64.DEFAULT);
            if (parseEapAkaHeader(data) && parseRandAndAutn(data)) {
                valid = true;
            } else {
                Log.d(TAG, "Invalid data!");
            }
        } catch (IllegalArgumentException illegalArgumentException) {
            Log.e(TAG, "Invalid base-64 content");
        }
    }

    /**
     * Parse EAP-AKA header, 8 bytes include 2 reserved bytes.
     *
     * @param data raw bytes of request data.
     * @return {@code true} if success to parse the header of request data.
     */
    private boolean parseEapAkaHeader(byte[] data) {
        if (data.length <= EAP_AKA_HEADER_LENGTH) {
            return false;
        }
        code = data[0];
        identifier = data[1];
        length = ((data[2] & 0xff) << 8) | (data[3] & 0xff);
        type = data[4];
        subType = data[5];

        // valid header
        if (code != CODE_REQUEST
                || length != data.length
                || type != TYPE_EAP_AKA
                || subType != SUBTYPE_AKA_CHALLENGE) {
            Log.d(
                    TAG,
                    "Invalid EAP-AKA Header, code="
                            + code
                            + ", length="
                            + length
                            + ", real length="
                            + data.length
                            + ", type="
                            + type
                            + ", subType="
                            + subType);
            return false;
        }

        return true;
    }

    /**
     * Refer to RFC 4187 section 10.6 AT_RAND/RFC 4187 section 10.7 AT_AUTN.
     *
     * @param data raw bytes of request data.
     * @return {@code true} if success to parse the RAND and AUTN data.
     */
    private boolean parseRandAndAutn(byte[] data) {
        int index = EAP_AKA_HEADER_LENGTH;
        while (index < data.length) {
            int remainsLength = data.length - index;
            if (remainsLength <= 2) {
                Log.d(TAG, "Error! remainsLength = " + remainsLength);
                return false;
            }

            byte attributeType = data[index];

            // the length of this attribute in multiples of 4 bytes, include attribute type and
            // length
            int length = (data[index + 1] & 0xff) * 4;
            if (length > remainsLength) {
                Log.d(TAG,
                        "Length Error! length is " + length + " but only remains " + remainsLength);
                return false;
            }

            // see RFC 4187 section 11 for attribute type
            if (attributeType == ATTRIBUTE_RAND) {
                if (length != RAND_LENGTH) {
                    Log.d(TAG, "AT_RAND length is " + length);
                    return false;
                }
                rand = new byte[16];
                System.arraycopy(data, index + 4, rand, 0, 16);
            } else if (attributeType == ATTRIBUTE_AUTN) {
                if (length != AUTN_LENGTH) {
                    Log.d(TAG, "AT_AUTN length is " + length);
                    return false;
                }
                autn = new byte[16];
                System.arraycopy(data, index + 4, autn, 0, 16);
            }

            index += length;
        } // while

        // check has AT_RAND and AT_AUTH
        if (rand == null || autn == null) {
            Log.d(TAG, "Invalid Type Datas!");
            return false;
        }

        return true;
    }

    /**
     * Returns EAP-AKA challenge response message which generated with SIM EAP-AKA authentication
     * with
     * network provided EAP-AKA challenge request message.
     */
    public String getEapAkaChallengeResponse(Context context, int simSubscriptionId)
            throws ServiceEntitlementException {
        if (!valid) {
            throw new ServiceEntitlementException("EAP-AKA Challenge message not valid!");
        }

        TelephonyManager telephonyManager =
                context.getSystemService(TelephonyManager.class).createForSubscriptionId(
                        simSubscriptionId);

        // process EAP-AKA authentication with SIM
        String response =
                telephonyManager.getIccAuthentication(
                        TelephonyManager.APPTYPE_USIM,
                        TelephonyManager.AUTHTYPE_EAP_AKA,
                        getSimAuthChallengeData());

        EapAkaSecurityContext securityContext = EapAkaSecurityContext.from(response);
        // RFC 4187, section 7.  Key Generation
        // generate master key
        MasterKey mk =
                MasterKey.create(
                        EapAkaApi.getImsiEap(telephonyManager.getSubscriberId(),
                                telephonyManager.getSimOperator()),
                        securityContext.getIk(),
                        securityContext.getCk());
        // K_aut is the key used to calculate MAC
        if (mk.getAut() == null) {
            throw new ServiceEntitlementException("Can't generate K_Aut!");
        }

        // generate EAP-AKA Challenge Response message
        byte[] challengeResponse =
                generateEapAkaChallengeResponse(securityContext.getRes(), mk.getAut());
        if (challengeResponse == null) {
            throw new ServiceEntitlementException(
                    "Failed to generate EAP-AKA Challenge Response data!");
        }

        return Base64.encodeToString(challengeResponse, Base64.NO_WRAP).trim();
    }

    /** Returns Base64 encoded GSM/3G security context for SIM Authentication request. */
    @Nullable
    private String getSimAuthChallengeData() {
        if (!valid) {
            return null;
        }

        byte[] challengeData = new byte[RAND_LEN + AUTN_LEN + 2];
        challengeData[0] = RAND_LEN;
        System.arraycopy(rand, 0, challengeData, 1, RAND_LEN);
        challengeData[RAND_LEN + 1] = AUTN_LEN;
        System.arraycopy(autn, 0, challengeData, RAND_LEN + 2, AUTN_LEN);

        return Base64.encodeToString(challengeData, Base64.NO_WRAP).trim();
    }

    /** Returns EAP-AKA Challenge response message byte array data or null if failed to generate. */
    @Nullable
    public byte[] generateEapAkaChallengeResponse(@Nullable byte[] res, byte[] aut) {
        if (res == null || aut == null) {
            return null;
        }

        byte[] message = createEapAkaChallengeResponse(res);

        // use K_aut as key to calculate mac
        byte[] mac = calculateMac(aut, message);
        if (mac == null) {
            return null;
        }

        // fill MAC value to the message
        // The value start index is 8 + AT_RES (4 + res.length) + header of AT_MAC (4)
        int index = 8 + 4 + res.length + 4;
        System.arraycopy(mac, 0, message, index, mac.length);

        return message;
    }

    // AT_MAC/AT_RES are must included in response message
    //
    // Reference RFC 4187 Section 8.1 Message Format
    //           RFC 4187 Section 9.4 EAP-Response/AKA-Challenge
    //           RFC 3748, Section 4.1.  Request and Response
    private byte[] createEapAkaChallengeResponse(byte[] res) {
        // size = 8 (header) + resHeader (4) + res.length + AT_MAC (20 bytes)
        byte[] message = new byte[32 + res.length];

        // set up header
        message[0] = CODE_RESPONSE;
        // Identifier need to same with request
        message[1] = identifier;
        // length include entire EAP-AKA message
        byte[] lengthBytes = BytesConverter.convertIntegerTo4Bytes(message.length);
        message[2] = lengthBytes[2];
        message[3] = lengthBytes[3];
        message[4] = TYPE_EAP_AKA;
        message[5] = SUBTYPE_AKA_CHALLENGE;
        // Reserved 2 bytes
        message[6] = 0x00;
        message[7] = 0x00;

        int index = 8;

        // set up AT_RES, RFC 4187, Section 10.8 AT_RES
        message[index++] = ATTRIBUTE_RES;
        // The length of the AT_RES attribute must be a multiple of 4 bytes which identifies the
        // exact length of the RES in bits. To pad 4 onto the length to ensure the reserved buffer
        // size large enough after convert to byte count.
        int resLength = (res.length + 4) / 4;
        message[index++] = (byte) (resLength & 0xff);
        // The value field of this attribute begins with the 2-byte RES Length, which identifies
        // the exact length of the RES in bits.
        byte[] resBitLength = BytesConverter.convertIntegerTo4Bytes(res.length * 8);
        message[index++] = resBitLength[2];
        message[index++] = resBitLength[3];
        System.arraycopy(res, 0, message, index, res.length);
        index += res.length;

        // set up AT_MAC, RFC 4187, 10.15 AT_MAC
        message[index++] = ATTRIBUTE_MAC;
        // fixed length, 5*4 = 20
        message[index++] = 0x05;
        // With two bytes reserved
        message[index++] = 0x00;
        message[index++] = 0x00;

        // The MAC is calculated over the whole EAP packet and concatenated with optional
        // message-specific data, with the exception that the value field of the
        // MAC attribute is set to zero when calculating the MAC.
        for (int i = 0; i < 16; i++) {
            message[index++] = 0x00;
        }

        return message;
    }

    // See RFC 4187, 10.15 AT_MAC, snippet as below, the key must be k_aut
    //
    // The MAC algorithm is HMAC-SHA1-128 [RFC2104] keyed hash value.  (The
    // HMAC-SHA1-128 value is obtained from the 20-byte HMAC-SHA1 value by
    // truncating the output to 16 bytes.  Hence, the length of the MAC is
    // 16 bytes.)  The derivation of the authentication key (K_aut) used in
    // the calculation of the MAC is specified in Section 7.
    @Nullable
    private byte[] calculateMac(byte[] key, byte[] message) {
        try {
            Mac mac = Mac.getInstance(ALGORITHM_HMAC_SHA1);
            SecretKeySpec secret = new SecretKeySpec(key, ALGORITHM_HMAC_SHA1);
            mac.init(secret);
            byte[] output = mac.doFinal(message);

            if (output == null || output.length != SHA1_OUTPUT_LENGTH) {
                Log.e(TAG, "Invalid result! length should be 20, but " + output.length);
                return null;
            }

            byte[] macValue = new byte[16];
            System.arraycopy(output, 0, macValue, 0, 16);
            return macValue;
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            Log.e(TAG, "calculateMac failed!", e);
        }

        return null;
    }
}
