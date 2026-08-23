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
import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.annotation.SemanticsOf;

import lombok.RequiredArgsConstructor;

import demoapp.dom._infra.samples.NameSamples;
import demoapp.dom.domain.actions.Action.choicesFrom.ActionChoicesFromPage;
import demoapp.dom.domain.actions.Action.commandPublishing.ActionCommandPublishingPage;
import demoapp.dom.domain.actions.Action.domainEvent.ActionDomainEventPage;
import demoapp.dom.domain.actions.Action.executionPublishing.ActionExecutionPublishingPage;
import demoapp.dom.domain.actions.Action.restrictTo.ActionRestrictToPage;
import demoapp.dom.domain.actions.Action.semantics.ActionSemanticsPage;
import demoapp.dom.domain.actions.Action.typeOf.ActionTypeOfPage;
import demoapp.dom.domain.actions.Action.typeOf.child.ActionTypeOfChildVm;

@Named("demo.ActionMenu")
@DomainService
@jakarta.annotation.Priority(PriorityPrecedence.EARLY)
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class ActionMenu {

    final NameSamples samples;

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(
            cssClassFa="fa-list-ul",
            describedAs = "Choices for multi-valued parameters taken from corresponding collection (aka \"bulk\" actions)"
    )
    public ActionChoicesFromPage choicesFrom(){
        return new ActionChoicesFromPage();
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(
            cssClassFa="fa-terminal",
            describedAs = "Action invocation intentions as XML"
    )
    public ActionCommandPublishingPage commandPublishing(){
        return new ActionCommandPublishingPage();
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(
            cssClassFa="fa-asterisk",
            describedAs = "Class of the domain event emitted when interacting with the action"
    )
    public ActionDomainEventPage domainEvent(){
        return new ActionDomainEventPage("value");
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(
            cssClassFa="fa-book",
            describedAs = "Action invocation events as XML"
    )
    public ActionExecutionPublishingPage executionPublishing(){
        return new ActionExecutionPublishingPage();
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(
            cssClassFa="fa-paper-plane",
            describedAs = "Availability of actions per environment"
    )
    public ActionRestrictToPage restrictTo(){
        return new ActionRestrictToPage("change me");
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(
            cssClassFa="fa-skull-crossbones",
            describedAs = "Whether the action has side-effects"
    )
    public ActionSemanticsPage semantics(){
        return new ActionSemanticsPage(123);
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(
            cssClassFa="fa-shapes",
            describedAs = "Semantic relationship between actions and other properties or collections"
    )
    public ActionTypeOfPage typeOf(){
        var page = new ActionTypeOfPage();
        samples.stream()
                .map(ActionTypeOfChildVm::new)
                .forEach(e -> page.getChildren().add(e));
        return page;
    }

}
