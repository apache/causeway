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
package demoapp.dom.domain.actions.ActionLayout;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.DomainService;
import org.apache.causeway.applib.annotation.NatureOfService;
import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.annotation.SemanticsOf;

import lombok.RequiredArgsConstructor;
import lombok.val;

import demoapp.dom._infra.samples.NameSamples;
import demoapp.dom.domain.actions.ActionLayout.associateWith.ActionLayoutAssociateWithPage;
import demoapp.dom.domain.actions.ActionLayout.associateWith.child.ActionLayoutAssociateWithChildVm;
import demoapp.dom.domain.actions.ActionLayout.cssClass.ActionLayoutCssClassPage;
import demoapp.dom.domain.actions.ActionLayout.cssClassFa.ActionLayoutCssClassFaPage;
import demoapp.dom.domain.actions.ActionLayout.describedAs.ActionLayoutDescribedAsPage;
import demoapp.dom.domain.actions.ActionLayout.fieldSet.ActionLayoutFieldSetPage;
import demoapp.dom.domain.actions.ActionLayout.hidden.ActionLayoutHiddenPage;
import demoapp.dom.domain.actions.ActionLayout.named.ActionLayoutNamedPage;
import demoapp.dom.domain.actions.ActionLayout.position.ActionLayoutPositionPage;
import demoapp.dom.domain.actions.ActionLayout.promptStyle.ActionLayoutPromptStylePage;
import demoapp.dom.domain.actions.ActionLayout.redirectPolicy.ActionLayoutRedirectPolicyPage;
import demoapp.dom.domain.actions.ActionLayout.sequence.ActionLayoutSequencePage;

@DomainService(nature=NatureOfService.VIEW)
@Named("demo.ActionLayoutMenu")
@javax.annotation.Priority(PriorityPrecedence.EARLY)
@RequiredArgsConstructor(onConstructor_ = {@Inject})
//tag::menu-item-described-as[]
public class ActionLayoutMenu {
    //...

//end::menu-item-described-as[]

    final NameSamples samples;

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(
            cssClassFa="fa-solid fa-arrows-left-right",
            describedAs = "Associate an action with a property or collection, specifying its id"
    )
    public ActionLayoutAssociateWithPage associateWith(){
        val page = new ActionLayoutAssociateWithPage();
        samples.stream()
                .map(ActionLayoutAssociateWithChildVm::new)
                .forEach(e -> page.getChildren().add(e));
        return page;
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(
            cssClassFa="fa-pen-nib",
            describedAs = "CSS class to wrap the UI component representing this action"
    )
    public ActionLayoutCssClassPage cssClass(){
        return new ActionLayoutCssClassPage();
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(
            cssClassFa="fa-font-awesome-flag",
            describedAs = "Font awesome icon to represent action"
    )
    public ActionLayoutCssClassFaPage cssClassFa(){
        return new ActionLayoutCssClassFaPage();
    }

//tag::menu-item-described-as[]
    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(
        cssClassFa="fa-comment",
        describedAs = "Description of the action, shown as a tooltip"
    )
    public ActionLayoutDescribedAsPage describedAs(){
        return new ActionLayoutDescribedAsPage();
    }
//end::menu-item-described-as[]

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(
            cssClassFa="fa-solid fa-vector-square",
            describedAs = "Associates an action with a fieldset"
    )
    public ActionLayoutFieldSetPage fieldSet(){
        return new ActionLayoutFieldSetPage();
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(
            cssClassFa="fa-glasses",
            describedAs = "Visibility of the action in different contexts"
    )
    public ActionLayoutHiddenPage hidden(){
        return new ActionLayoutHiddenPage();
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(
            cssClassFa="fa-signature",
            describedAs = "Custom text for the action's label"
    )
    public ActionLayoutNamedPage named(){
        return new ActionLayoutNamedPage();
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(
            cssClassFa="fa-map-pin",
            describedAs = "Position of action buttons"
    )
    public ActionLayoutPositionPage position(){
        return new ActionLayoutPositionPage();
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(
            cssClassFa="fa-question-circle",
            describedAs = "Location and style of action's prompt dialog"
    )
    public ActionLayoutPromptStylePage promptStyle(){
        return new ActionLayoutPromptStylePage();
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(
            cssClassFa="fa-random",
            describedAs = "Whether to redraw page if action returns same object"
    )
    public ActionLayoutRedirectPolicyPage redirectPolicy(){
        return new ActionLayoutRedirectPolicyPage();
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(
            cssClassFa="fa-sharp fa-solid fa-sort",
            describedAs = "Order an action relative to other members in the same (layout) group."
    )
    public ActionLayoutSequencePage sequence(){
        return new ActionLayoutSequencePage();
    }

//tag::menu-item-described-as[]
}
//end::menu-item-described-as[]
