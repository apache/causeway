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
package org.apache.isis.extensions.secman.api.permission;

import org.apache.isis.core.metamodel.services.appfeat.ApplicationFeatureType;
import org.apache.isis.extensions.secman.api.IsisModuleExtSecmanApi;
import org.apache.isis.extensions.secman.api.role.ApplicationRole;

public interface ApplicationPermission {
    
    ApplicationFeatureType getFeatureType();

    String getFeatureFqn();
    
    ApplicationPermissionRule getRule();
    void setRule(ApplicationPermissionRule rule);
    
    ApplicationPermissionMode getMode();
    void setMode(ApplicationPermissionMode changing);
    
    ApplicationRole getRole();
    void setRole(ApplicationRole applicationRole);
    
    // DOMAIN EVENTS
    
    public static abstract class PropertyDomainEvent<T> extends IsisModuleExtSecmanApi.PropertyDomainEvent<ApplicationPermission, T> {}
    public static abstract class CollectionDomainEvent<T> extends IsisModuleExtSecmanApi.CollectionDomainEvent<ApplicationPermission, T> {}
    public static abstract class ActionDomainEvent extends IsisModuleExtSecmanApi.ActionDomainEvent<ApplicationPermission> {}
    
    public static class AllowDomainEvent extends ActionDomainEvent {}
    public static class UpdateRoleDomainEvent extends ActionDomainEvent {}
    public static class VetoDomainEvent extends ActionDomainEvent {}
    public static class DeleteDomainEvent extends ActionDomainEvent {}
    public static class ChangingDomainEvent extends ActionDomainEvent {}
    public static class ViewingDomainEvent extends ActionDomainEvent {}
    

}
