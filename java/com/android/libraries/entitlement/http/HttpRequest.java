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

import android.util.ArrayMap;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableMap;

import org.json.JSONObject;

import java.util.Map;

/**
 * The parameters of the http request.
 */
@AutoValue
public abstract class HttpRequest {
    public abstract String url();

    public abstract String requestMethod();

    public abstract JSONObject postData();

    public abstract ImmutableMap<String, String> requestValues();

    public abstract ImmutableMap<String, String> requestProperties();

    /**
     * Builder of {@link HttpRequest}.
     */
    @AutoValue.Builder
    public abstract static class Builder {
        private final Map<String, String> values = new ArrayMap<>();
        private final Map<String, String> properties = new ArrayMap<>();

        public abstract HttpRequest build();

        public abstract Builder setUrl(String url);

        public abstract Builder setRequestMethod(String requestMethod);

        public abstract Builder setPostData(JSONObject postData);

        abstract Builder setRequestValues(ImmutableMap<String, String> value);

        abstract Builder setRequestProperties(ImmutableMap<String, String> properties);

        public Builder addRequestValues(String key, String value) {
            values.put(key, value);
            return this.setRequestValues(ImmutableMap.copyOf(values));
        }

        public Builder addRequestProperty(String key, String value) {
            properties.put(key, value);
            return this.setRequestProperties(ImmutableMap.copyOf(properties));
        }
    }

    public static Builder builder() {
        return new AutoValue_HttpRequest.Builder()
                .setUrl("")
                .setRequestMethod("")
                .setPostData(new JSONObject())
                .setRequestValues(ImmutableMap.of())
                .setRequestProperties(ImmutableMap.of());
    }
}
