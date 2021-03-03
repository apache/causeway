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
package org.apache.isis.extensions.secman.model.dom.permission;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.extensions.secman.api.permission.ApplicationPermission;
import org.apache.isis.extensions.secman.api.permission.ApplicationPermission.DeleteDomainEvent;
import org.apache.isis.extensions.secman.api.role.ApplicationRole;

import lombok.RequiredArgsConstructor;
import lombok.val;

@Action(
        domainEvent = DeleteDomainEvent.class, 
        semantics = SemanticsOf.IDEMPOTENT_ARE_YOU_SURE)
@RequiredArgsConstructor
public class ApplicationPermission_delete {
    
    @Inject private RepositoryService repository;

    private final ApplicationPermission target;

    public ApplicationRole act() {
        val owningRole = target.getRole();
        repository.remove(target);
        return owningRole;
    }

}
