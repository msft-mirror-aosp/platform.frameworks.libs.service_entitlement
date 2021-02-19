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

package com.android.libraries.entitlement.http;

import android.net.Network;
import android.util.ArrayMap;

import androidx.annotation.Nullable;

import com.android.libraries.entitlement.CarrierConfig;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableMap;

import org.json.JSONObject;

import java.util.Map;

/** The parameters of an http request. */
@AutoValue
public abstract class HttpRequest {
    /** The URL. */
    public abstract String url();

    /** The HTTP request method, like "GET" or "POST". */
    public abstract String requestMethod();

    /** For "POST" request method, the body of the request in JSON format. */
    public abstract JSONObject postData();

    /** For "GET" request method, the parameters to be encoded into the URL. */
    public abstract ImmutableMap<String, String> requestValues();

    /** HTTP header fields. */
    public abstract ImmutableMap<String, String> requestProperties();

    /** The client side timeout, in seconds. See {@link Builder#setTimeoutInSec}. */
    public abstract int timeoutInSec();

    /** The network used for this HTTP connection. See {@link Builder#setNetwork}. */
    @Nullable
    public abstract Network network();

    /** Builder of {@link HttpRequest}. */
    @AutoValue.Builder
    public abstract static class Builder {
        private final Map<String, String> values = new ArrayMap<>();
        private final Map<String, String> properties = new ArrayMap<>();

        public abstract HttpRequest build();

        /** Sets the URL. */
        public abstract Builder setUrl(String url);

        /**
         * Sets the HTTP request method, like "GET" or "POST".
         *
         * @see HttpConstants.RequestMethod
         */
        public abstract Builder setRequestMethod(String requestMethod);

        /** For "POST" request method, sets the body of the request in JSON format. */
        public abstract Builder setPostData(JSONObject postData);

        abstract Builder setRequestValues(ImmutableMap<String, String> value);

        abstract Builder setRequestProperties(ImmutableMap<String, String> properties);

        /** For "GET" request method, adds a parameter to be encoded into the URL. */
        public Builder addRequestValues(String key, String value) {
            values.put(key, value);
            return this.setRequestValues(ImmutableMap.copyOf(values));
        }

        /** Adds an HTTP header field. */
        public Builder addRequestProperty(String key, String value) {
            properties.put(key, value);
            return this.setRequestProperties(ImmutableMap.copyOf(properties));
        }

        /**
         * Sets the client side timeout for HTTP connection. Default to
         * {@link com.android.libraries.entitlement.CarrierConfig#DEFAULT_TIMEOUT_IN_SEC}.
         *
         * <p>This timeout is used by both {@link java.net.URLConnection#setConnectTimeout} and
         * {@link java.net.URLConnection#setReadTimeout}.
         */
        public abstract Builder setTimeoutInSec(int timeoutInSec);

        /**
         * Sets the network used for this HTTP connection. If not set, the device default network
         * is used.
         */
        public abstract Builder setNetwork(@Nullable Network network);
    }

    public static Builder builder() {
        return new AutoValue_HttpRequest.Builder()
                .setUrl("")
                .setRequestMethod("")
                .setPostData(new JSONObject())
                .setRequestValues(ImmutableMap.of())
                .setRequestProperties(ImmutableMap.of())
                .setTimeoutInSec(CarrierConfig.DEFAULT_TIMEOUT_IN_SEC);
    }
}
