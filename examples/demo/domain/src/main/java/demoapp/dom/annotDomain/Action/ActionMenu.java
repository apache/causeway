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
package demoapp.dom.annotDomain.Action;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.SemanticsOf;

import lombok.val;
import lombok.extern.log4j.Log4j2;

import demoapp.dom._infra.samples.NameSamples;
import demoapp.dom.annotDomain.Action.associateWith.ActionAssociateWithVm;
import demoapp.dom.annotDomain.Action.associateWith.child.ActionAssociateWithChildVm;
import demoapp.dom.annotDomain.Action.domainEvent.ActionDomainEventVm;
import demoapp.dom.annotDomain.Action.hidden.ActionHiddenVm;
import demoapp.dom.annotDomain.Action.publishing.ActionPublishingJdo;
import demoapp.dom.annotDomain.Action.publishing.ActionPublishingJdoEntities;
import demoapp.dom.annotDomain.Action.typeOf.ActionTypeOfVm;
import demoapp.dom.annotDomain.Action.typeOf.child.ActionTypeOfChildVm;
import demoapp.dom.annotDomain.Property.publishing.PropertyPublishingJdo;
import demoapp.dom.types.Samples;

@DomainService(nature=NatureOfService.VIEW, objectType = "demo.ActionMenu")
@Log4j2
public class ActionMenu {

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-ring", describedAs = "Semantic relationship between actions and other properties or collections")
    public ActionAssociateWithVm associateWith(){
        val associateWithVm = new ActionAssociateWithVm("value");
        val children = associateWithVm.getChildren();
        val favorites = associateWithVm.getFavorites();

        // add to either one collection or the other
        final boolean[] which = {false};
        samples.stream()
                .map(ActionAssociateWithChildVm::new)
                .forEach(e -> {
                    (which[0] ? children : favorites).add(e);
                    which[0] = !which[0];
                });
        return associateWithVm;
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-asterisk", describedAs = "Decouples interaction of actions")
    public ActionDomainEventVm domainEvent(){
        return new ActionDomainEventVm("value");
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-glasses", describedAs = "Visibility of actions")
    public ActionHiddenVm hidden(){
        return new ActionHiddenVm("value");
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-book", describedAs = "Property changed events as XML")
    public ActionPublishingJdo publishing(){
        return actionPublishingJdoEntities.first();
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-square", describedAs = "Semantic relationship between actions and other properties or collections")
    public ActionTypeOfVm typeOf(){
        val typeOfVm = new ActionTypeOfVm();
        val children = typeOfVm.getChildren();

        // add to either one collection or the other
        samples.stream()
                .map(ActionTypeOfChildVm::new)
                .forEach(children::add);
        return typeOfVm;
    }

    @Inject
    ActionPublishingJdoEntities actionPublishingJdoEntities;

    @Inject
    NameSamples samples;

}
