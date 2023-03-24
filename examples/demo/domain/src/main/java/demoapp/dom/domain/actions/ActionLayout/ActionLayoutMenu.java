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

import jakarta.inject.Named;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.DomainService;
import org.apache.causeway.applib.annotation.NatureOfService;
import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.annotation.SemanticsOf;

import demoapp.dom.domain.actions.ActionLayout.associateWith.ActionLayoutAssociateWithVm;
import demoapp.dom.domain.actions.ActionLayout.cssClass.ActionLayoutCssClassVm;
import demoapp.dom.domain.actions.ActionLayout.cssClassFa.ActionLayoutCssClassFaVm;
import demoapp.dom.domain.actions.ActionLayout.describedAs.ActionLayoutDescribedAsVm;
import demoapp.dom.domain.actions.ActionLayout.fieldSet.ActionLayoutFieldSetVm;
import demoapp.dom.domain.actions.ActionLayout.hidden.ActionLayoutHiddenVm;
import demoapp.dom.domain.actions.ActionLayout.named.ActionLayoutNamedVm;
import demoapp.dom.domain.actions.ActionLayout.position.ActionLayoutPositionVm;
import demoapp.dom.domain.actions.ActionLayout.promptStyle.ActionLayoutPromptStyleVm;
import demoapp.dom.domain.actions.ActionLayout.redirectPolicy.ActionLayoutRedirectPolicyVm;
import demoapp.dom.domain.actions.ActionLayout.sequence.ActionLayoutSequenceVm;

@DomainService(nature=NatureOfService.VIEW)
@Named("demo.ActionLayoutMenu")
@jakarta.annotation.Priority(PriorityPrecedence.EARLY)
//@Log4j2
public class ActionLayoutMenu {

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-solid fa-arrows-left-right",
        describedAs = "Associate an action with a property or collection, specifying its id")
    public ActionLayoutAssociateWithVm associateWith(){
        return new ActionLayoutAssociateWithVm();
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-pen-nib",
        describedAs = "CSS class to wrap the UI component representing this action")
    public ActionLayoutCssClassVm cssClass(){
        return new ActionLayoutCssClassVm();
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-font-awesome-flag",
        describedAs = "Font awesome icon to represent action")
    public ActionLayoutCssClassFaVm cssClassFa(){
        return new ActionLayoutCssClassFaVm();
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-comment",
        describedAs = "Description of the action, shown as a tooltip")
    public ActionLayoutDescribedAsVm describedAs(){
        return new ActionLayoutDescribedAsVm();
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-solid fa-vector-square",
        //TODO[CAUSEWAY-3310] missing description - need to double check how this behaves or should behave
        describedAs = "todo - how does this behave?")
    public ActionLayoutFieldSetVm fieldSet(){
        return new ActionLayoutFieldSetVm();
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-glasses",
        describedAs = "Visibility of the action in different contexts")
    public ActionLayoutHiddenVm hidden(){
        return new ActionLayoutHiddenVm();
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-signature",
        describedAs = "Custom text for the action's label")
    public ActionLayoutNamedVm named(){
        return new ActionLayoutNamedVm();
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-map-pin",
        describedAs = "Position of action buttons")
    public ActionLayoutPositionVm position(){
        return new ActionLayoutPositionVm();
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-question-circle",
        describedAs = "Location and style of action's prompt dialog")
    public ActionLayoutPromptStyleVm promptStyle(){
        return new ActionLayoutPromptStyleVm();
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-random",
        describedAs = "Whether to redraw page if action returns same object")
    public ActionLayoutRedirectPolicyVm redirectPolicy(){
        return new ActionLayoutRedirectPolicyVm();
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-sharp fa-solid fa-sort",
        describedAs = "Order an action relative to other members in the same (layout) group.")
    public ActionLayoutSequenceVm sequence(){
        return new ActionLayoutSequenceVm();
    }

}
