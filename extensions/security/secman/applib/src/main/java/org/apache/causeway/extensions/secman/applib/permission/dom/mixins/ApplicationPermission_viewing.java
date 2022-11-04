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
package org.apache.causeway.extensions.secman.applib.permission.dom.mixins;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.extensions.secman.applib.CausewayModuleExtSecmanApplib;
import org.apache.causeway.extensions.secman.applib.permission.dom.ApplicationPermission;
import org.apache.causeway.extensions.secman.applib.permission.dom.ApplicationPermissionMode;
import org.apache.causeway.extensions.secman.applib.permission.dom.mixins.ApplicationPermission_viewing.DomainEvent;

import lombok.RequiredArgsConstructor;

/**
 *
 * @since 2.0 {@index}
 */
@Action(
        domainEvent = DomainEvent.class,
        semantics = SemanticsOf.IDEMPOTENT
)
@ActionLayout(
        associateWith = "mode",
        sequence = "1"
)
@RequiredArgsConstructor
public class ApplicationPermission_viewing {

    public static class DomainEvent
            extends CausewayModuleExtSecmanApplib.ActionDomainEvent<ApplicationPermission_viewing> {}

    private final ApplicationPermission target;

    @MemberSupport public ApplicationPermission act() {
        target.setMode(ApplicationPermissionMode.VIEWING);
        return target;
    }

    @MemberSupport public String disableAct() {
        return target.getMode() == ApplicationPermissionMode.VIEWING ? "Mode is already set to VIEWING": null;
    }

}
