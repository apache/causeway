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
package org.apache.isis.extensions.secman.applib.tenancy.man.mixins;

import java.util.Collection;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.MemberSupport;
import org.apache.isis.applib.annotation.Optionality;
import org.apache.isis.applib.annotation.Parameter;
import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.extensions.secman.applib.tenancy.dom.ApplicationTenancy;
import org.apache.isis.extensions.secman.applib.tenancy.dom.ApplicationTenancyRepository;
import org.apache.isis.extensions.secman.applib.tenancy.man.ApplicationTenancyManager;
import org.apache.isis.extensions.secman.applib.user.man.mixins.ApplicationUserManager_newLocalUser.DomainEvent;

import lombok.RequiredArgsConstructor;


@Action(
        domainEvent = DomainEvent.class,
        semantics = SemanticsOf.IDEMPOTENT
)
@ActionLayout(
        associateWith = "allTenancies",
        sequence = "1"
)
@RequiredArgsConstructor
public class ApplicationTenancyManager_newTenancy {

    private final ApplicationTenancyManager target;

    @Inject
    private ApplicationTenancyRepository applicationTenancyRepository;

    @MemberSupport public ApplicationTenancyManager act(
            @Parameter(maxLength = ApplicationTenancy.Name.MAX_LENGTH)
            @ParameterLayout(named = "Name", typicalLength = ApplicationTenancy.Name.TYPICAL_LENGTH)
            final String name,
            @Parameter(maxLength = ApplicationTenancy.Path.MAX_LENGTH)
            @ParameterLayout(named = "Path")
            final String path,
            @Parameter(optionality = Optionality.OPTIONAL)
            @ParameterLayout(named = "Parent")
            final ApplicationTenancy parent) {
        applicationTenancyRepository.newTenancy(name, path, parent);
        return target;
    }

    @MemberSupport public Collection<ApplicationTenancy> choicesParent() {
        return applicationTenancyRepository.allTenancies();
    }

}
