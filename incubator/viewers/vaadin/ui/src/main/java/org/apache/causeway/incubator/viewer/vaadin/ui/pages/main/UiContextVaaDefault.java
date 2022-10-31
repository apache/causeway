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
package org.apache.causeway.incubator.viewer.vaadin.ui.pages.main;

import java.util.Optional;
import java.util.function.Consumer;

import javax.inject.Inject;

import com.vaadin.flow.component.Component;

import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.services.iactnlayer.InteractionService;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.metamodel.interactions.managed.ManagedAction;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.incubator.viewer.vaadin.model.context.MemberInvocationHandler;
import org.apache.causeway.incubator.viewer.vaadin.model.context.UiContextVaa;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Inject})
@Log4j2
public class UiContextVaaDefault implements UiContextVaa {

    @Getter(onMethod_ = {@Override})
    private final InteractionService interactionService;

    @Setter(onMethod_ = {@Override})
    private Consumer<Component> newPageHandler;

    @Setter(onMethod_ = {@Override})
    private MemberInvocationHandler<Component> pageFactory;

    // might not be initialized yet
    private Optional<MemberInvocationHandler<Component>> pageFactory() {
        return Optional.ofNullable(pageFactory);
    }

    @Override
    public void route(final ManagedObject object) {
        log.info("about to render object {}", object);
        newPage(pageFactory()
                .map(pageFactory->pageFactory.handle(object))
                .orElse(null));
    }

    @Override
    public void route(final ManagedAction managedAction, final Can<ManagedObject> params, final ManagedObject actionResult) {
        log.info("about to render object {}", actionResult);
        newPage(pageFactory()
                .map(pageFactory->pageFactory.handle(managedAction, params, actionResult))
                .orElse(null));
    }

//    public void route(final Supplier<ManagedObject> objectSupplier) {
//        interactionService.runAnonymous(()->{
//            var object = objectSupplier.get();
//            route(object);
//        });
//    }

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

    private void newPage(final @Nullable Component content) {
        if(newPageHandler!=null && content!=null) {
            newPageHandler.accept(content);
        }
    }

}
