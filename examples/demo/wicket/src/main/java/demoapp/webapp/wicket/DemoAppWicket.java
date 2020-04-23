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

import org.apache.isis.valuetypes.asciidoc.ui.wkt.IsisModuleValAsciidocUiWkt;
import org.apache.isis.valuetypes.sse.ui.IsisModuleValSseUi;
import org.apache.isis.viewer.wicket.viewer.IsisModuleViewerWicketViewer;

import demoapp.utils.ThereCanBeOnlyOne;
import demoapp.webapp.DemoAppManifest;

/**
 * Bootstrap the application.
 */
@SpringBootApplication
@Import({
    DemoAppManifest.class,
    
    // WICKET INTEGRATION
    IsisModuleViewerWicketViewer.class, // wicket viewer
    IsisModuleValSseUi.class, // server sent events
    IsisModuleValAsciidocUiWkt.class, // ascii-doc rendering support (for Wicket)
    
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
        
        ThereCanBeOnlyOne.remoteShutdownOthersIfAny();
        
        SpringApplication.run(new Class[] { DemoAppWicket.class }, args);
    }



}
