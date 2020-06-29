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
package org.apache.isis.incubator.viewer.javafx.ui.components.markup;

import org.springframework.core.annotation.Order;

import org.apache.isis.applib.annotation.LabelPosition;
import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.applib.value.Markup;
import org.apache.isis.core.metamodel.facets.objectvalue.labelat.LabelAtFacet;
import org.apache.isis.incubator.viewer.javafx.ui.components.UiComponentHandlerFx;
import org.apache.isis.incubator.viewer.javafx.ui.components.form.FormField;
import org.apache.isis.incubator.viewer.javafx.ui.components.form.SimpleFormField;
import org.apache.isis.viewer.common.model.binding.UiComponentFactory.Request;

import lombok.val;
import lombok.extern.log4j.Log4j2;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.concurrent.Worker.State;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSException;

@org.springframework.stereotype.Component
@Order(OrderPrecedence.MIDPOINT)
public class MarkupFieldFactory implements UiComponentHandlerFx {
    
    @Override
    public boolean isHandling(Request request) {
        return request.isFeatureTypeInstanceOf(Markup.class);
    }

    @Override
    public FormField handle(Request request) {
        
        val markupHtml = request.getFeatureValue(Markup.class)
                .map(Markup::asString)
                .orElse("");

        val uiComponent = new WebViewFitContent(markupHtml);
        
        val uiLabel = new Label(request.getFeatureLabel());
        
        val labelPosition = request.getFeatureFacet(LabelAtFacet.class)
                .map(LabelAtFacet::label)
                .orElse(LabelPosition.NOT_SPECIFIED);
        
        return new SimpleFormField(labelPosition, uiLabel, uiComponent);
    }

    // -- HELPER
    
    /**
     * Unfortunately we have no simple means of auto-fitting a WebView, so we need a wrapper,
     * that executes some JavaScript on the rendered content, do determine the preferred height. 
     * <p>
     * @see <a href="https://stackoverflow.com/questions/25838965/size-javafx-webview-to-the-minimum-size-needed-by-the-document-body">stackoverflow</a>
     *  
     * @since Jun 29, 2020
     */
    @Log4j2
    private static final class WebViewFitContent extends Region {

        final WebView webview = new WebView();
        final WebEngine webEngine = webview.getEngine();

        public WebViewFitContent(String content) {
            webview.setPrefHeight(5);

            widthProperty().addListener((e, o, newWidth) -> {
                    webview.setPrefWidth(newWidth.doubleValue());
                    Platform.runLater(this::adjustHeight);
            });

            webview.getEngine().getLoadWorker().stateProperty().addListener((e, o, newState) -> {
                if (newState == State.SUCCEEDED) {
                    Platform.runLater(this::adjustHeight);
                }
            });

            webview.getChildrenUnmodifiable().addListener((ListChangeListener.Change<? extends Node> change) -> {
                val scrolls = webview.lookupAll(".scroll-bar");
                for (val scroll : scrolls) {
                    scroll.setVisible(false);
                }
            });

            setContent(content);
            getChildren().add(webview);
        }

        public void setContent(final String content) {
            Platform.runLater(()->webEngine.loadContent(getHtml(content), "text/html"));
            Platform.runLater(this::adjustHeight);
        }

        @Override
        protected void layoutChildren() {
            layoutInArea(webview, 0, 0, getWidth(), getHeight(), 0, HPos.CENTER, VPos.CENTER);
        }

        private void adjustHeight() {
            try {
                Object result = webEngine.executeScript(
                        "var myDiv = document.getElementById('mydiv');" +
                                "if (myDiv != null) myDiv.offsetHeight");
                if (result instanceof Integer) {
                    val height = ((Integer) result).intValue();
                    webview.setPrefHeight(height + 20);
                }
            } catch (JSException e) {
                log.error("failed to execute JavaScript to determine WebView's height", e);
            }
        }

        private String getHtml(String content) {
            return "<html><body>" +
                    "<div id=\"mydiv\">" + content + "</div>" +
                    "</body></html>";
        }

    }
    

}
