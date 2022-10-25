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
package org.apache.causeway.incubator.viewer.javafx.ui;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import org.apache.causeway.incubator.viewer.javafx.model.events.CausewayModuleIncViewerJavaFxModel;
import org.apache.causeway.incubator.viewer.javafx.ui.components.UiComponentFactoryFx;
import org.apache.causeway.incubator.viewer.javafx.ui.components.markup.MarkupFieldFactory;
import org.apache.causeway.incubator.viewer.javafx.ui.components.number.NumberFieldFactory;
import org.apache.causeway.incubator.viewer.javafx.ui.components.objectref.ObjectReferenceFieldFactory;
import org.apache.causeway.incubator.viewer.javafx.ui.components.other.FallbackFieldFactory;
import org.apache.causeway.incubator.viewer.javafx.ui.components.temporal.TemporalFieldFactory;
import org.apache.causeway.incubator.viewer.javafx.ui.components.text.TextFieldFactory;
import org.apache.causeway.incubator.viewer.javafx.ui.decorator.disabling.DisablingDecoratorForButton;
import org.apache.causeway.incubator.viewer.javafx.ui.decorator.disabling.DisablingDecoratorForFormField;
import org.apache.causeway.incubator.viewer.javafx.ui.decorator.icon.IconDecoratorForLabeled;
import org.apache.causeway.incubator.viewer.javafx.ui.decorator.icon.IconDecoratorForMenuItem;
import org.apache.causeway.incubator.viewer.javafx.ui.decorator.icon.IconServiceDefault;
import org.apache.causeway.incubator.viewer.javafx.ui.decorator.prototyping.PrototypingDecoratorForButton;
import org.apache.causeway.incubator.viewer.javafx.ui.decorator.prototyping.PrototypingDecoratorForFormField;
import org.apache.causeway.incubator.viewer.javafx.ui.decorator.prototyping.PrototypingInfoPopupProvider;
import org.apache.causeway.incubator.viewer.javafx.ui.main.MainViewFx;
import org.apache.causeway.incubator.viewer.javafx.ui.main.UiActionHandlerFx;
import org.apache.causeway.incubator.viewer.javafx.ui.main.UiBuilderFx;
import org.apache.causeway.incubator.viewer.javafx.ui.main.UiContextFxDefault;
import org.apache.causeway.viewer.commons.applib.CausewayModuleViewerCommonsApplib;

/**
 *
 * @since 2.0
 */
@Configuration
@Import({
        // Modules
        CausewayModuleViewerCommonsApplib.class,
        CausewayModuleIncViewerJavaFxModel.class,

        // @Components's
        UiBuilderFx.class,
        MainViewFx.class,

        // Component Factories
        TextFieldFactory.class,
        MarkupFieldFactory.class,
        ObjectReferenceFieldFactory.class,
        NumberFieldFactory.class,
        TemporalFieldFactory.class,
        FallbackFieldFactory.class,

        // Decorators
        PrototypingDecoratorForButton.class,
        PrototypingDecoratorForFormField.class,
        PrototypingInfoPopupProvider.class,

        DisablingDecoratorForButton.class,
        DisablingDecoratorForFormField.class,

        IconDecoratorForLabeled.class,
        IconDecoratorForMenuItem.class,
        IconServiceDefault.class,

        // @Service's
        UiComponentFactoryFx.class,
        UiActionHandlerFx.class,

        UiContextFxDefault.class,

        // @Mixin's
})

public class CausewayModuleIncViewerJavaFxUi {

}
