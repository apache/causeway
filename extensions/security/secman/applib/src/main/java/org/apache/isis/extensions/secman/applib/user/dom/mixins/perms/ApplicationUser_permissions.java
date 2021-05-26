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
package org.apache.isis.extensions.secman.applib.user.dom.mixins.perms;


import java.util.List;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Collection;
import org.apache.isis.applib.annotation.CollectionLayout;
import org.apache.isis.applib.annotation.MemberSupport;
import org.apache.isis.applib.services.appfeat.ApplicationFeatureRepository;
import org.apache.isis.applib.services.factory.FactoryService;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.extensions.secman.applib.IsisModuleExtSecmanApplib;
import org.apache.isis.extensions.secman.applib.user.dom.ApplicationUser;

import lombok.RequiredArgsConstructor;
import lombok.val;

@Collection(
        domainEvent = ApplicationUser_permissions.DomainEvent.class)
@CollectionLayout(
        paged=50,
        defaultView = "table"
        )
@RequiredArgsConstructor
public class ApplicationUser_permissions {

    public static class DomainEvent
            extends IsisModuleExtSecmanApplib.CollectionDomainEvent<ApplicationUser_permissions, UserPermissionViewModel> {}

    @Inject private FactoryService factory;
    @Inject private ApplicationFeatureRepository applicationFeatureRepository;

    private final ApplicationUser target;

    @MemberSupport
    public List<UserPermissionViewModel> coll() {
        val allMembers = applicationFeatureRepository.allMembers();
        return _Lists.map(
                allMembers,
                UserPermissionViewModel.asViewModel(target, factory)
        );
    }


}
