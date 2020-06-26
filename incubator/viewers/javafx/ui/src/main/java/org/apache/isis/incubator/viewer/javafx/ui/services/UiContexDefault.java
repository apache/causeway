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
package org.apache.isis.incubator.viewer.javafx.ui.services;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.isis.incubator.viewer.javafx.model.action.ActionUiModelFactoryFx;
import org.apache.isis.incubator.viewer.javafx.model.context.UiContext;
import org.apache.isis.incubator.viewer.javafx.model.decorator.DecoratorService;
import org.apache.isis.incubator.viewer.javafx.model.icon.IconService;
import org.apache.isis.incubator.viewer.javafx.ui.main.UiActionHandler;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Inject})
@Getter
public class UiContexDefault implements UiContext {
    
    private final IconService iconService;
    private final DecoratorService decoratorService;
    private final UiActionHandler uiActionHandler;
    
    private final ActionUiModelFactoryFx actionUiModelFactory = new ActionUiModelFactoryFx();

    
}
