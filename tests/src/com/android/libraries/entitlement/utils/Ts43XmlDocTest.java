/*
 * Copyright (C) 2025 The Android Open Source Project
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

package com.android.libraries.entitlement.utils;

import static com.google.common.truth.Truth.assertThat;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.common.collect.ImmutableList;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public final class Ts43XmlDocTest {

    private static final String SAMPLE_RESPONSE =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                    + "<wap-provisioningdoc version=\"1.1\">"
                    + "<characteristic type=\"VERS\">"
                    + "<parm name=\"version\" value=\"1\" />"
                    + "<parm name=\"validity\" value=\"1440\" />"
                    + "</characteristic>"
                    + "<characteristic type=\"ACCESS-CONTROL\">"
                    + "<characteristic type=\"DEFAULT\">"
                    + "<parm name=\"app-id\" value=\"ap2009\" />"
                    + "</characteristic>"
                    + "</characteristic>"
                    + "<characteristic type=\"APPLICATION\">"
                    + "<parm name=\"AppID\" value=\"ap2009\" />"
                    + "<parm name=\"OperationResult\" value=\"1\" />"
                    + "<characteristic type=\"PrimaryConfiguration\">"
                    + "<parm name=\"ICCID\" value=\"8971000005500204414\" />"
                    + "<parm name=\"ServiceStatus\" value=\"2\" />"
                    + "<parm name=\"PollingInterval\" value=\"1\" />"
                    + "<characteristic type=\"DownloadInfo\">"
                    + "<parm name=\"ProfileIccid\" value=\"8971000005500204414\" />"
                    + "</characteristic>"
                    + "</characteristic>"
                    + "</characteristic>"
                    + "</wap-provisioningdoc>";

    private static final String SAMPLE_PRIMARY_CONFIGURATIONS_RESPONSE =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                    + "<wap-provisioningdoc version=\"1.1\">"
                    + "<characteristic type=\"VERS\">"
                    + "<parm name=\"version\" value=\"1\" />"
                    + "<parm name=\"validity\" value=\"1440\" />"
                    + "</characteristic>"
                    + "<characteristic type=\"ACCESS-CONTROL\">"
                    + "<characteristic type=\"DEFAULT\">"
                    + "<parm name=\"app-id\" value=\"ap2009\" />"
                    + "</characteristic>"
                    + "</characteristic>"
                    + "<characteristic type=\"APPLICATION\">"
                    + "<parm name=\"AppID\" value=\"ap2009\" />"
                    + "<parm name=\"OperationResult\" value=\"1\" />"
                    + "<characteristic type=\"PrimaryConfigurations\">"
                    + "<characteristic type=\"PrimaryConfiguration\">"
                    + "<parm name=\"ICCID\" value=\"8971000005500204414\" />"
                    + "<parm name=\"ServiceStatus\" value=\"2\" />"
                    + "<parm name=\"PollingInterval\" value=\"1\" />"
                    + "<characteristic type=\"DownloadInfo\">"
                    + "<parm name=\"ProfileIccid\" value=\"8971000005500204414\" />"
                    + "</characteristic>"
                    + "</characteristic>"
                    + "</characteristic>"
                    + "</characteristic>"
                    + "</wap-provisioningdoc>";

    private static final String INVALID_RESPONSE =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                    + "<wap-provisioningdoc version=\"1.1\">"
                    + "<characteristic aaa=\"PrimaryConfiguration\">"
                    + "<parm name=\"ICCID\" value=\"8971000005500204414\" />"
                    + "</characteristic>"
                    + "<characteristic>"
                    + "<parm name=\"PollingInterval\" value=\"1\" />"
                    + "</characteristic>"
                    + "</wap-provisioningdoc>";

    @Test
    public void testSampleResponse() throws Exception {
        Ts43XmlDoc xmlDoc = new Ts43XmlDoc(SAMPLE_RESPONSE);

        boolean applicationExists =
                xmlDoc.contains(ImmutableList.of(Ts43XmlDoc.CharacteristicType.APPLICATION));
        boolean primaryConfigurationExists =
                xmlDoc.contains(
                        ImmutableList.of(
                                Ts43XmlDoc.CharacteristicType.APPLICATION,
                                Ts43XmlDoc.CharacteristicType.PRIMARY_CONFIGURATION));
        boolean downloadInfoExists =
                xmlDoc.contains(
                        ImmutableList.of(
                                Ts43XmlDoc.CharacteristicType.APPLICATION,
                                Ts43XmlDoc.CharacteristicType.PRIMARY_CONFIGURATION,
                                Ts43XmlDoc.CharacteristicType.DOWNLOAD_INFO));
        String appId =
                xmlDoc.get(
                        ImmutableList.of(Ts43XmlDoc.CharacteristicType.APPLICATION),
                        Ts43XmlDoc.Parm.APP_ID);
        String opResult =
                xmlDoc.get(
                        ImmutableList.of(Ts43XmlDoc.CharacteristicType.APPLICATION),
                        Ts43XmlDoc.Parm.OPERATION_RESULT);
        String iccId =
                xmlDoc.get(
                        ImmutableList.of(
                                Ts43XmlDoc.CharacteristicType.APPLICATION,
                                Ts43XmlDoc.CharacteristicType.PRIMARY_CONFIGURATION),
                        Ts43XmlDoc.Parm.ICCID);
        String serviceStatus =
                xmlDoc.get(
                        ImmutableList.of(
                                Ts43XmlDoc.CharacteristicType.APPLICATION,
                                Ts43XmlDoc.CharacteristicType.PRIMARY_CONFIGURATION),
                        Ts43XmlDoc.Parm.SERVICE_STATUS);
        String pollingInterval =
                xmlDoc.get(
                        ImmutableList.of(
                                Ts43XmlDoc.CharacteristicType.APPLICATION,
                                Ts43XmlDoc.CharacteristicType.PRIMARY_CONFIGURATION),
                        Ts43XmlDoc.Parm.POLLING_INTERVAL);
        // misspelled parameter values
        String polligInterval =
                xmlDoc.get(
                        ImmutableList.of(
                                Ts43XmlDoc.CharacteristicType.APPLICATION,
                                Ts43XmlDoc.CharacteristicType.PRIMARY_CONFIGURATION),
                        "polligInterval");
        String apId =
                xmlDoc.get(ImmutableList.of(Ts43XmlDoc.CharacteristicType.APPLICATION), "apId");

        assertThat(applicationExists).isTrue();
        assertThat(primaryConfigurationExists).isTrue();
        assertThat(downloadInfoExists).isTrue();
        assertThat(appId).isEqualTo("ap2009");
        assertThat(opResult).isEqualTo("1");
        assertThat(iccId).isEqualTo("8971000005500204414");
        assertThat(serviceStatus).isEqualTo("2");
        assertThat(pollingInterval).isEqualTo("1");
        assertThat(polligInterval).isNull();
        assertThat(apId).isNull();
    }

    @Test
    public void testPrimaryConfigurations() throws Exception {
        Ts43XmlDoc xmlDoc = new Ts43XmlDoc(SAMPLE_PRIMARY_CONFIGURATIONS_RESPONSE);

        boolean applicationExists =
                xmlDoc.contains(ImmutableList.of(Ts43XmlDoc.CharacteristicType.APPLICATION));
        boolean primaryConfigurationExists =
                xmlDoc.contains(
                        ImmutableList.of(
                                Ts43XmlDoc.CharacteristicType.APPLICATION,
                                Ts43XmlDoc.CharacteristicType.PRIMARY_CONFIGURATION));
        boolean primaryConfigurationsExists =
                xmlDoc.contains(
                        ImmutableList.of(
                                Ts43XmlDoc.CharacteristicType.APPLICATION,
                                Ts43XmlDoc.CharacteristicType.PRIMARY_CONFIGURATIONS,
                                Ts43XmlDoc.CharacteristicType.PRIMARY_CONFIGURATION));
        boolean downloadInfoExists =
                xmlDoc.contains(
                        ImmutableList.of(
                                Ts43XmlDoc.CharacteristicType.APPLICATION,
                                Ts43XmlDoc.CharacteristicType.PRIMARY_CONFIGURATION,
                                Ts43XmlDoc.CharacteristicType.DOWNLOAD_INFO));
        String appId =
                xmlDoc.get(
                        ImmutableList.of(Ts43XmlDoc.CharacteristicType.APPLICATION),
                        Ts43XmlDoc.Parm.APP_ID);
        String opResult =
                xmlDoc.get(
                        ImmutableList.of(Ts43XmlDoc.CharacteristicType.APPLICATION),
                        Ts43XmlDoc.Parm.OPERATION_RESULT);
        String iccId =
                xmlDoc.get(
                        ImmutableList.of(
                                Ts43XmlDoc.CharacteristicType.APPLICATION,
                                Ts43XmlDoc.CharacteristicType.PRIMARY_CONFIGURATION),
                        Ts43XmlDoc.Parm.ICCID);
        String serviceStatus =
                xmlDoc.get(
                        ImmutableList.of(
                                Ts43XmlDoc.CharacteristicType.APPLICATION,
                                Ts43XmlDoc.CharacteristicType.PRIMARY_CONFIGURATION),
                        Ts43XmlDoc.Parm.SERVICE_STATUS);
        String pollingInterval =
                xmlDoc.get(
                        ImmutableList.of(
                                Ts43XmlDoc.CharacteristicType.APPLICATION,
                                Ts43XmlDoc.CharacteristicType.PRIMARY_CONFIGURATION),
                        Ts43XmlDoc.Parm.POLLING_INTERVAL);

        assertThat(applicationExists).isTrue();
        assertThat(primaryConfigurationExists).isTrue();
        assertThat(primaryConfigurationsExists).isTrue();
        assertThat(downloadInfoExists).isTrue();
        assertThat(appId).isEqualTo("ap2009");
        assertThat(opResult).isEqualTo("1");
        assertThat(iccId).isEqualTo("8971000005500204414");
        assertThat(serviceStatus).isEqualTo("2");
        assertThat(pollingInterval).isEqualTo("1");
    }

    @Test
    public void testInvalidResponse() throws Exception {
        Ts43XmlDoc xmlDoc = new Ts43XmlDoc(INVALID_RESPONSE);

        String iccid =
                xmlDoc.get(
                        ImmutableList.of(Ts43XmlDoc.CharacteristicType.PRIMARY_CONFIGURATION),
                        Ts43XmlDoc.Parm.ICCID);
        String pollingInterval = xmlDoc.get(ImmutableList.of(""), Ts43XmlDoc.Parm.POLLING_INTERVAL);

        assertThat(iccid).isNull();
        assertThat(pollingInterval).isNull();
    }
}
