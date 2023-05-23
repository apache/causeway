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
package demoapp.webapp.wicket.jpa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Import;

import org.apache.causeway.commons.internal.os._OsUtil;
import org.apache.causeway.core.config.presets.CausewayPresets;
import org.apache.causeway.core.config.util.SpringProfileUtil;
import org.apache.causeway.extensions.commandlog.applib.CausewayModuleExtCommandLogApplib;
import org.apache.causeway.extensions.pdfjs.wkt.ui.CausewayModuleExtPdfjsWicketUi;
import org.apache.causeway.extensions.sse.wicket.CausewayModuleExtSseWicket;
import org.apache.causeway.valuetypes.asciidoc.metamodel.CausewayModuleValAsciidocMetaModel;
import org.apache.causeway.valuetypes.asciidoc.persistence.jpa.CausewayModuleValAsciidocPersistenceJpa;
import org.apache.causeway.valuetypes.asciidoc.ui.wkt.CausewayModuleValAsciidocUiWkt;
import org.apache.causeway.valuetypes.markdown.metamodel.CausewayModuleValMarkdownMetaModel;
import org.apache.causeway.valuetypes.markdown.persistence.jpa.CausewayModuleValMarkdownPersistenceJpa;
import org.apache.causeway.valuetypes.markdown.ui.wkt.CausewayModuleValMarkdownUiWkt;
import org.apache.causeway.valuetypes.vega.metamodel.CausewayModuleValVegaMetaModel;
import org.apache.causeway.valuetypes.vega.persistence.jpa.CausewayModuleValVegaPersistenceJpa;
import org.apache.causeway.valuetypes.vega.ui.wkt.CausewayModuleValVegaUiWkt;
import org.apache.causeway.viewer.wicket.applib.CausewayModuleViewerWicketApplibMixins;
import org.apache.causeway.viewer.wicket.viewer.CausewayModuleViewerWicketViewer;

import demoapp.web.DemoAppManifestJpa;
import demoapp.webapp.wicket.common.DemoAppWicketCommon;

/**
 * Bootstrap the application.
 */
@SpringBootApplication
@Import({
    // App-Manifest (Configuration)
    DemoAppManifestJpa.class,
    //CausewayModuleSecurityBypass.class, // <-- bypass authentication

    // Metamodel
    CausewayModuleValAsciidocMetaModel.class,
    CausewayModuleValMarkdownMetaModel.class,
    CausewayModuleValVegaMetaModel.class,

    // UI (Wicket Viewer)
    CausewayModuleViewerWicketViewer.class,
    CausewayModuleViewerWicketApplibMixins.class,
    CausewayModuleExtSseWicket.class,
    CausewayModuleValAsciidocUiWkt.class,
    CausewayModuleValMarkdownUiWkt.class,
    CausewayModuleValVegaUiWkt.class,
    CausewayModuleExtPdfjsWicketUi.class,

    // Custom Demo UI (Wicket Viewer)
    DemoAppWicketCommon.class,

    // Persistence/Converters (JPA)
    CausewayModuleValAsciidocPersistenceJpa.class,
    CausewayModuleValMarkdownPersistenceJpa.class,
    CausewayModuleValVegaPersistenceJpa.class,

    // XrayEnable.class // for debugging only
    // WicketViewerXrayEnable.class // for debugging only
})
//@Log4j2
public class DemoAppWicketJpa extends SpringBootServletInitializer {

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
        //CausewayPresets.logging(ComponentFactoryRegistryDefault.class, "debug");
        //CausewayPresets.logging(EntityModel.class, "debug");
        //CausewayPresets.logging(FormExecutorDefault.class, "debug");
    	//System.setProperty("spring.jpa.show-sql", "true");

        SpringProfileUtil.removeActiveProfile("demo-jdo"); // just in case
    	SpringProfileUtil.addActiveProfile("demo-jpa");

    	CausewayModuleExtCommandLogApplib.honorSystemEnvironment();

        SpringApplication.run(new Class[] { DemoAppWicketJpa.class }, args);

    }

}
