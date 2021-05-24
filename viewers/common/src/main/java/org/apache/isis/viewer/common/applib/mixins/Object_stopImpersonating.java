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
import org.apache.isis.applib.services.user.ImpersonateMenu;

import lombok.RequiredArgsConstructor;

/**
 * Same as {@link ImpersonateMenu#stopImpersonating()},
 * but implemented as a mixin so that can be invoked while accessing an object.
 *
 * @since 2.0 {@index}
 */
@Action(
        domainEvent = Object_stopImpersonating.ActionDomainEvent.class,
        semantics = SemanticsOf.IDEMPOTENT,
        commandPublishing = Publishing.DISABLED,
        executionPublishing = Publishing.DISABLED,
        restrictTo = RestrictTo.PROTOTYPING
)
@ActionLayout(
        position = ActionLayout.Position.PANEL_DROPDOWN,
        redirectPolicy = Redirect.EVEN_IF_SAME,
        associateWith = LayoutMixinConstants.METADATA_LAYOUT_GROUPNAME,
        sequence = "850.3"
)
@RequiredArgsConstructor
public class Object_stopImpersonating {

    public static class ActionDomainEvent
    extends org.apache.isis.applib.events.domain.ActionDomainEvent<Object> {}

    private final Object holder;

    public Object act() {
        impersonateMenu.stopImpersonating();
        return holder;
    }

    @MemberSupport public boolean hideAct() {
        return impersonateMenu.hideStopImpersonating();
    }
    @MemberSupport public String disableAct() {
        return impersonateMenu.disableStopImpersonating();

    }

    @Inject ImpersonateMenu impersonateMenu;

}
