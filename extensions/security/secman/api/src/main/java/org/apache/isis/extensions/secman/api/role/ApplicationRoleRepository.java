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
import java.util.Optional;

import org.apache.isis.extensions.secman.api.user.ApplicationUser;

public interface ApplicationRoleRepository<R extends ApplicationRole> {

    /**
     * 
     * @return detached entity
     */
    R newApplicationRole();
    
    Collection<R> allRoles();

    R newRole(String name, String description);

    Collection<R> findNameContaining(String search);
    Collection<R> getRoles(ApplicationUser user);
    
    /**
     * auto-complete support
     * @param search
     */
    Collection<R> findMatching(String search);

    Optional<R> findByName(String roleName);
    Optional<R> findByNameCached(String roleName);

    void addRoleToUser(ApplicationRole role, ApplicationUser user);
    void removeRoleFromUser(ApplicationRole role, ApplicationUser user);

    boolean isAdminRole(ApplicationRole role);

    void deleteRole(ApplicationRole holder);

    

}
