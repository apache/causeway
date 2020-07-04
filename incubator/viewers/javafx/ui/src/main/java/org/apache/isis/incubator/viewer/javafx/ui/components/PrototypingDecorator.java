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
package org.apache.isis.incubator.viewer.javafx.ui.components;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.LabelPosition;
import org.apache.isis.core.metamodel.interactions.managed.ManagedMember;
import org.apache.isis.incubator.viewer.javafx.model.util._fx;
import org.apache.isis.incubator.viewer.javafx.ui.components.dialog.Dialogs;
import org.apache.isis.incubator.viewer.javafx.ui.components.form.FormField;
import org.apache.isis.incubator.viewer.javafx.ui.services.PrototypingInfoService;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class PrototypingDecorator {
    
    private final PrototypingInfoService prototypingInfoService;

    public Node decorateButton(
            @NonNull final ManagedMember managedAction, 
            @NonNull final Button uiButton) {
        
        val span = new HBox();
        val prototypingLabel = _fx.add(span, new Label("ⓘ"));
        _fx.add(span, uiButton);
        prototypingLabel.setTooltip(new Tooltip("Inspect Metamodel"));
        prototypingLabel.setOnMouseClicked(e->showPrototypingPopup(managedAction));
        return span;
    }

    public FormField decorateFormField(
            @NonNull final FormField formField, 
            @NonNull final ManagedMember managedMember) {
        
        return DecoratingFormField.of(formField, ()->showPrototypingPopup(managedMember));
    }
    
    // -- HELPER
    
    private void showPrototypingPopup(final ManagedMember managedMember) {
        val infoNode = prototypingInfoService.getPrototypingInfoUiComponent(managedMember);
        val headerText = String.format("%s: %s", 
                managedMember.getMemberType().name(), 
                managedMember.getName());
        val contentText = String.format("%s", managedMember.getId());
        Dialogs.message("Inspect Metamodel", headerText, contentText, infoNode);
    }

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
