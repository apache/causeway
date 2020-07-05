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
package org.apache.isis.incubator.viewer.javafx.viewer;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

import org.apache.isis.core.commons.internal._Constants;
import org.apache.isis.incubator.viewer.javafx.model.events.PrimaryStageReadyEvent;

import javafx.application.Application;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.stage.Stage;

public class JavafxViewerApplication extends Application {

    private ConfigurableApplicationContext springContext;
    
    static Class<?>[] sources;
    
    @Override
    public void init() throws Exception {
        
        final ApplicationContextInitializer<GenericApplicationContext> initializer = 
        ac->{
             ac.registerBean(Application.class, ()->JavafxViewerApplication.this);
             ac.registerBean(Parameters.class, this::getParameters);
             ac.registerBean(HostServices.class, this::getHostServices);
        };
        
        this.springContext = new SpringApplicationBuilder()
        .sources(sources)
        .initializers(initializer)
        .run(getParameters().getRaw().toArray(_Constants.emptyStringArray));
    }
    
    
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        this.springContext.publishEvent(new PrimaryStageReadyEvent(primaryStage));
    }
    
    @Override
    public void stop() throws Exception {
        this.springContext.close();
        Platform.exit();
    }
    
    

}
