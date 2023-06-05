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

import jakarta.annotation.Priority;
import jakarta.inject.Named;

import org.springframework.beans.factory.annotation.Autowired;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.DomainService;
import org.apache.causeway.applib.annotation.NatureOfService;
import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.applib.services.factory.FactoryService;

import demoapp.dom.domain.objects.DomainObjectLayout.bookmarking.DomainObjectLayoutBookmarkingPage;
import demoapp.dom.domain.objects.DomainObjectLayout.cssClass.DomainObjectLayoutCssClassPage;
import demoapp.dom.domain.objects.DomainObjectLayout.cssClassFa.DomainObjectLayoutCssClassFaPage;
import demoapp.dom.domain.objects.DomainObjectLayout.describedAs.DomainObjectLayoutDescribedAsPage;
import demoapp.dom.domain.objects.DomainObjectLayout.named.DomainObjectLayoutNamedPage;
import demoapp.dom.domain.objects.DomainObjectLayout.paged.DomainObjectLayoutPagedPage;
import demoapp.dom.domain.objects.DomainObjectLayout.tabledec.DomainObjectLayoutTableDecoratorPage;
import demoapp.dom.domain.objects.DomainObjectLayout.xxxUiEvent.DomainObjectLayoutXxxUiEventPage;

@Named("demo.DomainObjectLayoutMenu")
@DomainService(nature=NatureOfService.VIEW)
@Priority(PriorityPrecedence.EARLY)
//@Log4j2
public class DomainObjectLayoutMenu {

    @Autowired private FactoryService factoryService;

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(
            cssClassFa="fa-bookmark",
            describedAs = "Add link to object once visited as a bookmark"
    )
    public DomainObjectLayoutBookmarkingPage bookmarking(){
        return factoryService.viewModel(new DomainObjectLayoutBookmarkingPage());
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(
            cssClassFa="fa-pen-nib",
            describedAs = "CSS class to wrap the UI component representing the domain object"
    )
    public DomainObjectLayoutCssClassPage cssClass(){
        return factoryService.viewModel(new DomainObjectLayoutCssClassPage());
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(
            cssClassFa="fa-font-awesome-flag",
            describedAs = "Font awesome icon to represent domain object"
    )
    public DomainObjectLayoutCssClassFaPage cssClassFa(){
        return factoryService.viewModel(new DomainObjectLayoutCssClassFaPage());
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(
            cssClassFa="fa-comment",
            describedAs = "Description of the property, shown as a tooltip"
    )
    public DomainObjectLayoutDescribedAsPage describedAs(){
        return factoryService.viewModel(new DomainObjectLayoutDescribedAsPage());
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(
            cssClassFa="fa-signature",
            describedAs = "Custom text for the domain object's type wherever labeled"
    )
    public DomainObjectLayoutNamedPage named(){
        return factoryService.viewModel(new DomainObjectLayoutNamedPage());
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(
            cssClassFa="fa-fast-forward",
            describedAs = "Number of domain objects per page in collections"
    )
    public DomainObjectLayoutPagedPage paged(){
        return factoryService.viewModel(new DomainObjectLayoutPagedPage());
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(
            cssClassFa="fa-solid fa-table-columns",
            describedAs = "Allows to specify a custom client side table renderer."
    )
    public DomainObjectLayoutTableDecoratorPage tableDecorator(){
        return factoryService.viewModel(new DomainObjectLayoutTableDecoratorPage());
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(
            cssClassFa="fa-desktop",
            describedAs = "Class of the UI events emitted to allow subscribers to specify "
                + "title, icon, style and layout")
    public DomainObjectLayoutXxxUiEventPage uiEvents(){
        return factoryService.viewModel(new DomainObjectLayoutXxxUiEventPage());
    }
}
