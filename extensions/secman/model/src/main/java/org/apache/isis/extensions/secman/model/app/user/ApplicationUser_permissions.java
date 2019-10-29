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

import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.CollectionLayout;
import org.apache.isis.applib.annotation.Contributed;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Mixin;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.services.factory.FactoryService;
import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.extensions.secman.api.SecurityModule;
import org.apache.isis.extensions.secman.api.user.ApplicationUser;
import org.apache.isis.metamodel.services.appfeat.ApplicationFeature;
import org.apache.isis.metamodel.services.appfeat.ApplicationFeatureRepositoryDefault;

import lombok.RequiredArgsConstructor;
import lombok.val;

@Mixin @RequiredArgsConstructor
public class ApplicationUser_permissions {

    public static class ActionDomainEvent extends SecurityModule.ActionDomainEvent<ApplicationUser_permissions> {}

    private final ApplicationUser holder;

    @Action(
            semantics = SemanticsOf.SAFE,
            domainEvent = ActionDomainEvent.class
            )
    @ActionLayout(
            contributed = Contributed.AS_ASSOCIATION
            )
    @CollectionLayout(
            paged=50,
            defaultView = "table"
            )
    @MemberOrder(sequence = "30")
    public List<UserPermissionViewModel> $$() {
        val allMembers = applicationFeatureRepository.allMembers();
        return asViewModels(allMembers);
    }

    List<UserPermissionViewModel> asViewModels(final Collection<ApplicationFeature> features) {
        return _Lists.map(
                features,
                UserPermissionViewModel.Functions.asViewModel(holder, factory));
    }

    @Inject RepositoryService repository;
    @Inject FactoryService factory;
    @Inject ApplicationFeatureRepositoryDefault applicationFeatureRepository;

}
