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

package com.android.libraries.entitlement.testing;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableMap;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.net.ssl.HttpsURLConnection;

/**
 * Fakes {@linkplain java.net.URLStreamHandler} which is used to set URLStreamHandlerFactory for URL
 * as {@linkplain java.net.URL} is a final class and cannot be mocked using mockito.
 */
public class FakeURLStreamHandler extends URLStreamHandler implements URLStreamHandlerFactory {

    private Map<String, FakeResponse> mResponseMap;

    private static final String ACCESS_TOKEN = "8dGozfI6%2FEaSsE7LaTfJKwdy";
    private static final String LOCATION = "Location";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String RETRY_AFTER = "Retry-After";

    @Override
    public URLConnection openConnection(URL u) {
        FakeHttpsURLConnection connection = new FakeHttpsURLConnection(u);
        return connection;
    }

    @Override
    public URLStreamHandler createURLStreamHandler(String protocol) {
        return this;
    }

    public FakeURLStreamHandler stubResponse(Map<String, FakeResponse> response) {
        this.mResponseMap = response;
        return this;
    }

    /**
     * Fakes {@linkplain java.net.HttpURLConnection} to avoid making any network connection.
     */
    public class FakeHttpsURLConnection extends HttpsURLConnection {

        public ByteArrayOutputStream mByteArrayOutputStream;

        private final String mUrlString;

        protected FakeHttpsURLConnection(URL url) {
            super(url);
            this.mUrlString = url.toString();
        }

        @Override
        public InputStream getInputStream() throws IOException {
            InputStream inputStream = new ByteArrayInputStream(
                    mResponseMap.get(mUrlString).responseBody());
            if (inputStream == null) {
                throw new IOException();
            }
            return inputStream;
        }

        @Override
        public OutputStream getOutputStream() {
            mByteArrayOutputStream = new ByteArrayOutputStream();
            return mByteArrayOutputStream;
        }

        @Override
        public int getResponseCode() {
            return mResponseMap.get(mUrlString).responseCode();
        }

        @Override
        public Map<String, List<String>> getHeaderFields() {
            List<String> locationList = new ArrayList<>();
            locationList.add("access_token=" + ACCESS_TOKEN);
            return ImmutableMap.of("Location", locationList);
        }

        @Override
        public String getHeaderField(String name) {
            switch (name) {
                case LOCATION:
                    return "Location: " + mResponseMap.get(mUrlString).responseLocation();
                case CONTENT_TYPE:
                    return mResponseMap.get(mUrlString).contentType();
                case RETRY_AFTER:
                    return mResponseMap.get(mUrlString).retryAfter();
                default:
                    return null;
            }
        }

        @Override
        public void connect() {
        }

        @Override
        public void disconnect() {
        }

        @Override
        public boolean usingProxy() {
            return false;
        }

        @Override
        public String getCipherSuite() {
            return null;
        }

        @Override
        public Certificate[] getLocalCertificates() {
            return null;
        }

        @Override
        public Certificate[] getServerCertificates() {
            return null;
        }
    }

    @AutoValue
    public abstract static class FakeResponse {
        public abstract int responseCode();

        @Nullable
        public abstract String responseLocation();

        @SuppressWarnings("mutable") // For test only
        public abstract byte[] responseBody();

        public abstract String contentType();

        public abstract String retryAfter();

        public static Builder builder() {
            return new AutoValue_FakeURLStreamHandler_FakeResponse.Builder()
                    .setResponseBody(new byte[]{})
                    .setContentType("")
                    .setResponseCode(0)
                    .setResponseLocation("")
                    .setRetryAfter("");
        }

        @AutoValue.Builder
        public abstract static class Builder {
            public abstract Builder setResponseCode(int value);

            public abstract Builder setResponseLocation(String value);

            public abstract Builder setResponseBody(byte[] value);

            public abstract Builder setContentType(String contentType);

            public abstract Builder setRetryAfter(String retryAfter);

            public abstract FakeResponse build();
        }
    }
}