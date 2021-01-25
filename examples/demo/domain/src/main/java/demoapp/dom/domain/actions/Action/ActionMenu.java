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
package demoapp.dom.domain.actions.Action;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.SemanticsOf;

import lombok.RequiredArgsConstructor;
import lombok.val;

import demoapp.dom._infra.samples.NameSamples;
import demoapp.dom.domain.actions.Action.associateWith.ActionAssociateWithVm;
import demoapp.dom.domain.actions.Action.associateWith.child.ActionAssociateWithChildVm;
import demoapp.dom.domain.actions.Action.commandPublishing.ActionCommandPublishingJdo;
import demoapp.dom.domain.actions.Action.commandPublishing.ActionCommandPublishingJdoEntities;
import demoapp.dom.domain.actions.Action.domainEvent.ActionDomainEventVm;
import demoapp.dom.domain.actions.Action.executionPublishing.ActionExecutionPublishingJdo;
import demoapp.dom.domain.actions.Action.executionPublishing.ActionExecutionPublishingJdoEntities;
import demoapp.dom.domain.actions.Action.hidden.ActionHiddenVm;
import demoapp.dom.domain.actions.Action.restrictTo.ActionRestrictToVm;
import demoapp.dom.domain.actions.Action.semantics.ActionSemanticsVm;
import demoapp.dom.domain.actions.Action.typeOf.ActionTypeOfVm;
import demoapp.dom.domain.actions.Action.typeOf.child.ActionTypeOfChildVm;

@DomainService(nature=NatureOfService.VIEW, objectType = "demo.ActionMenu")
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class ActionMenu {

    final ActionCommandPublishingJdoEntities actionCommandJdoEntities;
    final ActionExecutionPublishingJdoEntities actionPublishingJdoEntities;
    final NameSamples samples;

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
    @ActionLayout(cssClassFa="fa-terminal", describedAs = "Action invocation intentions as XML")
    public ActionCommandPublishingJdo commandPublishing(){
        return actionCommandJdoEntities.first();
    }


    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-asterisk", describedAs = "Class of the domain event emitted when interacting with the action")
    public ActionDomainEventVm domainEvent(){
        return new ActionDomainEventVm("value");
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-book", describedAs = "Action invocation events as XML")
    public ActionExecutionPublishingJdo executionPublishing(){
        return actionPublishingJdoEntities.first();
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-glasses", describedAs = "Visibility of actions")
    public ActionHiddenVm hidden(){
        return new ActionHiddenVm("value");
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-paper-plane", describedAs = "Availability of actions per environment")
    public ActionRestrictToVm restrictTo(){
        return new ActionRestrictToVm("value");
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-skull-crossbones", describedAs = "Whether the action has side-effects")
    public ActionSemanticsVm semantics(){
        return new ActionSemanticsVm(123);
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-shapes", describedAs = "Semantic relationship between actions and other properties or collections")
    public ActionTypeOfVm typeOf(){
        val typeOfVm = new ActionTypeOfVm();
        val children = typeOfVm.getChildren();

        // add to either one collection or the other
        samples.stream()
                .map(ActionTypeOfChildVm::new)
                .forEach(children::add);
        return typeOfVm;
    }


}
