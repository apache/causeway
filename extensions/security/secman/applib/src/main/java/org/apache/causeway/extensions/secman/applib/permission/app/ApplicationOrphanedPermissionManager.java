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
package org.apache.causeway.extensions.secman.applib.permission.app;

import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.Nature;
import org.apache.causeway.applib.annotation.ObjectSupport;
import org.apache.causeway.extensions.secman.applib.CausewayModuleExtSecmanApplib;
import org.apache.causeway.extensions.secman.applib.permission.dom.ApplicationPermission;
import org.apache.causeway.extensions.secman.applib.permission.dom.ApplicationPermissionRepository;

/**
 *
 * @since 2.0 {@index}
 */
@Named(ApplicationOrphanedPermissionManager.LOGICAL_TYPE_NAME)
@DomainObject(
        nature = Nature.VIEW_MODEL)
public class ApplicationOrphanedPermissionManager {

    public static final String LOGICAL_TYPE_NAME = CausewayModuleExtSecmanApplib.NAMESPACE + ".ApplicationOrphanedPermissionManager";

    public static abstract class CollectionDomainEvent<T>
            extends CausewayModuleExtSecmanApplib.CollectionDomainEvent<ApplicationOrphanedPermissionManager, T> {}

    @Inject private ApplicationPermissionRepository applicationPermissionRepository;

    @ObjectSupport public String title() {
        return "Manage Orphaned Permissions";
    }


    // orphaned permissions

    public static class OrphanedPermissionsEvent extends CollectionDomainEvent<ApplicationPermission>{}

    @org.apache.causeway.applib.annotation.Collection(
            domainEvent = OrphanedPermissionsEvent.class,
            typeOf = ApplicationPermission.class
    )
    public Collection<ApplicationPermission> getOrphanedPermissions() {
        return applicationPermissionRepository.findOrphaned();
    }

}
