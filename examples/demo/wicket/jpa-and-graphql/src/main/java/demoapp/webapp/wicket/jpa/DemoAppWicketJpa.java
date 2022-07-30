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

import org.apache.isis.commons.internal.os._OsUtil;
import org.apache.isis.core.config.presets.IsisPresets;
import org.apache.isis.core.config.util.SpringProfileUtil;
import org.apache.isis.extensions.commandlog.applib.IsisModuleExtCommandLogApplib;
import org.apache.isis.extensions.pdfjs.wkt.ui.IsisModuleExtPdfjsWicketUi;
import org.apache.isis.extensions.sse.wicket.IsisModuleExtSseWicket;
import org.apache.isis.valuetypes.asciidoc.metamodel.IsisModuleValAsciidocMetaModel;
import org.apache.isis.valuetypes.asciidoc.persistence.jpa.IsisModuleValAsciidocPersistenceJpa;
import org.apache.isis.valuetypes.asciidoc.ui.wkt.IsisModuleValAsciidocUiWkt;
import org.apache.isis.valuetypes.markdown.metamodel.IsisModuleValMarkdownMetaModel;
import org.apache.isis.valuetypes.markdown.persistence.jpa.IsisModuleValMarkdownPersistenceJpa;
import org.apache.isis.valuetypes.markdown.ui.wkt.IsisModuleValMarkdownUiWkt;
import org.apache.isis.viewer.graphql.viewer.IsisModuleIncViewerGraphqlViewer;
import org.apache.isis.viewer.wicket.viewer.IsisModuleViewerWicketViewer;

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

    // UI (Wicket Viewer)
    IsisModuleViewerWicketViewer.class,
    IsisModuleExtSseWicket.class,
    IsisModuleValAsciidocUiWkt.class,
    IsisModuleValMarkdownUiWkt.class,
    IsisModuleExtPdfjsWicketUi.class,

    // GraphQL
    IsisModuleIncViewerGraphqlViewer.class,

    // Custom Demo UI (Wicket Viewer)
    DemoAppWicketCommon.class,

    // Persistence/Converters (JPA)
    IsisModuleValAsciidocPersistenceJpa.class,
    IsisModuleValMarkdownPersistenceJpa.class,

    //XrayEnable.class // for debugging only
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

        SpringProfileUtil.removeActiveProfile("demo-jdo"); // just in case
    	SpringProfileUtil.addActiveProfile("demo-jpa");

    	IsisModuleExtCommandLogApplib.honorSystemEnvironment();

        SpringApplication.run(new Class[] { DemoAppWicketJpa.class }, args);

    }

}
