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
package demoapp.webapp.wicket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Import;

import org.apache.isis.extensions.viewer.wicket.pdfjs.ui.IsisModuleExtPdfjsUi;
import org.apache.isis.valuetypes.asciidoc.metamodel.IsisModuleValAsciidocMetaModel;
import org.apache.isis.valuetypes.asciidoc.persistence.jdo.dn5.IsisModuleValAsciidocPersistenceJdoDn5;
import org.apache.isis.valuetypes.markdown.persistence.jdo.dn5.IsisModuleValMarkdownPersistenceJdoDn5;
import org.apache.isis.valuetypes.asciidoc.ui.wkt.IsisModuleValAsciidocUiWkt;
import org.apache.isis.valuetypes.markdown.ui.wkt.IsisModuleValMarkdownUiWkt;
import org.apache.isis.valuetypes.sse.ui.wkt.IsisModuleValSseUiWkt;
import org.apache.isis.viewer.wicket.viewer.IsisModuleViewerWicketViewer;

import demoapp.web.DemoAppManifest;
import demoapp.web._infra.utils.ThereCanBeOnlyOne;

/**
 * Bootstrap the application.
 */
@SpringBootApplication
@Import({
    DemoAppManifest.class,

    //ThereCanBeOnlyOne.class, // shutdown demo instance if any already running (specific to the demo only)
    
    // Metamodel
    IsisModuleValAsciidocMetaModel.class,

    // UI (Wicket Viewer)
    IsisModuleViewerWicketViewer.class,
    IsisModuleValSseUiWkt.class,
    IsisModuleValAsciidocUiWkt.class,
    IsisModuleValMarkdownUiWkt.class,
    IsisModuleExtPdfjsUi.class,

    // Persistence (JDO/DN5)
    IsisModuleValAsciidocPersistenceJdoDn5.class,   //
    IsisModuleValMarkdownPersistenceJdoDn5.class,

})
//@Log4j2
public class DemoAppWicket extends SpringBootServletInitializer {

    /**
     * 
     * @param args
     * @implNote this is to support the <em>Spring Boot Maven Plugin</em>, which auto-detects an 
     * entry point by searching for classes having a {@code main(...)}
     */
    public static void main(String[] args) {
        //IsisPresets.prototyping();
        //DebugLoggingPreset.PERSISTENCE.apply();
        //DebugLoggingPreset.ISIS_SESSION.apply();
        
        SpringApplication.run(new Class[] { DemoAppWicket.class }, args);
    }



}
