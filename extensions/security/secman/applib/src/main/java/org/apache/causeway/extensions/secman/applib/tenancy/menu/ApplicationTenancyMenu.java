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
package org.apache.causeway.extensions.secman.applib.tenancy.menu;

import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.DomainService;
import org.apache.causeway.applib.annotation.DomainServiceLayout;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.MinLength;
import org.apache.causeway.applib.annotation.NatureOfService;
import org.apache.causeway.applib.annotation.ObjectSupport;
import org.apache.causeway.applib.annotation.Optionality;
import org.apache.causeway.applib.annotation.Parameter;
import org.apache.causeway.applib.annotation.ParameterLayout;
import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.applib.services.factory.FactoryService;
import org.apache.causeway.extensions.secman.applib.CausewayModuleExtSecmanApplib;
import org.apache.causeway.extensions.secman.applib.tenancy.dom.ApplicationTenancy;
import org.apache.causeway.extensions.secman.applib.tenancy.dom.ApplicationTenancyRepository;
import org.apache.causeway.extensions.secman.applib.tenancy.man.ApplicationTenancyManager;

/**
 *
 * @since 2.0 {@index}
 */
@Named(ApplicationTenancyMenu.LOGICAL_TYPE_NAME)
@DomainService(
        nature = NatureOfService.VIEW
)
@DomainServiceLayout(
        named = "Security",
        menuBar = DomainServiceLayout.MenuBar.SECONDARY
)
@javax.annotation.Priority(PriorityPrecedence.EARLY)
public class ApplicationTenancyMenu {

    public static final String LOGICAL_TYPE_NAME = CausewayModuleExtSecmanApplib.NAMESPACE + ".ApplicationTenancyMenu";

    public static abstract class ActionDomainEvent<T> extends CausewayModuleExtSecmanApplib.ActionDomainEvent<T> {}

    @Inject private ApplicationTenancyRepository applicationTenancyRepository;
    @Inject private FactoryService factory;


    @ObjectSupport public String iconName() {
        return "applicationTenancy";
    }

    // -- TENANCY MANAGER

    @Action(
            domainEvent = tenancyManager.ActionDomainEvent.class,
            semantics = SemanticsOf.IDEMPOTENT
    )
    @ActionLayout(
            sequence = "100.30.1",
            cssClassFa = "globe"
    )
    public class tenancyManager{

        public class ActionDomainEvent extends ApplicationTenancyMenu.ActionDomainEvent<tenancyManager> { }

        @MemberSupport public ApplicationTenancyManager act(){
            return factory.viewModel(new ApplicationTenancyManager());
        }

    }

    // -- FIND TENANCIES

    @Action(
            domainEvent = findTenancies.ActionDomainEvent.class,
            semantics = SemanticsOf.SAFE
            )
    @ActionLayout(sequence = "100.30.2")
    public class findTenancies{

        public class ActionDomainEvent extends ApplicationTenancyMenu.ActionDomainEvent<findTenancies> {}

        @MemberSupport public Collection<? extends ApplicationTenancy> act(
                @Parameter(optionality = Optionality.OPTIONAL)
                @ParameterLayout(named = "Partial Name Or Path", describedAs = "String to search for, wildcard (*) can be used")
                @MinLength(1) // for auto-complete
                final String partialNameOrPath) {
            return applicationTenancyRepository.findByNameOrPathMatchingCached(partialNameOrPath);
        }
    }

}
