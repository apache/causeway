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

import javax.inject.Inject;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.applib.services.repository.RepositoryService;
import org.apache.causeway.extensions.secman.applib.CausewayModuleExtSecmanApplib;
import org.apache.causeway.extensions.secman.applib.permission.dom.ApplicationPermission;
import org.apache.causeway.extensions.secman.applib.permission.dom.mixins.ApplicationPermission_delete.DomainEvent;
import org.apache.causeway.extensions.secman.applib.role.dom.ApplicationRole;

import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 *
 * @since 2.0 {@index}
 */
@Action(
        domainEvent = DomainEvent.class,
        semantics = SemanticsOf.IDEMPOTENT_ARE_YOU_SURE
)
@ActionLayout(
        associateWith = "role",
        position = ActionLayout.Position.PANEL,
        sequence = "1"
)
@RequiredArgsConstructor
public class ApplicationPermission_delete {

    public static class DomainEvent
            extends CausewayModuleExtSecmanApplib.ActionDomainEvent<ApplicationPermission_delete> {}

    @Inject private RepositoryService repository;

    private final ApplicationPermission target;

    @MemberSupport public ApplicationRole act() {
        val owningRole = target.getRole();
        repository.remove(target);
        return owningRole;
    }

}
