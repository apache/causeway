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
package org.apache.isis.extensions.secman.integration.spiimpl;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.apache.isis.applib.services.message.MessageService;
import org.apache.isis.applib.services.user.ImpersonateMenuAdvisor;
import org.apache.isis.applib.services.user.UserService;
import org.apache.isis.extensions.secman.applib.role.dom.ApplicationRole;
import org.apache.isis.extensions.secman.applib.role.dom.ApplicationRoleRepository;
import org.apache.isis.extensions.secman.applib.user.dom.ApplicationUser;
import org.apache.isis.extensions.secman.applib.user.dom.ApplicationUserRepository;
import org.apache.isis.extensions.secman.applib.user.dom.ApplicationUserStatus;

import lombok.RequiredArgsConstructor;
import lombok.val;

@Service
@Named("isis.ext.secman.ImpersonateMenuAdvisorForSecman")
@javax.annotation.Priority(PriorityPrecedence.MIDPOINT)
@Qualifier("SecMan")
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class ImpersonateMenuAdvisorForSecman implements ImpersonateMenuAdvisor {

    final ApplicationUserRepository applicationUserRepository;
    final ApplicationRoleRepository applicationRoleRepository;

    final UserService userService;
    final MessageService messageService;

    @Override
    public List<String> allUserNames() {
        return this.applicationUserRepository.allUsers()
                .stream()
                .filter(x -> x.getStatus() == ApplicationUserStatus.UNLOCKED)
                .map(ApplicationUser::getName)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> allRoleNames() {
        return this.applicationRoleRepository.allRoles()
                .stream()
                .map(ApplicationRole::getName)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> roleNamesFor(
            final String username) {
        if(username == null) {
            return Collections.emptyList();
        }
        val applicationUser =
                applicationUserRepository.findByUsername(username)
                        .orElseThrow(RuntimeException::new);
        val applicationRoles = applicationUser.getRoles();
        return applicationRoles
                .stream().map(ApplicationRole::getName)
                .collect(Collectors.toList());
    }

    @Override
    public String multiTenancyTokenFor(String username) {
        if(username == null) {
            return null;
        }
        val applicationUser =
                applicationUserRepository.findByUsername(username)
                        .orElseThrow(RuntimeException::new);
        return applicationUser.getAtPath();
    }

}
