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
package org.apache.isis.extensions.secman.applib.tenancy.dom.mixins;

import org.apache.isis.applib.annotations.Action;
import org.apache.isis.applib.annotations.ActionLayout;
import org.apache.isis.applib.annotations.MemberSupport;
import org.apache.isis.applib.annotations.PromptStyle;
import org.apache.isis.applib.annotations.SemanticsOf;
import org.apache.isis.extensions.secman.applib.IsisModuleExtSecmanApplib;
import org.apache.isis.extensions.secman.applib.tenancy.dom.ApplicationTenancy;
import org.apache.isis.extensions.secman.applib.tenancy.dom.mixins.ApplicationTenancy_updateName.DomainEvent;

import lombok.RequiredArgsConstructor;

@Action(
        domainEvent = DomainEvent.class,
        semantics = SemanticsOf.IDEMPOTENT
)
@ActionLayout(
        associateWith = "name",
        promptStyle = PromptStyle.INLINE_AS_IF_EDIT,
        sequence = "1"
)
@RequiredArgsConstructor
public class ApplicationTenancy_updateName {

    public static class DomainEvent
            extends IsisModuleExtSecmanApplib.ActionDomainEvent<ApplicationTenancy_updateName> {}

    private final ApplicationTenancy target;

    @MemberSupport public ApplicationTenancy act(
            @ApplicationTenancy.Name
            final String name) {
        target.setName(name);
        return target;
    }

    @MemberSupport public String default0Act() { return target.getName(); }

}
