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
package org.apache.isis.incubator.viewer.vaadin.ui.pages.main;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.inject.Inject;

import com.vaadin.flow.component.Component;

import org.springframework.stereotype.Service;

import org.apache.isis.core.interaction.session.InteractionFactory;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.incubator.viewer.vaadin.model.context.UiContextVaa;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.val;
import lombok.extern.log4j.Log4j2;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Inject})
@Log4j2
public class UiContextVaaDefault implements UiContextVaa {

//    @Getter(onMethod_ = {@Override})
//    private final JavaFxViewerConfig javaFxViewerConfig;
    @Getter(onMethod_ = {@Override})
    private final InteractionFactory isisInteractionFactory;
//    @Getter(onMethod_ = {@Override})
//    private final ActionUiModelFactoryFx actionUiModelFactory = new ActionUiModelFactoryFx();

    @Setter(onMethod_ = {@Override})
    private Consumer<Component> newPageHandler;

    @Setter(onMethod_ = {@Override})
    private Function<ManagedObject, Component> pageFactory;

    @Override
    public void route(ManagedObject object) {
        log.info("about to render object {}", object);
        newPage(pageFor(object));
    }

    @Override
    public void route(Supplier<ManagedObject> objectSupplier) {
        isisInteractionFactory.runAnonymous(()->{
            val object = objectSupplier.get();
            route(object);
        });
    }

    // -- DECORATORS

//    @Getter(onMethod_ = {@Override})
//    private final IconDecorator<Labeled, Labeled> iconDecoratorForLabeled;
//    @Getter(onMethod_ = {@Override})
//    private final IconDecorator<MenuItem, MenuItem> iconDecoratorForMenuItem;
//
//    @Getter(onMethod_ = {@Override})
//    private final DisablingDecorator<Button> disablingDecoratorForButton;
//    @Getter(onMethod_ = {@Override})
//    private final DisablingDecorator<Node> disablingDecoratorForFormField;
//
//    @Getter(onMethod_ = {@Override})
//    private final PrototypingDecorator<Button, Node> prototypingDecoratorForButton;
//    @Getter(onMethod_ = {@Override})
//    private final PrototypingDecorator<Node, Node> prototypingDecoratorForFormField;

    // -- HELPER

    private void newPage(Component content) {
        if(newPageHandler!=null && content!=null) {
            newPageHandler.accept(content);
        }
    }

    private Component pageFor(ManagedObject object) {
        return pageFactory!=null
                ? pageFactory.apply(object)
                : null;
    }

}
