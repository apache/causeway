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
package demoapp.dom.domain.collections.CollectionLayout;

import javax.inject.Named;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.DomainService;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.NatureOfService;
import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.annotation.SemanticsOf;

@Named("demo.CollectionLayoutMenu")
@DomainService(
        nature=NatureOfService.VIEW
)
@javax.annotation.Priority(PriorityPrecedence.EARLY)
//@Log4j2
public class CollectionLayoutMenu {

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-pen-nib", describedAs = "CSS class to wrap the UI component representing this collection")
    public void cssClass(){
    }
    @MemberSupport public String disableCssClass(){
        return "CSS class to wrap the UI component representing this collection" +
                " (not yet implemented in demo)";
    }



    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-atom", describedAs = "View collection as a table, or collapsed, or some other representation if available")
    public void defaultView(){
    }
    @MemberSupport public String disableDefaultView(){
        return "View collection as a table, or collapsed, or some other representation if available" +
                " (not yet implemented in demo)";
    }



    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-comment", describedAs = "Description of the collection, shown as a tooltip")
    public void describedAs(){
    }
    @MemberSupport public String disableDescribedAs(){
        return "Description of the collection, shown as a tooltip" +
                " (not yet implemented in demo)";
    }



    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-glasses", describedAs = "Visibility of the collection in different contexts")
    public void hidden(){
    }
    @MemberSupport public String disableHidden(){
        return "Visibility of the collection in different contexts" +
                " (not yet implemented in demo)";
    }



    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-signature", describedAs = "Custom text for the collection's label")
    public void named(){
    }
    @MemberSupport public String disableNamed(){
        return "Custom text for the collection's label" +
                " (not yet implemented in demo)";
    }


    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-fast-forward", describedAs = "Number of domain objects per page in this collection")
    public void paged(){
    }
    @MemberSupport public String disablePaged(){
        return "Number of domain objects per page in this collection" +
                " (not yet implemented in demo)";
    }



    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-sort", describedAs = "Sort domain objects in this collection, overriding their default comparator")
    public void sortedBy(){
    }
    @MemberSupport public String disableSortedBy(){
        return "Sort domain objects in this collection, overriding their default comparator" +
                " (not yet implemented in demo)";
    }

}
