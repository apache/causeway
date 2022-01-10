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
package demoapp.dom.domain.objects.DomainObjectLayout;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.MemberSupport;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.apache.isis.applib.annotation.SemanticsOf;

@DomainService(
        nature=NatureOfService.VIEW,
        logicalTypeName = "demo.DomainObjectLayoutMenu"
)
@javax.annotation.Priority(PriorityPrecedence.EARLY)
//@Log4j2
public class DomainObjectLayoutMenu {

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-bookmark", describedAs = "Add link to object once visited as a bookmark")
    public void bookmarking(){
    }
    @MemberSupport public String disableBookmarking(){
        return "Add link to object once visited as a bookmark" +
                 " (not yet implemented in demo)";
    }



    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-pen-nib", describedAs = "CSS class to wrap the UI component representing the domain object"
    )
    public void cssClass(){
    }
    @MemberSupport public String disableCssClass(){
        return "CSS class to wrap the UI component representing the domain object" +
                 " (not yet implemented in demo)";
    }



    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-font-awesome-flag", describedAs = "Font awesome icon to represent domain object")
    public void cssClassFa(){
    }
    @MemberSupport public String disableCssClassFa(){
        return "Font awesome icon to represent domain object" +
                " (not yet implemented in demo)";
    }



    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-comment", describedAs = "Description of the property, shown as a tooltip")
    public void describedAs(){
    }
    @MemberSupport public String disableDescribedAs(){
        return "Description of the property, shown as a tooltip" +
                " (not yet implemented in demo)";
    }



    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-signature", describedAs = "Custom text for the domain object's type wherever labelled")
    public void named(){
    }
    @MemberSupport public String disableNamed(){
        return "Custom text for the domain object's type wherever labelled" +
                " (not yet implemented in demo)";
    }



    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-fast-forward", describedAs = "Number of domain objects per page in collections")
    public void paged(){
    }
    @MemberSupport public String disablePaged(){
        return "Number of domain objects per page in collections" +
                " (not yet implemented in demo)";
    }



    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-signature", describedAs = "Overrides plural form for the domain object's type, eg for irregular plurals")
    public void plural(){
    }
    @MemberSupport public String disablePlural(){
        return "Overrides plural form for the domain object's type, eg for irregular plurals" +
                " (not yet implemented in demo)";
    }



    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-desktop", describedAs = "Class of the UI events emitted to allow subscribers to specify title, icon etc")
    public void xxxUiEvent(){
    }
    @MemberSupport public String disableXxxUiEvent(){
        return "Class of the UI events emitted to allow subscribers to specify title, icon etc" +
                " (not yet implemented in demo)";
    }


}
