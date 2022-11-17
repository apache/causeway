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
package org.apache.causeway.incubator.viewer.javafx.ui.decorator.prototyping;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import org.apache.causeway.incubator.viewer.javafx.model.util._fx;
import org.apache.causeway.viewer.commons.model.decorators.PrototypingDecorator;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import lombok.RequiredArgsConstructor;
import lombok.val;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class PrototypingDecoratorForButton implements PrototypingDecorator<Button, Node> {

    private final PrototypingInfoPopupProvider prototypingInfoService;

    @Override
    public Node decorate(final Button uiButton, final PrototypingDecorationModel prototypingDecorationModel) {
        val span = new HBox();
        val prototypingLabel = _fx.add(span, new Label("ⓘ"));
        _fx.add(span, uiButton);
        prototypingLabel.setTooltip(new Tooltip("Inspect Metamodel"));
        prototypingLabel.setOnMouseClicked(e->
            prototypingInfoService.showPrototypingPopup(prototypingDecorationModel));

        uiButton.getStyleClass().add("button-prototyping");

        return span;
    }


}
