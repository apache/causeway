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
package org.apache.isis.extensions.secman.api.tenancy.menu;

import java.util.Collection;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.DomainServiceLayout;
import org.apache.isis.applib.annotation.MinLength;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Optionality;
import org.apache.isis.applib.annotation.Parameter;
import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.applib.annotation.RestrictTo;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.extensions.secman.api.IsisModuleExtSecmanApi;
import org.apache.isis.extensions.secman.api.tenancy.dom.ApplicationTenancy;
import org.apache.isis.extensions.secman.api.tenancy.dom.ApplicationTenancyRepository;

@DomainService(
        nature = NatureOfService.VIEW,
        objectType = "isis.ext.secman.ApplicationTenancyMenu"
        )
@DomainServiceLayout(
        named = "Security",
        menuBar = DomainServiceLayout.MenuBar.SECONDARY
        )
public class ApplicationTenancyMenu {

    // -- domain event classes
    public static abstract class PropertyDomainEvent<T> extends IsisModuleExtSecmanApi.PropertyDomainEvent<ApplicationTenancyMenu, T> {}
    public static abstract class CollectionDomainEvent<T> extends IsisModuleExtSecmanApi.CollectionDomainEvent<ApplicationTenancyMenu, T> {}
    public static abstract class ActionDomainEvent extends IsisModuleExtSecmanApi.ActionDomainEvent<ApplicationTenancyMenu> {}

    @Inject private ApplicationTenancyRepository applicationTenancyRepository;

    // -- iconName
    public String iconName() {
        return "applicationTenancy";
    }


    // -- findTenancies
    public static class FindTenanciesDomainEvent extends ActionDomainEvent {}

    @Action(
            domainEvent = FindTenanciesDomainEvent.class,
            semantics = SemanticsOf.SAFE
            )
    @ActionLayout(sequence = "100.30.1")
    public Collection<? extends ApplicationTenancy> findTenancies(
            @Parameter(optionality = Optionality.OPTIONAL)
            @ParameterLayout(named = "Partial Name Or Path", describedAs = "String to search for, wildcard (*) can be used")
            @MinLength(1) // for auto-complete
            final String partialNameOrPath) {
        return applicationTenancyRepository.findByNameOrPathMatchingCached(partialNameOrPath);
    }


    // -- newTenancy
    public static class NewTenancyDomainEvent extends ActionDomainEvent {}

    @Action(
            domainEvent = NewTenancyDomainEvent.class,
            semantics = SemanticsOf.IDEMPOTENT
            )
    @ActionLayout(sequence = "100.30.3")
    public ApplicationTenancy newTenancy(
            @Parameter(maxLength = ApplicationTenancy.MAX_LENGTH_NAME)
            @ParameterLayout(named = "Name", typicalLength = ApplicationTenancy.TYPICAL_LENGTH_NAME)
            final String name,
            @Parameter(maxLength = ApplicationTenancy.MAX_LENGTH_PATH)
            @ParameterLayout(named = "Path")
            final String path,
            @Parameter(optionality = Optionality.OPTIONAL)
            @ParameterLayout(named = "Parent")
            final ApplicationTenancy parent) {
        return applicationTenancyRepository.newTenancy(name, path, parent);
    }


    // -- allTenancies
    public static class AllTenanciesDomainEvent extends ActionDomainEvent {}

    @Action(
            domainEvent = AllTenanciesDomainEvent.class,
            semantics = SemanticsOf.SAFE,
            restrictTo = RestrictTo.PROTOTYPING
            )
    @ActionLayout(sequence = "100.30.4")
    public Collection<? extends ApplicationTenancy> allTenancies() {
        return applicationTenancyRepository.allTenancies();
    }


}
