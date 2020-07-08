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
package org.apache.isis.incubator.viewer.javafx.ui.decorator.prototyping;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import org.apache.isis.applib.annotation.LabelPosition;
import org.apache.isis.incubator.viewer.javafx.model.form.FormField;
import org.apache.isis.incubator.viewer.javafx.model.util._fx;
import org.apache.isis.viewer.common.model.decorator.prototyping.PrototypingDecorator;
import org.apache.isis.viewer.common.model.decorator.prototyping.PrototypingUiModel;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import lombok.RequiredArgsConstructor;
import lombok.val;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class PrototypingDecoratorForFormField implements PrototypingDecorator<FormField, FormField> {

    private final PrototypingInfoPopupProvider prototypingInfoService;
    
    @Override
    public FormField decorate(final FormField formField, final PrototypingUiModel prototypingUiModel) {
        return DecoratingFormField.of(formField, ()->
            prototypingInfoService.showPrototypingPopup(prototypingUiModel));
    }
    
    // -- HELPER

    @RequiredArgsConstructor(staticName = "of")
    private static class DecoratingFormField implements FormField {

        private final FormField delegate;
        private final Runnable onPrototypingPopup;
        private Pane fieldNode;
        
        @Override
        public LabelPosition getLabelPosition() {
            return delegate.getLabelPosition();
        }

        @Override
        public Node getUiLabel() {
            return delegate.getUiLabel();
        }

        @Override
        public Node getUiField() {
            if(fieldNode==null) {
                fieldNode = new VBox();
                val prototypingLabel = _fx.add(fieldNode, new Label("ⓘ"));
                _fx.add(fieldNode, delegate.getUiField());
                prototypingLabel.setTooltip(new Tooltip("Inspect Metamodel"));
                prototypingLabel.setOnMouseClicked(e->onPrototypingPopup.run());
            }
            return fieldNode;
        }

    }
    
}
