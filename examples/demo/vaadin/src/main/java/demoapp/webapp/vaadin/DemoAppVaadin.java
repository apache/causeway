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
package demoapp.webapp.vaadin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Import;

import org.apache.causeway.extensions.sse.wicket.CausewayModuleExtSseWicket;
import org.apache.causeway.incubator.viewer.vaadin.viewer.CausewayModuleIncViewerVaadinViewer;
import org.apache.causeway.valuetypes.asciidoc.ui.vaa.CausewayModuleValAsciidocUiVaa;
import org.apache.causeway.valuetypes.asciidoc.ui.wkt.CausewayModuleValAsciidocUiWkt;
import org.apache.causeway.viewer.wicket.viewer.CausewayModuleViewerWicketViewer;

import demoapp.dom.DemoModuleCommon;
import demoapp.web.DemoAppManifestJdo;

/**
 * Bootstrap the application.
 */
@SpringBootApplication
@Import({
    DemoModuleCommon.class,
    DemoAppManifestJdo.class,

    // INCUBATING
    CausewayModuleIncViewerVaadinViewer.class, // vaadin viewer
    CausewayModuleValAsciidocUiVaa.class, // ascii-doc rendering support (for Vaadin)

    // WICKET INTEGRATION ... to allow side by side comparison
    CausewayModuleViewerWicketViewer.class, // wicket viewer
    CausewayModuleExtSseWicket.class, // server sent events
    CausewayModuleValAsciidocUiWkt.class, // ascii-doc rendering support (for Wicket)

})
public class DemoAppVaadin extends SpringBootServletInitializer {

    /**
     *
     * @param args
     * @implNote this is to support the <em>Spring Boot Maven Plugin</em>, which auto-detects an
     * entry point by searching for classes having a {@code main(...)}
     */
    public static void main(final String[] args) {

//        CausewayPresets.logging(InteractionServiceDefault.class, "debug");
//        CausewayPresets.logging(VaadinAuthenticationHandler.class, "debug");
//        CausewayPresets.logging(CausewayServletForVaadin.class, "debug");
//        CausewayPresets.logging(_Probe.class, "debug"); // enable debug entry logging

        System.setProperty("spring.profiles.active", "demo-jdo");

        SpringApplication.run(new Class[] { DemoAppVaadin.class }, args);
    }

}
