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

import org.apache.causeway.commons.internal.os._OsUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Import;

import org.apache.causeway.core.config.presets.IsisPresets;
import org.apache.causeway.core.config.util.SpringProfileUtil;
import org.apache.causeway.extensions.commandlog.applib.IsisModuleExtCommandLogApplib;
import org.apache.causeway.extensions.pdfjs.wkt.ui.IsisModuleExtPdfjsWicketUi;
import org.apache.causeway.extensions.sse.wicket.IsisModuleExtSseWicket;
import org.apache.causeway.valuetypes.asciidoc.metamodel.IsisModuleValAsciidocMetaModel;
import org.apache.causeway.valuetypes.asciidoc.persistence.jpa.IsisModuleValAsciidocPersistenceJpa;
import org.apache.causeway.valuetypes.asciidoc.ui.wkt.IsisModuleValAsciidocUiWkt;
import org.apache.causeway.valuetypes.markdown.metamodel.IsisModuleValMarkdownMetaModel;
import org.apache.causeway.valuetypes.markdown.persistence.jpa.IsisModuleValMarkdownPersistenceJpa;
import org.apache.causeway.valuetypes.markdown.ui.wkt.IsisModuleValMarkdownUiWkt;
import org.apache.causeway.valuetypes.vega.metamodel.IsisModuleValVegaMetaModel;
import org.apache.causeway.valuetypes.vega.persistence.jpa.IsisModuleValVegaPersistenceJpa;
import org.apache.causeway.valuetypes.vega.ui.wkt.IsisModuleValVegaUiWkt;
import org.apache.causeway.viewer.wicket.viewer.IsisModuleViewerWicketViewer;

import demoapp.web.DemoAppManifestJpa;
import demoapp.webapp.wicket.common.ui.DemoAppWicketCommon;

/**
 * Bootstrap the application.
 */
@SpringBootApplication
@Import({
    // App-Manifest (Configuration)
    DemoAppManifestJpa.class,
    //IsisModuleSecurityBypass.class, // <-- bypass authentication

    // Metamodel
    IsisModuleValAsciidocMetaModel.class,
    IsisModuleValMarkdownMetaModel.class,
    IsisModuleValVegaMetaModel.class,

    // UI (Wicket Viewer)
    IsisModuleViewerWicketViewer.class,
    IsisModuleExtSseWicket.class,
    IsisModuleValAsciidocUiWkt.class,
    IsisModuleValMarkdownUiWkt.class,
    IsisModuleValVegaUiWkt.class,
    IsisModuleExtPdfjsWicketUi.class,

    // Custom Demo UI (Wicket Viewer)
    DemoAppWicketCommon.class,

    // Persistence/Converters (JPA)
    IsisModuleValAsciidocPersistenceJpa.class,
    IsisModuleValMarkdownPersistenceJpa.class,
    IsisModuleValVegaPersistenceJpa.class,

    //XrayEnable.class // for debugging only
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

    	IsisPresets.prototyping();
        //IsisPresets.logging(WebRequestCycleForIsis.class, "debug");
        //IsisPresets.logging(ComponentFactoryRegistryDefault.class, "debug");
        //IsisPresets.logging(EntityModel.class, "debug");
        //IsisPresets.logging(FormExecutorDefault.class, "debug");
    	//System.setProperty("spring.jpa.show-sql", "true");

        SpringProfileUtil.removeActiveProfile("demo-jdo"); // just in case
    	SpringProfileUtil.addActiveProfile("demo-jpa");

    	IsisModuleExtCommandLogApplib.honorSystemEnvironment();

        SpringApplication.run(new Class[] { DemoAppWicketJpa.class }, args);

    }

}
