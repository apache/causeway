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
package demoapp.dom.domain.objects.DomainObject;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.DomainService;
import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.annotation.SemanticsOf;

import demoapp.dom.domain.objects.DomainObject.aliased.DomainObjectAliasedPage;
import demoapp.dom.domain.objects.DomainObject.autoComplete.DomainObjectAutoCompletePage;
import demoapp.dom.domain.objects.DomainObject.bounded.DomainObjectBoundingPage;
import demoapp.dom.domain.objects.DomainObject.editing.DomainObjectEditingPage;
import demoapp.dom.domain.objects.DomainObject.entityChangePublishing.DomainObjectEntityChangePublishingPage;
import demoapp.dom.domain.objects.DomainObject.introspection.DomainObjectIntrospectionPage;
import demoapp.dom.domain.objects.DomainObject.mixinMethod.DomainObjectMixinMethodPage;
import demoapp.dom.domain.objects.DomainObject.nature.DomainObjectNaturePage;
import demoapp.dom.domain.objects.DomainObject.xxxDomainEvent.DomainObjectXxxDomainEventPage;
import demoapp.dom.domain.objects.DomainObject.xxxLifecycleEvent.DomainObjectXxxLifecycleEventPage;
import lombok.RequiredArgsConstructor;

@Named("demo.DomainObjectMenu")
@DomainService
@Priority(PriorityPrecedence.EARLY)
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class DomainObjectMenu {

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(
            cssClassFa="fa-circle",
            describedAs = "Specify logical type name aliases"
    )
    public DomainObjectAliasedPage aliased() {
        return new DomainObjectAliasedPage();
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(
            cssClassFa="fa-question-circle",
            describedAs = "Search object in prompt"
    )
    public DomainObjectAutoCompletePage autoComplete(){
        return new DomainObjectAutoCompletePage();
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(
            cssClassFa="fa-list-ul",
            describedAs = "Choose 'reference data' object (one of a bounded set) in prompt"
    )
    public DomainObjectBoundingPage bounding(){
        return new DomainObjectBoundingPage();
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(
            cssClassFa="fa-pencil-alt",
            describedAs = "Default editability of properties"
    )
    public DomainObjectEditingPage editing() {
        return new DomainObjectEditingPage();
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(
            cssClassFa="fa-book",
            describedAs = "Entity changed events as XML"
    )
    public DomainObjectEntityChangePublishingPage entityChangePublishing(){
        return new DomainObjectEntityChangePublishingPage();
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(
            cssClassFa="fa-pen-ruler",
            describedAs = "Control over introspection process"
    )
    public DomainObjectIntrospectionPage introspection(){
        return new DomainObjectIntrospectionPage();
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(
            cssClassFa="fa-mortar-pestle",
            describedAs = "For mixins, override the default method name"
    )
    public DomainObjectMixinMethodPage mixinMethod() {
        return new DomainObjectMixinMethodPage();
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(
            cssClassFa = "fa-gamepad",
            describedAs = "@DomainObject(nature=VIEW_MODEL) for a Stateful View Model"
    )
    public DomainObjectNaturePage nature() {
        return new DomainObjectNaturePage();
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(
            cssClassFa="fa-asterisk",
            describedAs = "Default class of the domain event emitted when interacting with the domain object's actions, properties or collections"
    )
    public DomainObjectXxxDomainEventPage domainEvents() {
        var page = new DomainObjectXxxDomainEventPage("change me");
        page.addChild("#1");
        page.addChild("#2");
        page.addChild("#3");
        return page;
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(
            cssClassFa="fa-redo",
            describedAs = "Class of the lifecycle event emitted when the domain entity transitions through its persistence lifecycle"
    )
    public DomainObjectXxxLifecycleEventPage lifecycleEvents() {
        return new DomainObjectXxxLifecycleEventPage();
    }

}
