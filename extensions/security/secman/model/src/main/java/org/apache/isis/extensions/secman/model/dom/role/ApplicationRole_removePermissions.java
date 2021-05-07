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
package org.apache.isis.extensions.secman.model.dom.role;

import java.util.Collection;
import java.util.Objects;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.MemberSupport;
import org.apache.isis.applib.annotation.PromptStyle;
import org.apache.isis.applib.services.message.MessageService;
import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.extensions.secman.api.SecmanConfiguration;
import org.apache.isis.extensions.secman.api.permission.dom.ApplicationPermission;
import org.apache.isis.extensions.secman.api.role.dom.ApplicationRole;
import org.apache.isis.extensions.secman.api.role.dom.ApplicationRole.RemovePermissionDomainEvent;
import org.apache.isis.extensions.secman.api.role.dom.ApplicationRoleRepository;

import lombok.RequiredArgsConstructor;

@Action(
        domainEvent = RemovePermissionDomainEvent.class,
        associateWith = "permissions")
@ActionLayout(
		named="Remove",
		sequence = "10",
		promptStyle = PromptStyle.DIALOG_MODAL)
@RequiredArgsConstructor
public class ApplicationRole_removePermissions {

    @Inject private MessageService messageService;
    @Inject private SecmanConfiguration configBean;
    @Inject private RepositoryService repository;
    @Inject private ApplicationRoleRepository<? extends ApplicationRole> applicationRoleRepository;

    private final ApplicationRole target;

    @MemberSupport
    public ApplicationRole act(Collection<ApplicationPermission> permissions) {

        _NullSafe.stream(permissions)
        .filter(this::canRemove)
        .forEach(repository::remove);

        return target;
    }

    private boolean canRemove(ApplicationPermission permission) {
        if(!Objects.equals(permission.getRole(), target)) {
            return false;
        }
        if(applicationRoleRepository.isAdminRole(target)
                && configBean.isStickyAdminNamespace(permission.getFeatureFqn())) {

            messageService.warnUser("Cannot remove top-level namespace permissions for the admin role.");
            return false;
        }
        return true;
    }

}
