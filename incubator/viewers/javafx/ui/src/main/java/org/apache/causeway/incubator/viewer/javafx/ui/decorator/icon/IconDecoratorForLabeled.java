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
package org.apache.causeway.incubator.viewer.javafx.ui.decorator.icon;

import java.util.Optional;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import org.apache.causeway.incubator.viewer.javafx.model.icon.IconService;
import org.apache.causeway.incubator.viewer.javafx.model.util._fx;
import org.apache.causeway.viewer.commons.model.decorators.IconDecorator;

import javafx.scene.control.Labeled;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class IconDecoratorForLabeled implements IconDecorator<Labeled, Labeled> {

    private final IconService iconService;

    @Override
    public Labeled decorate(final Labeled uiComponent, final Optional<FontAwesomeDecorationModel> fontAwesomeDecorationModel) {
        fontAwesomeDecorationModel.ifPresent(fa->{
            var icon = iconService.fontAwesome(fa);
            icon
            .map(this::iconForImage)
            .ifPresent(uiComponent::setGraphic);
        });
        return uiComponent;
    }

    private ImageView iconForImage(final Image image) {
        return _fx.iconForImage(image, 16, 16);
    }

}
