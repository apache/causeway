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
package org.apache.isis.extensions.secman.model.dom.permission;

import java.util.Collection;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.DomainServiceLayout;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.RestrictTo;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.extensions.secman.api.IsisModuleExtSecmanApi;
import org.apache.isis.extensions.secman.api.permission.ApplicationPermission;
import org.apache.isis.extensions.secman.api.permission.ApplicationPermissionRepository;

@DomainService(
        nature = NatureOfService.VIEW,
        objectType = "isis.ext.secman.ApplicationPermissionMenu"
        )
@DomainServiceLayout(
        named="Security",
        menuBar = DomainServiceLayout.MenuBar.SECONDARY
        )
public class ApplicationPermissionMenu {

    // -- domain event classes
    public static abstract class PropertyDomainEvent<T> extends IsisModuleExtSecmanApi.PropertyDomainEvent<ApplicationPermissionMenu, T> {}
    public static abstract class CollectionDomainEvent<T> extends IsisModuleExtSecmanApi.CollectionDomainEvent<ApplicationPermissionMenu, T> {}
    public static abstract class ActionDomainEvent extends IsisModuleExtSecmanApi.ActionDomainEvent<ApplicationPermissionMenu> {}

    @Inject private ApplicationPermissionRepository<? extends ApplicationPermission> applicationPermissionRepository;

    // -- iconName
    public String iconName() {
        return "applicationPermission";
    }


    // -- findOrphanedPermissions (action)
    public static class FindOrphanedPermissionsDomainEvent extends ActionDomainEvent {}

    @Action(
            domainEvent=FindOrphanedPermissionsDomainEvent.class,
            semantics = SemanticsOf.SAFE
            )
    @MemberOrder(sequence = "100.50.1")
    public Collection<? extends ApplicationPermission> findOrphanedPermissions() {
        return applicationPermissionRepository.findOrphaned();
    }


    // -- allPermissions (action)
    public static class AllPermissionsDomainEvent extends ActionDomainEvent {}

    @Action(
            domainEvent=AllPermissionsDomainEvent.class,
            semantics = SemanticsOf.SAFE,
            restrictTo = RestrictTo.PROTOTYPING
            )
    @MemberOrder(sequence = "100.50.2")
    public Collection<? extends ApplicationPermission> allPermissions() {
        return applicationPermissionRepository.allPermissions();
    }


}
