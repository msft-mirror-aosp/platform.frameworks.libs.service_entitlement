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

import static com.android.libraries.entitlement.http.HttpConstants.RequestMethod.POST;

import static com.google.common.base.Strings.nullToEmpty;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.concurrent.TimeUnit.SECONDS;

import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.WorkerThread;

import com.android.libraries.entitlement.ServiceEntitlementException;
import com.android.libraries.entitlement.http.HttpConstants.ContentType;
import com.android.libraries.entitlement.utils.StreamUtils;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;

/**
 * Implement the HTTP request method according to TS.43 specification.
 */
public class HttpClient {
    private static final String TAG = "ServiceEntitlement";
    private static final boolean DEBUG = false; // STOPSHIP if true

    private static final int SOCKET_TIMEOUT_VALUE = (int) SECONDS.toMillis(30);
    private static final int CONNECT_TIMEOUT_VALUE = (int) SECONDS.toMillis(30);

    private HttpURLConnection mConnection;

    @WorkerThread
    // TODO(b/177544547): Add debug messages
    public HttpResponse request(HttpRequest request) throws ServiceEntitlementException {
        try {
            logd("HttpClient.request url: " + request.url());
            createConnection(request);
            if (mConnection == null) {
                logd("HttpClient.request connection is null");
                throw new ServiceEntitlementException("No connection");
            }
            logd("HttpClient.request headers (partial): " + mConnection.getRequestProperties());
            if (POST.equals(request.requestMethod())) {
                try (OutputStream out = new DataOutputStream(mConnection.getOutputStream())) {
                    out.write(request.postData().toString().getBytes(UTF_8));
                    logd("HttpClient.request post data: " + request.postData());
                }
            }
            mConnection.connect(); // This is to trigger SocketTimeoutException early
            HttpResponse response = getHttpResponse(mConnection);
            Log.d(TAG, "HttpClient.response : " + response);
            return response;
        } catch (IOException e) {
            InputStream errorStream = mConnection.getErrorStream();
            Log.e(
                    TAG,
                    "HttpClient.request() error: " + StreamUtils.inputStreamToStringSafe(
                            errorStream));
            throw new ServiceEntitlementException("request failed! exception: " + e.getMessage());
        } finally {
            closeConnection();
        }
    }

    private void createConnection(HttpRequest request) throws ServiceEntitlementException {
        try {
            URL url = new URL(request.url());
            mConnection = (HttpURLConnection) url.openConnection();

            // add HTTP headers
            for (Map.Entry<String, String> entry : request.requestProperties().entrySet()) {
                mConnection.addRequestProperty(entry.getKey(), entry.getValue());
            }

            // set parameters
            mConnection.setRequestMethod(request.requestMethod());
            mConnection.setConnectTimeout(CONNECT_TIMEOUT_VALUE);
            mConnection.setReadTimeout(SOCKET_TIMEOUT_VALUE);
            if (POST.equals(request.requestMethod())) {
                mConnection.setDoOutput(true);
            }
        } catch (IOException e) {
            Log.e(TAG, "IOException: " + e.getMessage());
            throw new ServiceEntitlementException("Configure connection failed!" + e.getMessage());
        }
    }

    private void closeConnection() {
        if (mConnection != null) {
            mConnection.disconnect();
            mConnection = null;
        }
    }

    private static HttpResponse getHttpResponse(HttpURLConnection connection)
            throws ServiceEntitlementException {
        try {
            int responseCode = connection.getResponseCode();
            logd("HttpClient.response headers: " + connection.getHeaderFields());
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new ServiceEntitlementException(
                        ServiceEntitlementException.ERROR_HTTP_STATUS_NOT_SUCCESS, responseCode,
                        null,
                        "Invalid connection response", null);
            }
            String responseBody = readResponse(connection);
            logd("HttpClient.response body: " + responseBody);
            return HttpResponse.builder()
                    .setContentType(getContentType(connection))
                    .setBody(responseBody)
                    .setResponseCode(responseCode)
                    .setResponseMessage(nullToEmpty(connection.getResponseMessage()))
                    .build();
        } catch (IOException e) {
            throw new ServiceEntitlementException(
                    ServiceEntitlementException.ERROR_HTTP_STATUS_NOT_SUCCESS, 0, null,
                    "Read response failed!", e);
        }
    }

    private static String readResponse(URLConnection connection) throws IOException {
        try (InputStream in = connection.getInputStream()) {
            return StreamUtils.inputStreamToStringSafe(in);
        }
    }

    private static int getContentType(URLConnection connection) {
        String contentType = connection.getHeaderField(ContentType.NAME);
        if (TextUtils.isEmpty(contentType)) {
            return ContentType.UNKNOWN;
        }

        if (contentType.contains("xml")) {
            return ContentType.XML;
        } else if ("text/vnd.wap.connectivity".equals(contentType)) {
            // Workaround that a server vendor uses this type for XML
            return ContentType.XML;
        } else if (contentType.contains("json")) {
            return ContentType.JSON;
        }
        return ContentType.UNKNOWN;
    }

    private static void logd(String message) {
        if (DEBUG) {
            Log.d(TAG, message);
        }
    }
}
