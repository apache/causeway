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
package org.apache.isis.extensions.secman.applib.tenancy.menu;

import java.util.Collection;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.DomainServiceLayout;
import org.apache.isis.applib.annotation.MemberSupport;
import org.apache.isis.applib.annotation.MinLength;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.ObjectSupport;
import org.apache.isis.applib.annotation.Optionality;
import org.apache.isis.applib.annotation.Parameter;
import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.apache.isis.applib.annotation.RestrictTo;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.extensions.secman.applib.IsisModuleExtSecmanApplib;
import org.apache.isis.extensions.secman.applib.tenancy.dom.ApplicationTenancy;
import org.apache.isis.extensions.secman.applib.tenancy.dom.ApplicationTenancyRepository;

@DomainService(
        nature = NatureOfService.VIEW,
        logicalTypeName = ApplicationTenancyMenu.LOGICAL_TYPE_NAME
)
@DomainServiceLayout(
        named = "Security",
        menuBar = DomainServiceLayout.MenuBar.SECONDARY
)
@javax.annotation.Priority(PriorityPrecedence.EARLY)
public class ApplicationTenancyMenu {

    public static final String LOGICAL_TYPE_NAME = IsisModuleExtSecmanApplib.NAMESPACE + ".ApplicationTenancyMenu";

    public static abstract class ActionDomainEvent<T> extends IsisModuleExtSecmanApplib.ActionDomainEvent<T> {}

    @Inject private ApplicationTenancyRepository applicationTenancyRepository;


    @ObjectSupport
    public String iconName() {
        return "applicationTenancy";
    }



    @Action(
            domainEvent = findTenancies.ActionEvent.class,
            semantics = SemanticsOf.SAFE
            )
    @ActionLayout(sequence = "100.30.1")
    public class findTenancies{

        public class ActionEvent extends ActionDomainEvent<findTenancies> {}

        @MemberSupport public Collection<? extends ApplicationTenancy> act(
                @Parameter(optionality = Optionality.OPTIONAL)
                @ParameterLayout(named = "Partial Name Or Path", describedAs = "String to search for, wildcard (*) can be used")
                @MinLength(1) // for auto-complete
                final String partialNameOrPath) {
            return applicationTenancyRepository.findByNameOrPathMatchingCached(partialNameOrPath);
        }
    }



    @Action(
            domainEvent = newTenancy.ActionEvent.class,
            semantics = SemanticsOf.IDEMPOTENT
            )
    @ActionLayout(sequence = "100.30.3")
    public class newTenancy{

        public class ActionEvent extends ActionDomainEvent<newTenancy> {}

        @MemberSupport public ApplicationTenancy act(
                @Parameter(maxLength = ApplicationTenancy.Name.MAX_LENGTH)
                @ParameterLayout(named = "Name", typicalLength = ApplicationTenancy.Name.TYPICAL_LENGTH)
                final String name,
                @Parameter(maxLength = ApplicationTenancy.Path.MAX_LENGTH)
                @ParameterLayout(named = "Path")
                final String path,
                @Parameter(optionality = Optionality.OPTIONAL)
                @ParameterLayout(named = "Parent")
                final ApplicationTenancy parent) {
            return applicationTenancyRepository.newTenancy(name, path, parent);
        }

        //FIXME[ISIS-2703] when not provided, MM validation should fail, but yet does not
        @MemberSupport public Collection<ApplicationTenancy> choicesParent() {
            return applicationTenancyRepository.allTenancies();
        }

    }


    @Action(
            domainEvent = allTenancies.ActionEvent.class,
            semantics = SemanticsOf.SAFE,
            restrictTo = RestrictTo.PROTOTYPING
            )
    @ActionLayout(sequence = "100.30.4")
    public class allTenancies{

        public class ActionEvent extends ActionDomainEvent<allTenancies> {}

        @MemberSupport public Collection<? extends ApplicationTenancy> act() {
            return applicationTenancyRepository.allTenancies();
        }
    }

}
