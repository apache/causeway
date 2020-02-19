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
package org.apache.isis.extensions.secman.model.app.user;


import java.util.List;

import javax.enterprise.inject.Model;
import javax.inject.Inject;

import org.apache.isis.applib.annotation.Collection;
import org.apache.isis.applib.annotation.CollectionLayout;
import org.apache.isis.applib.services.factory.FactoryService;
import org.apache.isis.core.commons.internal.collections._Lists;
import org.apache.isis.core.metamodel.services.appfeat.ApplicationFeature;
import org.apache.isis.core.metamodel.services.appfeat.ApplicationFeatureRepositoryDefault;
import org.apache.isis.extensions.secman.api.IsisModuleExtSecmanApi;
import org.apache.isis.extensions.secman.api.user.ApplicationUser;

import lombok.RequiredArgsConstructor;
import lombok.val;

@Collection(
        domainEvent = ApplicationUser_permissions.CollectionDomainEvent.class)
@CollectionLayout(
        paged=50,
        defaultView = "table"
        )
@RequiredArgsConstructor
public class ApplicationUser_permissions {

    public static class CollectionDomainEvent 
    extends IsisModuleExtSecmanApi.CollectionDomainEvent<ApplicationUser_permissions, UserPermissionViewModel> {}
    
    @Inject private FactoryService factory;
    @Inject private ApplicationFeatureRepositoryDefault applicationFeatureRepository;

    private final ApplicationUser holder;

    @Model
    public List<UserPermissionViewModel> coll() {
        val allMembers = applicationFeatureRepository.allMembers();
        return asViewModels(allMembers);
    }

    List<UserPermissionViewModel> asViewModels(final java.util.Collection<ApplicationFeature> features) {
        return _Lists.map(
                features,
                UserPermissionViewModel.Functions.asViewModel(holder, factory));
    }



}
