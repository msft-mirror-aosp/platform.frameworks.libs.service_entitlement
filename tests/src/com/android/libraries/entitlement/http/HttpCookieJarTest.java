/*
 * Copyright (C) 2024 The Android Open Source Project
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

import androidx.test.runner.AndroidJUnit4;

import com.google.common.collect.ImmutableList;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class HttpCookieJarTest {
    @Test
    public void parseSetCookieHeaders_and_toCookieHeaders() {
        ImmutableList<String> setCookieHeaders = ImmutableList.of(
                "DEGCID=WnA8oHIgPbrYMbe+fDai/yhrNTY=; Path=/; Domain=.mobile.com",
                "SID=dNbG%3D; Secure; HttpOnly"
        );
        ImmutableList<String> cookieHeaders = ImmutableList.of(
                "DEGCID=WnA8oHIgPbrYMbe+fDai/yhrNTY=",
                "SID=dNbG%3D"
        );

        HttpCookieJar cookieJar = HttpCookieJar.parseSetCookieHeaders(setCookieHeaders);

        assertThat(cookieJar.toCookieHeaders()).isEqualTo(cookieHeaders);
    }
}
