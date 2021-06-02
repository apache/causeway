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
package org.apache.isis.viewer.common.applib.mixins;

import java.util.List;

import javax.inject.Inject;

import org.apache.isis.applib.IsisModuleApplib;
import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.MemberSupport;
import org.apache.isis.applib.annotation.Publishing;
import org.apache.isis.applib.annotation.Redirect;
import org.apache.isis.applib.annotation.RestrictTo;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.mixins.layout.LayoutMixinConstants;
import org.apache.isis.applib.mixins.security.HasUsername;
import org.apache.isis.applib.services.user.ImpersonateMenu;

import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * Same as {@link ImpersonateMenu#impersonateWithRoles(String, List)},
 * but implemented as a mixin so that can be invoked while accessing an object.
 *
 * @since 2.0 {@index}
 */
@Action(
        domainEvent = Object_impersonateWithRoles.ActionDomainEvent.class,
        semantics = SemanticsOf.IDEMPOTENT,
        commandPublishing = Publishing.DISABLED,
        executionPublishing = Publishing.DISABLED,
        restrictTo = RestrictTo.PROTOTYPING
)
@ActionLayout(
        cssClassFa = "fa-mask",
        position = ActionLayout.Position.PANEL_DROPDOWN,
        redirectPolicy = Redirect.EVEN_IF_SAME,
        associateWith = LayoutMixinConstants.METADATA_LAYOUT_GROUPNAME,
        sequence = "850.2"
)
@DomainObject(logicalTypeName = IsisModuleApplib.NAMESPACE_SUDO + ".mixins.Object_impersonateWithRoles")
@RequiredArgsConstructor
public class Object_impersonateWithRoles {

    public static class ActionDomainEvent
    extends org.apache.isis.applib.events.domain.ActionDomainEvent<Object> {}

    private final Object holder;

    public Object act(
            final String userName,
            final List<String> roleNames) {
        impersonateMenu.impersonateWithRoles(userName, roleNames);
        return holder;
    }

    @MemberSupport public boolean hideAct() {
        return impersonateMenu.hideImpersonateWithRoles();
    }

    @MemberSupport public String disableAct() {
        return impersonateMenu.disableImpersonateWithRoles();
    }

    @MemberSupport public List<String> choices0Act() {
        return impersonateMenu.choices0ImpersonateWithRoles();
    }

    @MemberSupport public String default0Act() {
        if (holder instanceof HasUsername) {
            val username = ((HasUsername) holder).getUsername();
            if (choices0Act().contains(username)) {
                return username;
            }
        }
        return null;
    }

    @MemberSupport public List<String> choices1Act(
            final String userName) {
        return impersonateMenu.choices1ImpersonateWithRoles(userName);
    }

    @MemberSupport public List<String> default1Act(
            final String userName) {
        return impersonateMenu.default1ImpersonateWithRoles(userName);
    }

    @Inject ImpersonateMenu impersonateMenu;

}
