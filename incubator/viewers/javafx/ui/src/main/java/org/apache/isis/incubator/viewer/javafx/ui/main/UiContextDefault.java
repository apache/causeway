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

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.isis.core.runtime.iactn.IsisInteractionFactory;
import org.apache.isis.incubator.viewer.javafx.model.action.ActionUiModelFactoryFx;
import org.apache.isis.incubator.viewer.javafx.model.context.UiContext;
import org.apache.isis.incubator.viewer.javafx.model.form.FormField;
import org.apache.isis.viewer.common.model.decorator.disable.DisablingDecorator;
import org.apache.isis.viewer.common.model.decorator.icon.IconDecorator;
import org.apache.isis.viewer.common.model.decorator.prototyping.PrototypingDecorator;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Labeled;
import javafx.scene.control.MenuItem;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class UiContextDefault implements UiContext {

    //private final UiActionHandler uiActionHandler;
    @Getter(onMethod_ = {@Override})
    private final IsisInteractionFactory isisInteractionFactory;
    @Getter(onMethod_ = {@Override})
    private final ActionUiModelFactoryFx actionUiModelFactory = new ActionUiModelFactoryFx();
    
    // -- DECORATORS

    @Getter(onMethod_ = {@Override})
    private final IconDecorator<Labeled, Labeled> iconDecoratorForLabeled;
    @Getter(onMethod_ = {@Override})
    private final IconDecorator<MenuItem, MenuItem> iconDecoratorForMenuItem;
   
    @Getter(onMethod_ = {@Override})
    private final DisablingDecorator<Button> disablingDecoratorForButton;
    @Getter(onMethod_ = {@Override})
    private final DisablingDecorator<FormField> disablingDecoratorForFormField;
    
    @Getter(onMethod_ = {@Override})
    private final PrototypingDecorator<Button, Node> prototypingDecoratorForButton;
    @Getter(onMethod_ = {@Override})
    private final PrototypingDecorator<FormField, FormField> prototypingDecoratorForFormField;
    
    
}
