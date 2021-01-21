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
package org.apache.isis.extensions.secman.jpa;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import org.apache.isis.extensions.secman.jpa.dom.permission.ApplicationPermission;
import org.apache.isis.extensions.secman.jpa.dom.permission.ApplicationPermissionRepository;
import org.apache.isis.extensions.secman.jpa.dom.role.ApplicationRole;
import org.apache.isis.extensions.secman.jpa.dom.role.ApplicationRoleRepository;
import org.apache.isis.extensions.secman.jpa.dom.tenancy.ApplicationTenancy;
import org.apache.isis.extensions.secman.jpa.dom.tenancy.ApplicationTenancyRepository;
import org.apache.isis.extensions.secman.jpa.dom.user.ApplicationUser;
import org.apache.isis.extensions.secman.jpa.dom.user.ApplicationUserRepository;
import org.apache.isis.extensions.secman.jpa.seed.SeedSecurityModuleService;

@Configuration
@Import({
    ApplicationPermissionRepository.class,
    ApplicationRoleRepository.class,
    ApplicationTenancyRepository.class,
    ApplicationUserRepository.class,

    ApplicationPermission.class,
    ApplicationRole.class,
    ApplicationTenancy.class,
    ApplicationUser.class,

    SeedSecurityModuleService.class,

})
@EntityScan(basePackageClasses = {
        ApplicationPermission.class,
        ApplicationRole.class,
        ApplicationTenancy.class,
        ApplicationUser.class,
})
public class IsisModuleExtSecmanPersistenceJpa {

}
