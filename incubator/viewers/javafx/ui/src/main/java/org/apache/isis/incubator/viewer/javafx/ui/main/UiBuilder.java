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

import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import org.apache.isis.core.commons.internal.debug._Probe;
import org.apache.isis.core.runtime.events.iactn.IsisInteractionLifecycleEvent;
import org.apache.isis.incubator.viewer.javafx.model.events.JavaFxViewerConfig;
import org.apache.isis.incubator.viewer.javafx.model.events.PrimaryStageReadyEvent;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import lombok.extern.log4j.Log4j2;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Inject})
@Log4j2
public class UiBuilder {
    
    private final ApplicationContext springContext;
    private final JavaFxViewerConfig viewerConfig;
    private Scene scene;

    @EventListener(PrimaryStageReadyEvent.class)
    @SneakyThrows
    public void onStageReady(PrimaryStageReadyEvent event) {
        log.info("JavaFX primary stage is ready");
        val layoutUrl = this.viewerConfig.getUiLayout().getURL();
        val fxmlLoader = new FXMLLoader(layoutUrl);
        fxmlLoader.setControllerFactory(springContext::getBean);
        val uiRoot = (Parent)fxmlLoader.load();
        val scene = new Scene(uiRoot);
        val stage = event.getStage();
        stage.setScene(scene);
        stage.setTitle(viewerConfig.getApplicationTitle());
        stage.show();
        
        this.scene = scene;
    }
    
    @EventListener(IsisInteractionLifecycleEvent.class)
    public void onIsisInteractionLifecycleEvent(IsisInteractionLifecycleEvent event) {
        if(scene==null) {
            return;
        }
        switch(event.getEventType()) {
        case HAS_STARTED:
            //TODO this would be the place to indicate to the user, that a long running task has started  
            _Probe.errOut("Interaction HAS_STARTED conversationId=%s", event.getConversationId());
            //scene.getRoot().cursorProperty().set(Cursor.WAIT);
            break;
        case IS_ENDING:
            //TODO this would be the place to indicate to the user, that a long running task has ended
            _Probe.errOut("Interaction IS_ENDING conversationId=%s", event.getConversationId());
            //scene.getRoot().cursorProperty().set(Cursor.DEFAULT);
            break;
        default:
            break;
        }
        
    }
    
    

}
