/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.causeway.viewer.thymeflux.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Import;

import org.apache.causeway.commons.internal.os._OsUtil;
import org.apache.causeway.core.config.presets.CausewayPresets;
import org.apache.causeway.security.bypass.CausewayModuleSecurityBypass;
import org.apache.causeway.viewer.thymeflux.test.config.ThymefluxConfig_headlessPersistence;
import org.apache.causeway.viewer.thymeflux.test.features.ThymefluxFeaturesController;
import org.apache.causeway.viewer.thymeflux.viewer.CausewayModuleIncViewerThymefluxViewer;

/**
 * Bootstrap the application.
 */
@SpringBootApplication
@Import({
    CausewayModuleSecurityBypass.class,
    ThymefluxConfig_headlessPersistence.class,

    // THYMEFLUX INTEGRATION
    CausewayModuleIncViewerThymefluxViewer.class,
    // FEATURES
    ThymefluxFeaturesController.class
})
public class ThymefluxFeaturesApp extends SpringBootServletInitializer {

    /**
     *
     * @param args
     * @implNote this is to support the <em>Spring Boot Maven Plugin</em>, which auto-detects an
     * entry point by searching for classes having a {@code main(...)}
     */
    public static void main(final String[] args) {

        // activates when sys-env THERE_CAN_BE_ONLY_ONE=true
        _OsUtil.thereCanBeOnlyOne();

        CausewayPresets.prototyping();
        //CausewayPresets.logging(WebRequestCycleForCauseway.class, "debug");

        SpringApplication.run(new Class[] { ThymefluxFeaturesApp.class }, args);
    }

}
