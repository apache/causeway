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
import org.springframework.context.annotation.Import;

import org.apache.isis.core.commons.internal.debug._Probe;
import org.apache.isis.core.config.presets.IsisPresets;
import org.apache.isis.core.runtimeservices.session.IsisInteractionFactoryDefault;
import org.apache.isis.incubator.viewer.javafx.ui.IsisModuleIncViewerJavaFxUi;
import org.apache.isis.incubator.viewer.javafx.viewer.JavafxViewer;

import demoapp.dom.DemoModule;

/**
 * Bootstrap the application.
 */
@SpringBootApplication
@Import({
    DemoModule.class,
    
    IsisModuleIncViewerJavaFxUi.class,
    
    // INCUBATING
  
})
public class DemoAppJavaFx {

    public static void main(String[] args) {
        IsisPresets.logging(IsisInteractionFactoryDefault.class, "debug");
        IsisPresets.logging(_Probe.class, "debug"); // enable debug entry logging
        
        JavafxViewer.launch(DemoAppJavaFx.class, args);
    }

}
