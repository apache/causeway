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
package demoapp.webapp.wicket.jdo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Import;

import org.apache.isis.core.config.presets.IsisPresets;
import org.apache.isis.extensions.viewer.wicket.pdfjs.ui.IsisModuleExtPdfjsUi;
import org.apache.isis.valuetypes.asciidoc.metamodel.IsisModuleValAsciidocMetaModel;
import org.apache.isis.valuetypes.asciidoc.persistence.jdo.dn5.IsisModuleValAsciidocPersistenceJdoDn5;
import org.apache.isis.valuetypes.asciidoc.ui.wkt.IsisModuleValAsciidocUiWkt;
import org.apache.isis.valuetypes.markdown.metamodel.IsisModuleValMarkdownMetaModel;
import org.apache.isis.valuetypes.markdown.persistence.jdo.dn5.IsisModuleValMarkdownPersistenceJdoDn5;
import org.apache.isis.valuetypes.markdown.ui.wkt.IsisModuleValMarkdownUiWkt;
import org.apache.isis.valuetypes.sse.ui.wkt.IsisModuleValSseUiWkt;
import org.apache.isis.viewer.wicket.viewer.IsisModuleViewerWicketViewer;

import demoapp.web.DemoAppManifestJdo;
import demoapp.webapp.wicket.common.ui.DemoAppWicketCommon;

/**
 * Bootstrap the application.
 */
@SpringBootApplication
@Import({
    // App-Manifest (Configuration)
    DemoAppManifestJdo.class,

    // Metamodel
    IsisModuleValAsciidocMetaModel.class,
    IsisModuleValMarkdownMetaModel.class,

    // UI (Wicket Viewer)
    IsisModuleViewerWicketViewer.class,
    IsisModuleValSseUiWkt.class,
    IsisModuleValAsciidocUiWkt.class,
    IsisModuleValMarkdownUiWkt.class,
    IsisModuleExtPdfjsUi.class,

    // Custom Demo UI (Wicket Viewer)
    DemoAppWicketCommon.class,

    // Persistence (JDO/DN5)
    IsisModuleValAsciidocPersistenceJdoDn5.class,
    IsisModuleValMarkdownPersistenceJdoDn5.class,

    // XrayEnable.class // for debugging only
})
//@Log4j2
public class DemoAppWicketJdo extends SpringBootServletInitializer {

    /**
     *
     * @param args
     * @implNote this is to support the <em>Spring Boot Maven Plugin</em>, which auto-detects an
     * entry point by searching for classes having a {@code main(...)}
     */
    public static void main(final String[] args) {
    	IsisPresets.prototyping();
        //IsisPresets.logging(WebRequestCycleForIsis.class, "debug");

        System.setProperty("spring.profiles.active", "demo-jdo");

        SpringApplication.run(new Class[] { DemoAppWicketJdo.class }, args);

    }

}
