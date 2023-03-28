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
package demoapp.dom.domain.objects.DomainObject;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.DomainService;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.NatureOfService;
import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.annotation.SemanticsOf;

import demoapp.dom.domain.objects.DomainObject.aliased.DomainObjectAliasedVm;
import demoapp.dom.domain.objects.DomainObject.autoComplete.DomainObjectAutoCompleteVm;
import demoapp.dom.domain.objects.DomainObject.bounded.DomainObjectBoundingVm;
import demoapp.dom.domain.objects.DomainObject.editing.DomainObjectEditingVm;
import demoapp.dom.domain.objects.DomainObject.entityChangePublishing.DomainObjectEntityChangePublishingVm;
import demoapp.dom.domain.objects.DomainObject.introspection.DomainObjectIntrospectionVm;
import demoapp.dom.domain.objects.DomainObject.mixinMethod.DomainObjectMixinMethodVm;
import demoapp.dom.domain.objects.DomainObject.nature.viewmodels.jaxbrefentity.StatefulVmJaxbRefsEntity;
import demoapp.dom.domain.objects.DomainObject.nature.viewmodels.usingjaxb.StatefulVmUsingJaxb;
import demoapp.dom.domain.objects.DomainObject.xxxDomainEvent.DomainObjectDomainEventsVm;
import demoapp.dom.domain.objects.DomainObject.xxxLifecycleEvent.DomainObjectLifecyleEventsVm;
import lombok.RequiredArgsConstructor;
import lombok.val;

@Named("demo.DomainObjectMenu")
@DomainService(
        nature=NatureOfService.VIEW
)
@javax.annotation.Priority(PriorityPrecedence.EARLY)
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class DomainObjectMenu {

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-circle", describedAs = "Specify logical type name aliases")
    public DomainObjectAliasedVm aliased() {
        return new DomainObjectAliasedVm();
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-question-circle", describedAs = "Search object in prompt")
    public DomainObjectAutoCompleteVm autoComplete(){
        return new DomainObjectAutoCompleteVm();
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-list-ul", describedAs = "Choose 'reference data' object (one of a bounded set) in prompt")
    public DomainObjectBoundingVm bounding(){
        return new DomainObjectBoundingVm();
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-pencil-alt", describedAs = "Default editability of properties")
    public DomainObjectEditingVm editing() {
        return new DomainObjectEditingVm();
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-book", describedAs = "Entity changed events as XML")
    public DomainObjectEntityChangePublishingVm entityChangePublishing(){
        return new DomainObjectEntityChangePublishingVm();
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-triangle-person-digging", describedAs = "Control over introspection process")
    public DomainObjectIntrospectionVm introspection(){
        return new DomainObjectIntrospectionVm();
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-tools", describedAs = "For mixins, override the default method name")
    public DomainObjectMixinMethodVm mixinMethod() {
        return new DomainObjectMixinMethodVm();
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa = "fa-gamepad", describedAs = "@DomainObject(nature=VIEW_MODEL) for a Stateful View Model")
    public StatefulVmUsingJaxb natureStateful(final String message) {
        val viewModel = new StatefulVmUsingJaxb();
        viewModel.setMessage(message);
        return viewModel;
    }
    @MemberSupport public String default0NatureStateful() {
        return "Some initial state";
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa = "fa-gamepad", describedAs = "@DomainObject(nature=VIEW_MODEL) for a Stateful View Model referencing an entity")
    public StatefulVmJaxbRefsEntity natureStatefulRefsEntity(final String message) {
        val viewModel = new StatefulVmJaxbRefsEntity();
        viewModel.setMessage(message);
        return viewModel;
    }
    @MemberSupport public String default0NatureStatefulRefsEntity() {
        return "Some initial state";
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-asterisk", describedAs = "Default class of the domain event emitted when interacting with the domain object's actions, properties or collections")
    public DomainObjectDomainEventsVm domainEvents() {
        return new DomainObjectDomainEventsVm();
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-redo", describedAs = "Class of the lifecycle event emitted when the domain entity transitions through its persistence lifecycle")
    public DomainObjectLifecyleEventsVm lifecycleEvents() {
        return new DomainObjectLifecyleEventsVm();
    }

}
