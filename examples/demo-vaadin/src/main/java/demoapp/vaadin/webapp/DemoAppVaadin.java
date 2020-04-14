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
package demoapp.vaadin.webapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Import;

import org.apache.isis.core.config.presets.IsisPresets;
import org.apache.isis.core.runtimeservices.session.IsisInteractionFactoryDefault;
import org.apache.isis.incubator.viewer.vaadin.ui.auth.VaadinAuthenticationHandler;
import org.apache.isis.incubator.viewer.vaadin.viewer.IsisModuleIncViewerVaadinViewer;
import org.apache.isis.incubator.viewer.vaadin.viewer.IsisServletForVaadin;

import demoapp.utils.ThereCanBeOnlyOne;
import demoapp.webapp.DemoApp;

/**
 * Bootstrap the application.
 */
@SpringBootApplication
@Import({
    DemoApp.AppManifest.class,
    
    // INCUBATING
    IsisModuleIncViewerVaadinViewer.class, // vaadin viewer
  
})
public class DemoAppVaadin extends SpringBootServletInitializer {

    /**
     * 
     * @param args
     * @implNote this is to support the <em>Spring Boot Maven Plugin</em>, which auto-detects an 
     * entry point by searching for classes having a {@code main(...)}
     */
    public static void main(String[] args) {
        
        IsisPresets.logging(IsisInteractionFactoryDefault.class, "debug");
        IsisPresets.logging(VaadinAuthenticationHandler.class, "debug");
        IsisPresets.logging(IsisServletForVaadin.class, "debug");
        
        ThereCanBeOnlyOne.remoteShutdownOthersIfAny();
        
        SpringApplication.run(new Class[] { DemoAppVaadin.class }, args);
    }

}
