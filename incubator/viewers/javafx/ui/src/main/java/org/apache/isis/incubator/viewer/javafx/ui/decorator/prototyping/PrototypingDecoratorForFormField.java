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

import org.apache.isis.incubator.viewer.javafx.model.util._fx;
import org.apache.isis.viewer.common.model.decorator.prototyping.PrototypingDecorator;
import org.apache.isis.viewer.common.model.decorator.prototyping.PrototypingUiModel;

import lombok.RequiredArgsConstructor;
import lombok.val;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class PrototypingDecoratorForFormField implements PrototypingDecorator<Node, Node> {

    private final PrototypingInfoPopupProvider prototypingInfoService;

    @Override
    public Node decorate(final Node formField, final PrototypingUiModel prototypingUiModel) {

        val container = new HBox();
        val infoLabel = _fx.add(container, new Label("ⓘ"));
        _fx.add(container, formField);

        infoLabel.setTooltip(new Tooltip("Inspect Metamodel"));
        infoLabel.setOnMouseClicked(e->prototypingInfoService.showPrototypingPopup(prototypingUiModel));

        return container;
    }

}
