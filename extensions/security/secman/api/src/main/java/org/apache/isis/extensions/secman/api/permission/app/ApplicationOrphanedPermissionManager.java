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
package org.apache.isis.extensions.secman.api.permission.app;

import java.util.Collection;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.extensions.secman.api.IsisModuleExtSecmanApi;
import org.apache.isis.extensions.secman.api.permission.dom.ApplicationPermission;
import org.apache.isis.extensions.secman.api.permission.dom.ApplicationPermissionRepository;

@DomainObject(
        nature = Nature.VIEW_MODEL,
        objectType = ApplicationOrphanedPermissionManager.OBJECT_TYPE
)
public class ApplicationOrphanedPermissionManager {

    public static final String OBJECT_TYPE = IsisModuleExtSecmanApi.NAMESPACE + ".ApplicationOrphanedPermissionManager";

    public static abstract class CollectionDomainEvent<T>
            extends IsisModuleExtSecmanApi.CollectionDomainEvent<ApplicationOrphanedPermissionManager, T> {}

    @Inject private ApplicationPermissionRepository applicationPermissionRepository;


    public String title() {
        return "Manage Orphaned Permissions";
    }


    // orphaned permissions

    public static class OrphanedPermissionsEvent extends CollectionDomainEvent<ApplicationPermission>{}

    @org.apache.isis.applib.annotation.Collection(
            domainEvent = OrphanedPermissionsEvent.class,
            typeOf = ApplicationPermission.class
    )
    public Collection<ApplicationPermission> getOrphanedPermissions() {
        return applicationPermissionRepository.findOrphaned();
    }

}
