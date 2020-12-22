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

import java.util.Optional;

import javax.inject.Inject;

import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import org.apache.isis.commons.internal.debug._Probe;
import org.apache.isis.core.interaction.events.IsisInteractionLifecycleEvent;
import org.apache.isis.incubator.viewer.javafx.model.events.JavaFxViewerConfig;
import org.apache.isis.incubator.viewer.javafx.model.events.PrimaryStageReadyEvent;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import lombok.extern.log4j.Log4j2;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Inject})
@Log4j2
public class UiBuilderFx {
    
    private final ApplicationContext springContext;
    private final JavaFxViewerConfig viewerConfig;

    @EventListener(PrimaryStageReadyEvent.class)
    @SneakyThrows
    public void onStageReady(PrimaryStageReadyEvent event) {
        log.info("JavaFX primary stage is ready");
        val layoutUrl = this.viewerConfig.getUiLayout().getURL();
        val fxmlLoader = new FXMLLoader(layoutUrl);
        fxmlLoader.setControllerFactory(springContext::getBean);
        val uiRoot = (Parent)fxmlLoader.load();
        val scene = new Scene(uiRoot);
        scene.getStylesheets().add("/ui.css");
        val stage = event.getStage();
        stage.setScene(scene);
        setupTitle(stage);
        setupIcon(stage);
        stage.show();
    }

    @EventListener(IsisInteractionLifecycleEvent.class)
    public void onIsisInteractionLifecycleEvent(IsisInteractionLifecycleEvent event) {
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
    
    // -- HELPER
        
    private void setupTitle(Stage stage) {
        val title = Optional.ofNullable(viewerConfig.getApplicationTitle())
                .orElse("Unknonw Title");
        stage.setTitle(title);
    }
    
    private void setupIcon(Stage stage) {
        val icon = viewerConfig.getApplicationIcon();
        if(icon==null) {
            return; 
        }
        stage.getIcons().add(icon);
    }
    
    

}
