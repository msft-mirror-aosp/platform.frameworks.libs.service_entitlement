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

package com.android.libraries.entitlement;

import static com.google.common.truth.Truth.assertThat;

import static org.mockito.Mockito.when;

import android.content.Context;

import androidx.test.runner.AndroidJUnit4;

import com.android.libraries.entitlement.eapaka.EapAkaApi;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

@RunWith(AndroidJUnit4.class)
public class ServiceEntitlementTest {
    private static final String QUERY_APP_VOLTE_RESULT = "QUERY_APP_VOLTE_RESULT";
    private static final String QUERY_APP_VOWIFI_RESULT = "QUERY_APP_VOWIFI_RESULT";
    private static final String TEST_URL = "https://test.url";

    @Rule public final MockitoRule rule = MockitoJUnit.rule();
    @Mock Context mockContext;
    CarrierConfig carrierConfig;
    @Mock EapAkaApi mockEapAkaApi;

    private ServiceEntitlement serviceEntitlement;

    @Before
    public void setUp() {
        carrierConfig = CarrierConfig.builder().setServerUrl(TEST_URL).build();
        serviceEntitlement = new ServiceEntitlement(mockContext, carrierConfig, mockEapAkaApi);
    }

    @Test
    public void queryEntitlementStatus_appVolte_returnResult() throws Exception {
        ServiceEntitlementRequest request = ServiceEntitlementRequest.builder().build();
        when(mockEapAkaApi.queryEntitlementStatus(ServiceEntitlement.APP_VOLTE, TEST_URL, request))
            .thenReturn(QUERY_APP_VOLTE_RESULT);

        assertThat(serviceEntitlement.queryEntitlementStatus(ServiceEntitlement.APP_VOLTE, request))
            .isEqualTo(QUERY_APP_VOLTE_RESULT);
    }

    @Test
    public void queryEntitlementStatus_appVowifi_returnResult() throws Exception {
        ServiceEntitlementRequest request = ServiceEntitlementRequest.builder().build();
        when(mockEapAkaApi.queryEntitlementStatus(ServiceEntitlement.APP_VOWIFI, TEST_URL, request))
            .thenReturn(QUERY_APP_VOWIFI_RESULT);

        assertThat(
            serviceEntitlement.queryEntitlementStatus(ServiceEntitlement.APP_VOWIFI, request))
                .isEqualTo(QUERY_APP_VOWIFI_RESULT);
    }
}
