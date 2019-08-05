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
package org.apache.isis.extensions.secman.api.user;

import java.util.List;

import org.apache.isis.applib.value.Password;
import org.apache.isis.extensions.secman.api.role.ApplicationRole;

public interface ApplicationUserRepository {

    ApplicationUser findByUsername(String username);

    ApplicationUser findOrCreateUserByUsername(String username);

    List<? extends ApplicationUser> allUsers();

    List<? extends ApplicationUser> find(String search);

    String validateNewLocalUser(String username, Password password, Password passwordRepeat,
            ApplicationRole initialRole, Boolean enabled, String emailAddress);

    ApplicationUser newLocalUser(String username, Password password, Password passwordRepeat,
            ApplicationRole initialRole, Boolean enabled, String emailAddress);

    ApplicationUser newDelegateUser(String username, ApplicationRole initialRole, Boolean enabled);

    void enable(ApplicationUser svenUser);
    void disable(ApplicationUser svenUser);

}
