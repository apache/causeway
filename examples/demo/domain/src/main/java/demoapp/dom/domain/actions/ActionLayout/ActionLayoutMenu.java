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

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.apache.isis.applib.annotation.SemanticsOf;

import lombok.extern.log4j.Log4j2;

@DomainService(nature=NatureOfService.VIEW, logicalTypeName = "demo.ActionLayoutMenu")
@javax.annotation.Priority(PriorityPrecedence.EARLY)
@Log4j2
public class ActionLayoutMenu {

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-bookmark", describedAs = "Add link to action prompt as a bookmark"
    )
    public void bookmarking(){
    }
    public String disableBookmarking(){
        return "Add link to action prompt as a bookmark" +
                " (not supported by Wicket viewer)";
    }



    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-pen-nib", describedAs = "CSS class to wrap the UI component representing this action")
    public void cssClass(){
    }
    public String disableCssClass(){
        return "CSS class to wrap the UI component representing this action" +
                " (not yet implemented in demo)";
    }



    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-font-awesome-flag", describedAs = "Font awesome icon to represent action")
    public void cssClassFa(){
    }
    public String disableCssClassFa(){
        return "Font awesome icon to represent action" +
                " (not yet implemented in demo)";
    }




    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-comment", describedAs = "Description of the action, shown as a tooltip")
    public void describedAs(){
    }
    public String disableDescribedAs(){
        return "Description of the action, shown as a tooltip" +
                " (not yet implemented in demo)";
    }



    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-glasses", describedAs = "Visibility of the action in different contexts")
    public void hidden(){
    }
    public String disableHidden(){
        return "Visibility of the action in different contexts" +
                " (not yet implemented in demo)";
    }



    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-signature", describedAs = "Custom text for the action's label")
    public void named(){
    }
    public String disableNamed(){
        return "Custom text for the action's label" +
                " (not yet implemented in demo)";
    }



    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-map-pin", describedAs = "Position of action buttons")
    public demoapp.dom.domain.actions.ActionLayout.position.ActionLayoutPositionVm position(){
        return new demoapp.dom.domain.actions.ActionLayout.position.ActionLayoutPositionVm();
    }



    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-question-circle", describedAs = "Location and style of action's prompt dialog")
    public demoapp.dom.domain.actions.ActionLayout.promptStyle.ActionLayoutPromptStyleVm promptStyle(){
        return new demoapp.dom.domain.actions.ActionLayout.promptStyle.ActionLayoutPromptStyleVm();
    }



    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-random", describedAs = "Whether to redraw page if action returns same object")
    public void redirectPolicy(){
    }
    public String disableRedirectPolicy(){
        return "Whether to redraw page if action returns same object" +
                " (not yet implemented in demo)";
    }


}
