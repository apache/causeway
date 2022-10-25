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
package demoapp.javafx;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import org.apache.causeway.commons.internal.debug._Probe;
import org.apache.causeway.core.config.presets.CausewayPresets;
import org.apache.causeway.core.runtimeservices.session.InteractionServiceDefault;
import org.apache.causeway.incubator.viewer.javafx.model.events.JavaFxViewerConfig;
import org.apache.causeway.incubator.viewer.javafx.model.util._fx;
import org.apache.causeway.incubator.viewer.javafx.viewer.CausewayModuleIncViewerJavaFxViewer;
import org.apache.causeway.incubator.viewer.javafx.viewer.JavafxViewer;
import org.apache.causeway.security.bypass.CausewayModuleSecurityBypass;

import demoapp.dom.DemoModuleJpa;

/**
 * Bootstrap the application.
 */
@SpringBootApplication
@Import({
    DemoModuleJpa.class,

    // INCUBATING
    CausewayModuleSecurityBypass.class,
    CausewayModuleIncViewerJavaFxViewer.class,

})
public class DemoAppJavaFx {

    @Bean
    public JavaFxViewerConfig viewerConfig() {
        return JavaFxViewerConfig.builder()
                .applicationTitle("Apache Causeway Demo")
                .applicationIcon(_fx.imageFromClassPath(DemoAppJavaFx.class, "icon.png"))
                .brandingIcon(_fx.imageFromClassPath(DemoAppJavaFx.class, "gift_32.png"))
                .objectFallbackIcon(_fx.imageFromClassPath(DemoAppJavaFx.class, "object_fallback_icon.png"))
                .build();
    }

    public static void main(final String[] args) {

        CausewayPresets.logging(InteractionServiceDefault.class, "debug");
        CausewayPresets.logging(_Probe.class, "debug"); // enable debug entry logging

        CausewayPresets.prototyping(); // use prototyping mode as default, unless explicitly overridden (INCUBATING)
        System.setProperty("spring.profiles.active", "demo-jpa");

        JavafxViewer.launch(DemoAppJavaFx.class, args);
    }

}
