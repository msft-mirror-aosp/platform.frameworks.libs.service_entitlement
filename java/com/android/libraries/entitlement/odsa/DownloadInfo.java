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

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;

import java.net.URL;
import java.util.List;

/**
 * Download information described in GSMA Service Entitlement Configuration section 6.5.3 table
 * 38.
 */
@AutoValue
public abstract class DownloadInfo {
    /**
     * The ICCID of the eSIM profile to download from SM-DP+. This is not {@code null} when
     * {@link #profileSmdpAddresses()} is used to trigger the profile download.
     */
    @Nullable
    public abstract String profileIccid();

    /**
     * Address(es) of SM-DP+ to obtain eSIM profile. It is an empty list if
     * {@link #profileActivationCode()} is not {@code null}.
     */
    @NonNull
    public abstract ImmutableList<URL> profileSmdpAddresses();

    /**
     * Activation code as defined in SGP.22 to permit the download of an eSIM profile from an
     * SM-DP+. It is {@code null} if {@link #profileSmdpAddresses()} is not empty.
     */
    @Nullable
    public abstract String profileActivationCode();


    /**
     * @return Builder of {@link DownloadInfo}.
     */
    @NonNull
    public static Builder builder() {
        return new AutoValue_DownloadInfo.Builder();
    }

    /**
     * Builder of DownloadInfo.
     */
    @AutoValue.Builder
    public abstract static class Builder {
        /**
         * Set the ICCID of the download profile.
         *
         * @param iccid The ICCID of the eSIM profile to download from SM-DP+.
         *
         * @return The builder.
         */
        @NonNull
        public abstract Builder setProfileIccid(@NonNull String iccid);

        /**
         * Set the activation code.
         *
         * @param activationCode Activation code as defined in SGP.22 to permit the download of
         * an eSIM profile from an SM-DP+.
         *
         * @return The builder.
         */
        @NonNull
        public abstract Builder setProfileActivationCode(@NonNull String activationCode);

        /**
         * Set address(es) of SM-DP+ to obtain eSIM profile.
         *
         * @param smdpAddress Address(es) of SM-DP+ to obtain eSIM profile.
         *
         * @return The builder.
         */
        @NonNull
        public abstract Builder setProfileSmdpAddresses(@NonNull List<URL> smdpAddress);

        /**
         * Build the DownloadInfo object.
         */
        @NonNull
        public abstract DownloadInfo build();
    }
}
