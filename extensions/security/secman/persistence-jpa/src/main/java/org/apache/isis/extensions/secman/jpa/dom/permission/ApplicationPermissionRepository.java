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
package org.apache.isis.extensions.secman.jpa.dom.permission;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.stereotype.Repository;

import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.extensions.secman.api.permission.dom.ApplicationPermissionRepositoryAbstract;
import org.apache.isis.extensions.secman.api.user.dom.ApplicationUser;
import org.apache.isis.extensions.secman.api.user.dom.ApplicationUserRepository;
import org.apache.isis.extensions.secman.api.permission.dom.ApplicationPermission;

import lombok.NonNull;

@Repository
@Named("isis.ext.secman.ApplicationPermissionRepository")
public class ApplicationPermissionRepository
extends ApplicationPermissionRepositoryAbstract<ApplicationPermission> {

    protected ApplicationPermissionRepository() {
        super(ApplicationPermission.class);
    }

    // TODO NAMED_QUERY_FIND_BY_USER not working yet, using workaround  ...
    @Override
    public List<ApplicationPermission> findByUser(@NonNull final ApplicationUser user) {
        final String username = user.getUsername();

        return userRepository.findByUsername(username)
                .map(ApplicationUser::getRoles)
                .map(_NullSafe::stream)
                .map(roleStream -> roleStream
                        .map(this::findByRole)
                        .flatMap(List::stream)
                        .collect(Collectors.toList()))
                .orElse(Collections.emptyList());
    }

    @Inject private ApplicationUserRepository userRepository;

}
