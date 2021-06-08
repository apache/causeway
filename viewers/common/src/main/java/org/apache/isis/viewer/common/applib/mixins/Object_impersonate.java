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

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.MemberSupport;
import org.apache.isis.applib.annotation.Publishing;
import org.apache.isis.applib.annotation.Redirect;
import org.apache.isis.applib.annotation.RestrictTo;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.mixins.layout.LayoutMixinConstants;
import org.apache.isis.applib.mixins.security.HasUsername;
import org.apache.isis.applib.services.user.ImpersonateMenu;

import lombok.RequiredArgsConstructor;

/**
 * Same as {@link ImpersonateMenu#impersonate(String)},
 * but implemented as a mixin so that can be invoked while accessing an object.
 *
 * @since 2.0 {@index}
 */
@Action(
        domainEvent = Object_impersonate.ActionDomainEvent.class,
        semantics = SemanticsOf.IDEMPOTENT,
        commandPublishing = Publishing.DISABLED,
        executionPublishing = Publishing.DISABLED,
        restrictTo = RestrictTo.PROTOTYPING
)
@ActionLayout(
        associateWith = LayoutMixinConstants.METADATA_LAYOUT_GROUPNAME,
        cssClassFa = "fa-mask",
        position = ActionLayout.Position.PANEL_DROPDOWN,
        redirectPolicy = Redirect.EVEN_IF_SAME,
        sequence = "850.1"
)
//mixin's don't need a logicalTypeName, in fact MM validation should guard against wrong usage here
//@DomainObject(logicalTypeName = IsisModuleApplib.NAMESPACE_SUDO + ".mixins.Object_impersonate")
@RequiredArgsConstructor
public class Object_impersonate {

    public static class ActionDomainEvent
    extends org.apache.isis.applib.events.domain.ActionDomainEvent<Object> {}

    private final Object holder;

    public Object act(final String userName) {
        impersonateMenu.impersonate(userName);
        return holder;
    }

    @MemberSupport public boolean hideAct() {
        return impersonateMenu.hideImpersonate();
    }
    @MemberSupport public String disableAct() {
        return impersonateMenu.disableImpersonate();
    }
    @MemberSupport public String default0Act() {
        return holder instanceof HasUsername
                ? ((HasUsername)holder).getUsername()
                : null;
    }

    @Inject ImpersonateMenu impersonateMenu;

}
