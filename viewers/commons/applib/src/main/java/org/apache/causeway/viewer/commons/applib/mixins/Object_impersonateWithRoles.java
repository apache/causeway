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
package org.apache.causeway.viewer.commons.applib.mixins;

import java.util.List;

import jakarta.inject.Inject;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.Publishing;
import org.apache.causeway.applib.annotation.RestrictTo;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.applib.layout.LayoutConstants;
import org.apache.causeway.applib.mixins.security.HasUsername;
import org.apache.causeway.applib.services.factory.FactoryService;
import org.apache.causeway.applib.services.user.ImpersonateMenu;

import lombok.RequiredArgsConstructor;

/**
 * Same as {@link org.apache.causeway.applib.services.user.ImpersonateMenu.impersonateWithRoles#act(String, List, String)},
 * but implemented as a mixin so that can be invoked while accessing an object.
 *
 * @since 2.0 {@index}
 */
@Action(
        commandPublishing = Publishing.DISABLED,
        domainEvent = Object_impersonateWithRoles.ActionDomainEvent.class,
        executionPublishing = Publishing.DISABLED,
        restrictTo = RestrictTo.PROTOTYPING,
        semantics = SemanticsOf.IDEMPOTENT
)
@ActionLayout(
        cssClassFa = "fa-mask",
        describedAs = "Switch to another user account with specified roles (for prototype/testing only)",
        fieldSetId = LayoutConstants.FieldSetId.METADATA,
        position = ActionLayout.Position.PANEL_DROPDOWN,
        sequence = "850.2"
)
//mixin's don't need a logicalTypeName
@RequiredArgsConstructor
public class Object_impersonateWithRoles {

    public static class ActionDomainEvent
    extends org.apache.causeway.applib.events.domain.ActionDomainEvent<Object> {}

    private final Object holder;

    @MemberSupport public Object act(
            final String userName,
            final List<String> roleNames,
            final String multiTenancyToken) {
        impersonateWithRoles().act(userName, roleNames, multiTenancyToken);
        return holder;
    }

    @MemberSupport public boolean hideAct() {
        return impersonateWithRoles().hideAct();
    }

    @MemberSupport public String disableAct() {
        return impersonateWithRoles().disableAct();
    }

    @MemberSupport public List<String> choices0Act() {
        return impersonateWithRoles().choices0Act();
    }

    @MemberSupport public String default0Act() {
        if (holder instanceof HasUsername) {
            var username = ((HasUsername) holder).getUsername();
            if (choices0Act().contains(username)) {
                return username;
            }
        }
        return null;
    }

    @MemberSupport public List<String> choices1Act(final String userName) {
        return impersonateWithRoles().choices1Act(userName);
    }
    @MemberSupport public List<String> default1Act(final String userName) {
        return impersonateWithRoles().default1Act(userName);
    }
    @MemberSupport public String default2Act(final String userName, final List<String> roleNames) {
        return impersonateWithRoles().default2Act(userName, roleNames);
    }

    private ImpersonateMenu.impersonateWithRoles impersonateWithRoles() {
        return factoryService.mixin(ImpersonateMenu.impersonateWithRoles.class, impersonateMenu);
    }

    @Inject ImpersonateMenu impersonateMenu;
    @Inject FactoryService factoryService;

}
