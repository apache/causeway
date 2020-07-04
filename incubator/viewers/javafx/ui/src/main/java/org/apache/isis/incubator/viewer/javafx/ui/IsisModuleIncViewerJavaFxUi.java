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
package org.apache.isis.incubator.viewer.javafx.ui;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import org.apache.isis.incubator.viewer.javafx.model.events.IsisModuleIncViewerJavaFxModel;
import org.apache.isis.incubator.viewer.javafx.ui.components.PrototypingDecorator;
import org.apache.isis.incubator.viewer.javafx.ui.components.UiComponentFactoryFx;
import org.apache.isis.incubator.viewer.javafx.ui.components.markup.MarkupFieldFactory;
import org.apache.isis.incubator.viewer.javafx.ui.components.other.FallbackFieldFactory;
import org.apache.isis.incubator.viewer.javafx.ui.components.text.TextFieldFactory;
import org.apache.isis.incubator.viewer.javafx.ui.main.UiActionHandler;
import org.apache.isis.incubator.viewer.javafx.ui.main.UiBuilder;
import org.apache.isis.incubator.viewer.javafx.ui.main.UiController;
import org.apache.isis.incubator.viewer.javafx.ui.services.DecoratorServiceDefault;
import org.apache.isis.incubator.viewer.javafx.ui.services.IconServiceDefault;
import org.apache.isis.incubator.viewer.javafx.ui.services.PrototypingInfoService;
import org.apache.isis.incubator.viewer.javafx.ui.services.UiContexDefault;
import org.apache.isis.viewer.common.model.IsisModuleViewerCommon;

/**
 * 
 * @since 2.0
 */
@Configuration
@Import({
        // modules
        IsisModuleViewerCommon.class,
        IsisModuleIncViewerJavaFxModel.class,
        
        // @Components's
        UiBuilder.class,
        UiController.class,
        
        // component factories
        TextFieldFactory.class,
        MarkupFieldFactory.class,
        FallbackFieldFactory.class,
        
        // @Service's
        UiComponentFactoryFx.class,
        UiActionHandler.class,
        PrototypingInfoService.class,
        PrototypingDecorator.class,
        
        UiContexDefault.class,
        IconServiceDefault.class,
        DecoratorServiceDefault.class,

        // @Mixin's
})

public class IsisModuleIncViewerJavaFxUi {

}
