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
package org.apache.isis.extensions.secman.api.role;

import java.util.Collection;

import org.apache.isis.extensions.secman.api.user.ApplicationUser;

public interface ApplicationRoleRepository {

    /**
     * 
     * @return detached entity
     */
    ApplicationRole newApplicationRole();
    
    Collection<ApplicationRole> allRoles();

    ApplicationRole newRole(String name, String description);

    Collection<ApplicationRole> findNameContaining(String search);
    Collection<? extends ApplicationRole> getRoles(ApplicationUser user);
    
    Collection<ApplicationUser> getUsers(ApplicationRole role);

    ApplicationRole findByName(String roleName);

    ApplicationRole findByNameCached(String roleName);

    void addRoleToUser(ApplicationRole role, ApplicationUser holder);
    void removeRoleFromUser(ApplicationRole role, ApplicationUser holder);

    boolean isAdminRole(ApplicationRole role);

    void deleteRole(ApplicationRole holder);
    

}
