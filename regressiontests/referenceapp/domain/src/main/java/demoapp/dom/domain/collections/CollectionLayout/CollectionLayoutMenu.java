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

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.DomainService;
import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.annotation.SemanticsOf;

import demoapp.dom._infra.samples.NameSamples;
import demoapp.dom.domain.collections.CollectionLayout.cssClass.CollectionLayoutCssClassPage;
import demoapp.dom.domain.collections.CollectionLayout.cssClass.child.CollectionLayoutCssClassChildVm;
import demoapp.dom.domain.collections.CollectionLayout.defaultView.CollectionLayoutDefaultViewPage;
import demoapp.dom.domain.collections.CollectionLayout.defaultView.child.CollectionLayoutDefaultViewChildVm;
import demoapp.dom.domain.collections.CollectionLayout.describedAs.CollectionLayoutDescribedAsPage;
import demoapp.dom.domain.collections.CollectionLayout.describedAs.child.CollectionLayoutDescribedAsChildVm;
import demoapp.dom.domain.collections.CollectionLayout.hidden.CollectionLayoutHiddenPage;
import demoapp.dom.domain.collections.CollectionLayout.hidden.child.CollectionLayoutHiddenChildVm;
import demoapp.dom.domain.collections.CollectionLayout.named.CollectionLayoutNamedPage;
import demoapp.dom.domain.collections.CollectionLayout.named.child.CollectionLayoutNamedChildVm;
import demoapp.dom.domain.collections.CollectionLayout.paged.CollectionLayoutPagedPage;
import demoapp.dom.domain.collections.CollectionLayout.paged.child.CollectionLayoutPagedChildVm;
import demoapp.dom.domain.collections.CollectionLayout.sequence.CollectionLayoutSequencePage;
import demoapp.dom.domain.collections.CollectionLayout.sequence.child.CollectionLayoutSequenceChildVm;
import demoapp.dom.domain.collections.CollectionLayout.sortedBy.CollectionLayoutSortedByPage;
import demoapp.dom.domain.collections.CollectionLayout.sortedBy.child.CollectionLayoutSortedByChildVm;
import demoapp.dom.domain.collections.CollectionLayout.tabledec.CollectionLayoutTableDecoratorPage;
import demoapp.dom.domain.collections.CollectionLayout.tabledec.child.CollectionLayoutTableDecoratorChildVm;
import lombok.RequiredArgsConstructor;

@Named("demo.CollectionLayoutMenu")
@DomainService
@Priority(PriorityPrecedence.EARLY)
@RequiredArgsConstructor(onConstructor_ = {@Inject})
//@Log4j2
public class CollectionLayoutMenu {

    final NameSamples samples;

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(
            cssClassFa="fa-pen-nib",
            describedAs = "CSS class to wrap the UI component representing this collection"
    )
    public CollectionLayoutCssClassPage cssClass(){
        var page = new CollectionLayoutCssClassPage();
        samples.stream()
                .map(CollectionLayoutCssClassChildVm::new
                )
                .forEach(e -> page.getChildren().add(e));
        samples.stream()
                .map(CollectionLayoutCssClassChildVm::new)
                .forEach(e -> page.getMoreChildren().add(e));

        return page;
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(
            cssClassFa="fa-atom",
            describedAs = "View collection as a table, or collapsed, or some other representation if available"
    )
    public CollectionLayoutDefaultViewPage defaultView(){
        var page = new CollectionLayoutDefaultViewPage();
        samples.stream()
                .map(CollectionLayoutDefaultViewChildVm::new)
                .forEach(e -> page.getChildren().add(e));
        samples.stream()
                .map(CollectionLayoutDefaultViewChildVm::new)
                .forEach(e -> page.getMoreChildren().add(e));
        samples.stream()
                .map(CollectionLayoutDefaultViewChildVm::new)
                .forEach(e -> page.getYetMoreChildren().add(e));
        return page;
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(
            cssClassFa="fa-comment",
            describedAs = "Description of the collection, shown as a tooltip"
    )
    public CollectionLayoutDescribedAsPage describedAs(){
        var page = new CollectionLayoutDescribedAsPage();
        samples.stream()
                .map(CollectionLayoutDescribedAsChildVm::new)
                .forEach(e -> page.getChildren().add(e));
        samples.stream()
                .map(CollectionLayoutDescribedAsChildVm::new)
                .forEach(e -> page.getMoreChildren().add(e));
        return page;
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(
            cssClassFa="fa-glasses",
            describedAs = "Visibility of the collection in different contexts"
    )
    public CollectionLayoutHiddenPage hidden(){
        var page = new CollectionLayoutHiddenPage();
        samples.stream()
                .map(CollectionLayoutHiddenChildVm::new)
                .forEach(e -> page.getChildren().add(e));
        return page;
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(
            cssClassFa="fa-signature",
            describedAs = "Custom text for the collection's label"
    )
    public CollectionLayoutNamedPage named(){
        var page = new CollectionLayoutNamedPage();
        samples.stream()
                .map(CollectionLayoutNamedChildVm::new)
                .forEach(e -> page.getChildren().add(e));
        return page;
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(
            cssClassFa="fa-fast-forward",
            describedAs = "Number of domain objects per page in this collection"
    )
    public CollectionLayoutPagedPage paged(){
        var page = new CollectionLayoutPagedPage();
        samples.stream()
                .map(CollectionLayoutPagedChildVm::new)
                .forEach(e -> page.getChildren().add(e));
        samples.stream()
                .map(CollectionLayoutPagedChildVm::new)
                .forEach(e -> page.getMoreChildren().add(e));
        return page;
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(
            cssClassFa="fa-sharp fa-solid fa-sort",
            describedAs = "Order of this member relative to other members in the same (layout) group."
    )
    public CollectionLayoutSequencePage sequence(){
        var page = new CollectionLayoutSequencePage();
        samples.stream()
                .map(CollectionLayoutSequenceChildVm::new)
                .forEach(e -> page.getChildren().add(e));
        return page;
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(
            cssClassFa="fa-solid fa-arrow-down-a-z",
            describedAs = "Sort domain objects in this collection, overriding their default comparator"
    )
    public CollectionLayoutSortedByPage sortedBy(){
        var page = new CollectionLayoutSortedByPage();
        samples.stream()
                .map(CollectionLayoutSortedByChildVm::new)
                .forEach(e -> page.getChildren().add(e));
        return page;

    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(
            cssClassFa="fa-solid fa-table-columns",
            describedAs = "Allows to specify a custom client side table renderer."
    )
    public CollectionLayoutTableDecoratorPage tableDecorator(){
        var page = new CollectionLayoutTableDecoratorPage();
        samples.stream()
                .map(CollectionLayoutTableDecoratorChildVm::new)
                .forEach(e -> page.getChildren().add(e));
        return page;

    }
}
