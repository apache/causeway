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
 * Same as {@link org.apache.causeway.applib.services.user.ImpersonateMenu.impersonate#act(String)},
 * but implemented as a mixin so that can be invoked while accessing an object.
 *
 * @since 2.0 {@index}
 */
@Action(
        commandPublishing = Publishing.DISABLED,
        domainEvent = Object_impersonate.ActionDomainEvent.class,
        executionPublishing = Publishing.DISABLED,
        restrictTo = RestrictTo.PROTOTYPING,
        semantics = SemanticsOf.IDEMPOTENT
)
@ActionLayout(
        cssClassFa = "fa-mask",
        describedAs = "Switch to another user account (for prototype/testing only)",
        fieldSetId = LayoutConstants.FieldSetId.METADATA,
        position = ActionLayout.Position.PANEL_DROPDOWN,
        sequence = "850.1"
)
@RequiredArgsConstructor
public class Object_impersonate {

    public static class ActionDomainEvent
    extends org.apache.causeway.applib.events.domain.ActionDomainEvent<Object> {}

    private final Object holder;

    @MemberSupport public Object act(final String userName) {
        impersonate().act(userName);
        return holder;
    }

    @MemberSupport public boolean hideAct() { return impersonate().hideAct(); }
    @MemberSupport public String disableAct() { return impersonate().disableAct();
    }
    @MemberSupport public String default0Act() {
        return holder instanceof HasUsername
                ? ((HasUsername)holder).getUsername()
                : null;
    }

    private ImpersonateMenu.impersonate impersonate() {
        return factoryService.mixin(ImpersonateMenu.impersonate.class, impersonateMenu);
    }

    @Inject ImpersonateMenu impersonateMenu;
    @Inject FactoryService factoryService;

}
