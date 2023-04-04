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

import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.DomainService;
import org.apache.causeway.applib.annotation.NatureOfService;
import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.annotation.SemanticsOf;

import demoapp.dom._infra.samples.NameSamples;
import demoapp.dom._infra.values.ValueHolderRepository;
import demoapp.dom.domain.actions.Action.associateWith.ActionAssociateWithPage;
import demoapp.dom.domain.actions.Action.associateWith.child.ActionAssociateWithChildVm;
import demoapp.dom.domain.actions.Action.commandPublishing.ActionCommandPublishingEntity;
import demoapp.dom.domain.actions.Action.domainEvent.ActionDomainEventPage;
import demoapp.dom.domain.actions.Action.executionPublishing.ActionExecutionPublishingEntity;
import demoapp.dom.domain.actions.Action.hidden.ActionHiddenPage;
import demoapp.dom.domain.actions.Action.restrictTo.ActionRestrictToPage;
import demoapp.dom.domain.actions.Action.semantics.ActionSemanticsPage;
import demoapp.dom.domain.actions.Action.typeOf.ActionTypeOfPage;
import demoapp.dom.domain.actions.Action.typeOf.child.ActionTypeOfChildVm;
import lombok.RequiredArgsConstructor;
import lombok.val;

@Named("demo.ActionMenu")
@DomainService(nature=NatureOfService.VIEW)
@jakarta.annotation.Priority(PriorityPrecedence.EARLY)
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class ActionMenu {

    final ValueHolderRepository<String, ? extends ActionCommandPublishingEntity> actionCommandEntities;
    final ValueHolderRepository<String, ? extends ActionExecutionPublishingEntity> actionPublishingEntities;
    final NameSamples samples;

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-ring", describedAs = "Semantic relationship between actions and other properties or collections")
    public ActionAssociateWithPage associateWith(){
        val associateWithVm = new ActionAssociateWithPage("value");
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
    public ActionCommandPublishingEntity commandPublishing(){
        return actionCommandEntities.first().orElse(null);
    }


    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-asterisk", describedAs = "Class of the domain event emitted when interacting with the action")
    public ActionDomainEventPage domainEvent(){
        return new ActionDomainEventPage("value");
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-book", describedAs = "Action invocation events as XML")
    public ActionExecutionPublishingEntity executionPublishing(){
        return actionPublishingEntities.first().orElse(null);
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-glasses", describedAs = "Visibility of actions")
    public ActionHiddenPage hidden(){
        return new ActionHiddenPage("value");
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-paper-plane", describedAs = "Availability of actions per environment")
    public ActionRestrictToPage restrictTo(){
        return new ActionRestrictToPage("value");
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-skull-crossbones", describedAs = "Whether the action has side-effects")
    public ActionSemanticsPage semantics(){
        return new ActionSemanticsPage(123);
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-shapes", describedAs = "Semantic relationship between actions and other properties or collections")
    public ActionTypeOfPage typeOf(){
        val typeOfVm = new ActionTypeOfPage();
        val children = typeOfVm.getChildren();

        // add to either one collection or the other
        samples.stream()
                .map(ActionTypeOfChildVm::new)
                .forEach(children::add);
        return typeOfVm;
    }


}
