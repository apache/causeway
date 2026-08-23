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
package demoapp.dom.domain.collections.Collection;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.DomainService;
import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.applib.services.factory.FactoryService;

import demoapp.dom._infra.samples.NameSamples;
import demoapp.dom.domain.collections.Collection.domainEvent.CollectionDomainEventPage;
import demoapp.dom.domain.collections.Collection.typeOf.CollectionTypeOfPage;
import demoapp.dom.domain.collections.Collection.typeOf.child.CollectionTypeOfChildVm;
import lombok.RequiredArgsConstructor;

@Named("demo.CollectionMenu")
@DomainService
@Priority(PriorityPrecedence.EARLY)
@RequiredArgsConstructor(onConstructor_ = {@Inject})
//@Log4j2
class CollectionMenu {

    final FactoryService factoryService;

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(
            cssClassFa="fa-asterisk",
            describedAs = "Class of the domain event emitted when interacting with the collection"
    )
    public CollectionDomainEventPage domainEvent(){
        var page = new CollectionDomainEventPage();
        samples.stream()
                .forEach(page::addChild);
        samples.stream()
                .forEach(page::addOtherChild);
        return page;
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(
            cssClassFa="fa-shapes",
            describedAs = "Element type of collections"
    )
    public CollectionTypeOfPage typeOf(){
        var page = new CollectionTypeOfPage();
        samples.stream()
                .map(CollectionTypeOfChildVm::new)
                .forEach(e -> page.getChildren().add(e));
        samples.stream()
                .map(CollectionTypeOfChildVm::new)
                .forEach(e -> page.getOtherChildren().add(e));
        return page;
    }

    final NameSamples samples;

}
