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
package org.apache.isis.incubator.viewer.vaadin.model.context;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.vaadin.flow.component.Component;

import org.apache.isis.applib.services.iactnlayer.InteractionService;
import org.apache.isis.core.metamodel.spec.ManagedObject;

public interface UiContextVaa {

    //JavaFxViewerConfig getJavaFxViewerConfig();

    InteractionService getInteractionService();
    //ActionUiModelFactoryFx getActionUiModelFactory();

    void setNewPageHandler(Consumer<Component> onNewPage);
    void setPageFactory(Function<ManagedObject, Component> pageFactory);

    void route(ManagedObject object);
    void route(Supplier<ManagedObject> objectSupplier);

    // -- DECORATORS

//    IconDecorator<Labeled, Labeled> getIconDecoratorForLabeled();
//    IconDecorator<MenuItem, MenuItem> getIconDecoratorForMenuItem();
//
//    DisablingDecorator<Button> getDisablingDecoratorForButton();
//    DisablingDecorator<Node> getDisablingDecoratorForFormField();
//
//    PrototypingDecorator<Button, Node> getPrototypingDecoratorForButton();
//    PrototypingDecorator<Node, Node> getPrototypingDecoratorForFormField();



}
