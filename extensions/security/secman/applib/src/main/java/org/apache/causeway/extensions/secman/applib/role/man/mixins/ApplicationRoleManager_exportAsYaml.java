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
package org.apache.causeway.extensions.secman.applib.role.man.mixins;

import java.util.Locale;

import javax.inject.Inject;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.Parameter;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.applib.value.Clob;
import org.apache.causeway.applib.value.NamedWithMimeType.CommonMimeType;
import org.apache.causeway.applib.value.semantics.ValueSemanticsProvider;
import org.apache.causeway.extensions.secman.applib.CausewayModuleExtSecmanApplib;
import org.apache.causeway.extensions.secman.applib.role.dom.ApplicationRoleRepository;
import org.apache.causeway.extensions.secman.applib.role.man.ApplicationRoleManager;
import org.apache.causeway.extensions.secman.applib.tenancy.dom.ApplicationTenancyRepository;
import org.apache.causeway.extensions.secman.applib.user.dom.ApplicationUserRepository;
import org.apache.causeway.extensions.secman.applib.user.man.mixins.ApplicationUserManager_newLocalUser.DomainEvent;
import org.apache.causeway.extensions.secman.applib.util.ApplicationSecurityDto;

import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 *
 * @since 2.0 {@index}
 */
@Action(
        domainEvent = DomainEvent.class,
        semantics = SemanticsOf.IDEMPOTENT
)
@ActionLayout(
        associateWith = "allRoles",
        sequence = "1.1",
        describedAs = "Exports authentication and authorization data to YAML format. "
                + "Includes users, roles, permissions and tenancies."
)
@RequiredArgsConstructor
public class ApplicationRoleManager_exportAsYaml {

    public static class DomainEvent
            extends CausewayModuleExtSecmanApplib.ActionDomainEvent<ApplicationRoleManager_exportAsYaml> {}

    @Inject private ApplicationRoleRepository applicationRoleRepository;
    @Inject private ApplicationUserRepository applicationUserRepository;
    @Inject private ApplicationTenancyRepository applicationTenancyRepository;
    @Inject private ValueSemanticsProvider<Locale> localeSemantics;

    @SuppressWarnings("unused")
    private final ApplicationRoleManager target;

    @MemberSupport public Clob act(
            @Parameter
            final String fileName) {

        val yaml = ApplicationSecurityDto.create(
                applicationRoleRepository,
                applicationUserRepository,
                applicationTenancyRepository,
                localeSemantics)
                .toYaml();

        return Clob.of(fileName, CommonMimeType.YAML, yaml);
    }

    @MemberSupport public String defaultFileName() {
        return "secman-permissions.yml";
    }

}
