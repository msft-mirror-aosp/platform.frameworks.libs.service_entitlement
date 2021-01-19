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

import static com.google.common.truth.Truth.assertThat;

import static org.testng.Assert.fail;

import static java.nio.charset.StandardCharsets.UTF_8;

import androidx.test.runner.AndroidJUnit4;

import com.android.libraries.entitlement.ServiceEntitlementException;
import com.android.libraries.entitlement.http.HttpConstants.ContentType;
import com.android.libraries.entitlement.http.HttpConstants.RequestMethod;
import com.android.libraries.entitlement.testing.FakeURLStreamHandler;
import com.android.libraries.entitlement.testing.FakeURLStreamHandler.FakeResponse;

import com.google.common.collect.ImmutableMap;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

@RunWith(AndroidJUnit4.class)
public class HttpClientTest {
    private static final String TEST_URL = "https://test.url";
    private static final String TEST_RESPONSE_BODY = "TEST_RESPONSE_BODY";
    private static final String CONTENT_TYPE_STRING_JSON = "application/json";
    private static final String RETRY_AFTER = "RETRY_AFTER";

    private static FakeURLStreamHandler sFakeURLStreamHandler;

    private final HttpClient mHttpClient = new HttpClient();

    @BeforeClass
    public static void setupURLStreamHandlerFactory() {
        sFakeURLStreamHandler = new FakeURLStreamHandler();
        URL.setURLStreamHandlerFactory(sFakeURLStreamHandler);
    }

    @Test
    public void request_contentTypeXml_returnsXmlBody() throws Exception {
        FakeResponse responseContent =
                FakeResponse.builder()
                        .setResponseCode(HttpURLConnection.HTTP_OK)
                        .setResponseLocation(null)
                        .setResponseBody(TEST_RESPONSE_BODY.getBytes(UTF_8))
                        .setContentType(CONTENT_TYPE_STRING_JSON)
                        .build();
        HttpRequest request =
                HttpRequest.builder().setUrl(TEST_URL).setRequestMethod(RequestMethod.GET).build();
        Map<String, FakeResponse> response = ImmutableMap.of(TEST_URL, responseContent);
        sFakeURLStreamHandler.stubResponse(response);

        HttpResponse httpResponse = mHttpClient.request(request);

        assertThat(httpResponse.contentType()).isEqualTo(ContentType.JSON);
        assertThat(httpResponse.body()).isEqualTo(TEST_RESPONSE_BODY);
        assertThat(httpResponse.responseCode()).isEqualTo(HttpURLConnection.HTTP_OK);
    }

    @Test
    public void request_httpGetResponseBadRequest_throwsException() {
        FakeResponse responseContent =
                FakeResponse.builder()
                        .setResponseCode(HttpURLConnection.HTTP_BAD_REQUEST)
                        .setResponseLocation(null)
                        .setResponseBody(TEST_RESPONSE_BODY.getBytes(UTF_8))
                        .setContentType(CONTENT_TYPE_STRING_JSON)
                        .setRetryAfter(RETRY_AFTER)
                        .build();
        HttpRequest request =
                HttpRequest.builder().setUrl(TEST_URL).setRequestMethod(RequestMethod.GET).build();
        Map<String, FakeResponse> response = ImmutableMap.of(TEST_URL, responseContent);
        sFakeURLStreamHandler.stubResponse(response);

        try {
            mHttpClient.request(request);
            fail();
        } catch (ServiceEntitlementException exception) {
            assertThat(exception.getErrorCode()).isEqualTo(
                    ServiceEntitlementException.ERROR_HTTP_STATUS_NOT_SUCCESS);
            assertThat(exception.getHttpStatus()).isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST);
            assertThat(exception.getMessage()).isEqualTo("Invalid connection response");
            assertThat(exception.getRetryAfter()).isEqualTo(RETRY_AFTER);
        }
    }
}