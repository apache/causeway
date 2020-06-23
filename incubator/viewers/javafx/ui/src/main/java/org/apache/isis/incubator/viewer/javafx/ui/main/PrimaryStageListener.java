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
package org.apache.isis.incubator.viewer.javafx.ui.main;

import javax.inject.Inject;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import org.apache.isis.incubator.viewer.javafx.model.events.PrimaryStageReadyEvent;

import lombok.SneakyThrows;
import lombok.val;
import lombok.extern.log4j.Log4j2;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

@Component
@Log4j2
public class PrimaryStageListener {
    
    private final String applicationTitle;
    private final Resource fxml;
    private final ApplicationContext springContext;
    
    @Inject
    public PrimaryStageListener(
            @Value("isis.viewer.javafx.application.title") String applicationTitle,
            @Value("classpath:/ui.fxml") Resource fxml,
            ApplicationContext springContext) {
        this.applicationTitle = applicationTitle;
        this.fxml = fxml;
        this.springContext = springContext;
    }

    @EventListener(PrimaryStageReadyEvent.class)
    @SneakyThrows
    public void onStageReady(PrimaryStageReadyEvent event) {
        log.info("JavaFX primary stage is ready");
        val layoutUrl = this.fxml.getURL();
        val fxmlLoader = new FXMLLoader(layoutUrl);
        fxmlLoader.setControllerFactory(springContext::getBean);
        val uiRoot = (Parent)fxmlLoader.load();
        val scene = new Scene(uiRoot, 800, 600);
        val stage = event.getStage();
        stage.setScene(scene);
        stage.setTitle(applicationTitle);
        stage.show();
    }

}
