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
package org.apache.isis.extensions.secman.api.tenancy.dom.mixins;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.MemberSupport;
import org.apache.isis.applib.annotation.Parameter;
import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.extensions.secman.api.IsisModuleExtSecmanApi;
import org.apache.isis.extensions.secman.api.tenancy.dom.ApplicationTenancy;
import org.apache.isis.extensions.secman.api.tenancy.dom.mixins.ApplicationTenancy_updateName.DomainEvent;

import lombok.RequiredArgsConstructor;

@Action(
        associateWith = "name",
        domainEvent = DomainEvent.class,
        semantics = SemanticsOf.IDEMPOTENT
)
@ActionLayout(
        sequence = "1"
)
@RequiredArgsConstructor
public class ApplicationTenancy_updateName {

    public static class DomainEvent
            extends IsisModuleExtSecmanApi.ActionDomainEvent<ApplicationTenancy_updateName> {}

    private final ApplicationTenancy target;

    @MemberSupport
    public ApplicationTenancy act(
            @Parameter(maxLength = ApplicationTenancy.MAX_LENGTH_NAME)
            @ParameterLayout(named="Name", typicalLength=ApplicationTenancy.TYPICAL_LENGTH_NAME)
            final String name) {
        target.setName(name);
        return target;
    }

    @MemberSupport
    public String default0Act() {
        return target.getName();
    }

}
