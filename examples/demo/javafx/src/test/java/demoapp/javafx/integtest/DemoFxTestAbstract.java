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
package demoapp.javafx.integtest;

import javax.inject.Inject;

import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.context.SpringBootTest;

import org.apache.isis.incubator.viewer.javafx.model.context.UiContextFx;
import org.apache.isis.incubator.viewer.javafx.viewer.IsisModuleIncViewerJavaFxViewer;
import org.apache.isis.security.bypass.IsisModuleSecurityBypass;
import org.apache.isis.testing.integtestsupport.applib.IsisIntegrationTestAbstract;

import demoapp.dom.DemoModule;

@SpringBootTest(
        classes = { 
                DemoModule.class,
                DemoFxTestConfig.class,
                
                // INCUBATING
                IsisModuleSecurityBypass.class,
                IsisModuleIncViewerJavaFxViewer.class,
        } 
)
public abstract class DemoFxTestAbstract extends IsisIntegrationTestAbstract {
    
    @Inject protected UiContextFx uiContext;
    
    @BeforeAll
    static void beforeAll() {
       //JavafxViewer.launch(DemoAppJavaFx.class, _Constants.emptyStringArray);    
    }
    
}
