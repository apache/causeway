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

import org.apache.isis.extensions.secman.api.IsisModuleExtSecmanApi;

public interface ApplicationRole {

    public static final int MAX_LENGTH_NAME = 50;
    public static final int TYPICAL_LENGTH_NAME = 30;
    public static final int TYPICAL_LENGTH_DESCRIPTION = 50;

    // -- EVENTS
    
    public static abstract class PropertyDomainEvent<T> extends IsisModuleExtSecmanApi.PropertyDomainEvent<ApplicationRole, T> {}
    public static abstract class CollectionDomainEvent<T> extends IsisModuleExtSecmanApi.CollectionDomainEvent<ApplicationRole, T> {}
    public static abstract class ActionDomainEvent extends IsisModuleExtSecmanApi.ActionDomainEvent<ApplicationRole> {}
    
    public static class AddUserDomainEvent extends ActionDomainEvent {}
    public static class RemoveUserDomainEvent extends ActionDomainEvent {}
    public static class AddActionDomainEvent extends ActionDomainEvent {}
    public static class AddClassDomainEvent extends ActionDomainEvent {}
    public static class AddCollectionDomainEvent extends ActionDomainEvent {}
    public static class AddPackageDomainEvent extends ActionDomainEvent {}
    public static class AddPropertyDomainEvent extends ActionDomainEvent {}
    public static class RemovePermissionDomainEvent extends ActionDomainEvent {}
    
    public static class DeleteDomainEvent extends ActionDomainEvent {}
    
    public static class UpdateDescriptionDomainEvent extends ActionDomainEvent {}
    public static class UpdateNameDomainEvent extends ActionDomainEvent {}
    
    // -- MODEL
    
    /**
     * having a title() method (rather than using @Title annotation) is necessary as a workaround to be able to use
     * wrapperFactory#unwrap(...) method, which is otherwise broken in Isis 1.6.0
     */
    default String title() {
        return getName();
    }
    
    String getName();
    void setName(String name);
    
    String getDescription();
    void setDescription(String description);


}
