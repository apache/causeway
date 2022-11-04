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
package org.apache.causeway.extensions.secman.applib.role.dom.mixins;

import java.util.Collection;
import java.util.Objects;

import javax.inject.Inject;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.PromptStyle;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.applib.services.message.MessageService;
import org.apache.causeway.applib.services.repository.RepositoryService;
import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.config.CausewayConfiguration.Extensions.Secman;
import org.apache.causeway.extensions.secman.applib.CausewayModuleExtSecmanApplib;
import org.apache.causeway.extensions.secman.applib.permission.dom.ApplicationPermission;
import org.apache.causeway.extensions.secman.applib.role.dom.ApplicationRole;
import org.apache.causeway.extensions.secman.applib.role.dom.ApplicationRoleRepository;
import org.apache.causeway.extensions.secman.applib.role.dom.mixins.ApplicationRole_removePermissions.DomainEvent;

import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 *
 * @since 2.0 {@index}
 */
@Action(
        choicesFrom = "permissions",
        domainEvent = DomainEvent.class,
        semantics = SemanticsOf.IDEMPOTENT
)
@ActionLayout(
        associateWith = "permissions",
		named = "Remove",
		promptStyle = PromptStyle.DIALOG_MODAL,
		sequence = "2"
)
@RequiredArgsConstructor
public class ApplicationRole_removePermissions {

    public static class DomainEvent
            extends CausewayModuleExtSecmanApplib.ActionDomainEvent<ApplicationRole_removePermissions> {}

    @Inject private MessageService messageService;
    @Inject private CausewayConfiguration config;
    @Inject private RepositoryService repository;
    @Inject private ApplicationRoleRepository applicationRoleRepository;

    private final ApplicationRole target;

    @MemberSupport public ApplicationRole act(final Collection<ApplicationPermission> permissions) {

        _NullSafe.stream(permissions)
        .filter(this::canRemove)
        .forEach(repository::remove);

        return target;
    }

    private boolean canRemove(final ApplicationPermission permission) {
        if(!Objects.equals(permission.getRole(), target)) {
            return false;
        }
        if(applicationRoleRepository.isAdminRole(target)
                && isStickyAdminNamespace(config.getExtensions().getSecman(), permission.getFeatureFqn())) {

            messageService.warnUser("Cannot remove top-level namespace permissions for the admin role.");
            return false;
        }
        return true;
    }

    private static boolean isStickyAdminNamespace(final Secman secman, final String featureFqn) {
        val adminNamespacePermissions = secman.getSeed().getAdmin().getNamespacePermissions();
        return _NullSafe.stream(adminNamespacePermissions.getSticky())
                .anyMatch(stickyPackage -> stickyPackage.equals(featureFqn));
    }

}
