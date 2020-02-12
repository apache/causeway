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

import java.util.SortedSet;

import org.apache.isis.applib.services.HasUsername;
import org.apache.isis.extensions.secman.api.IsisModuleExtSecmanApi;
import org.apache.isis.extensions.secman.api.permission.ApplicationPermissionValueSet;
import org.apache.isis.extensions.secman.api.role.ApplicationRole;
import org.apache.isis.extensions.secman.api.tenancy.HasAtPath;


public interface ApplicationUser extends HasUsername, HasAtPath {

    // -- constants
    public static final int MAX_LENGTH_USERNAME = 30;
    public static final int MAX_LENGTH_FAMILY_NAME = 50;
    public static final int MAX_LENGTH_GIVEN_NAME = 50;
    public static final int MAX_LENGTH_KNOWN_AS = 20;
    public static final int MAX_LENGTH_EMAIL_ADDRESS = 50;
    public static final int MAX_LENGTH_PHONE_NUMBER = 25;


    String getName();

    String getEncryptedPassword();

    AccountType getAccountType();

    ApplicationPermissionValueSet getPermissionSet();

    SortedSet<? extends ApplicationRole> getRoles();

    ApplicationUserStatus getStatus();

    @Override
    String getUsername();
    
    // -- EVENTS 
    
    public static abstract class PropertyDomainEvent<T>
    extends IsisModuleExtSecmanApi.PropertyDomainEvent<ApplicationUser, T> {}

    public static abstract class CollectionDomainEvent<T> 
    extends IsisModuleExtSecmanApi.CollectionDomainEvent<ApplicationUser, T> {}

    public static abstract class ActionDomainEvent 
    extends IsisModuleExtSecmanApi.ActionDomainEvent<ApplicationUser> {}
    
    public static class AddRoleDomainEvent extends ActionDomainEvent {}

    

}
