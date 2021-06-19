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

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.services.factory.FactoryService;

import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.extern.log4j.Log4j2;

import demoapp.dom.domain.collections.Collection.domainEvent.CollectionDomainEventVm;
import demoapp.dom.domain.collections.Collection.domainEvent.CollectionDomainEventVm_addChild;

@DomainService(
        nature=NatureOfService.VIEW,
        logicalTypeName = "demo.CollectionMenu"
)
@javax.annotation.Priority(PriorityPrecedence.EARLY)
@Log4j2
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class CollectionMenu {

    final FactoryService factoryService;

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-asterisk", describedAs = "Class of the domain event emitted when interacting with the collection")
    public CollectionDomainEventVm domainEvent(){
        val collectionDomainEventVm = new CollectionDomainEventVm();

        addChild(collectionDomainEventVm).act();
        addChild(collectionDomainEventVm).act();
        addChild(collectionDomainEventVm).act();

        return collectionDomainEventVm;
    }



    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-glasses", describedAs = "Visibility of collections")
    public void hidden(){
    }
    public String disableHidden(){
        return "Visibility of collections" +
                 " (not yet implemented in demo)";
    }



    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-shapes", describedAs = "Element type of collections")
    public void typeOf(){
    }
    public String disableTypeOf(){
        return "Element type of collections" +
                 " (not yet implemented in demo)";
    }



    private CollectionDomainEventVm_addChild addChild(CollectionDomainEventVm collectionDomainEventVm) {
        return factoryService.mixin(CollectionDomainEventVm_addChild.class, collectionDomainEventVm);
    }


}
