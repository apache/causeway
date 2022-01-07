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
package org.apache.isis.extensions.secman.applib.feature.contributions;

import javax.inject.Inject;

import org.apache.isis.applib.IsisModuleApplib;
import org.apache.isis.applib.annotations.Action;
import org.apache.isis.applib.annotations.Collection;
import org.apache.isis.applib.annotations.CollectionLayout;
import org.apache.isis.applib.services.appfeatui.ApplicationFeatureViewModel;
import org.apache.isis.extensions.secman.applib.permission.dom.ApplicationPermission;
import org.apache.isis.extensions.secman.applib.permission.dom.ApplicationPermissionRepository;

import lombok.RequiredArgsConstructor;

@Collection(
) @CollectionLayout(
        defaultView = "table",
        sequence = "10"
)
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class ApplicationFeatureViewModel_permissions {

    private final ApplicationFeatureViewModel viewModel;

    public static class ActionDomainEvent extends IsisModuleApplib.ActionDomainEvent<ApplicationFeatureViewModel_permissions> {}

    @Inject private ApplicationPermissionRepository applicationPermissionRepository;

    @Action(
            domainEvent = ApplicationFeatureViewModel_permissions.ActionDomainEvent.class
    )
    public java.util.Collection<ApplicationPermission> coll() {
        return applicationPermissionRepository.findByFeatureCached(viewModel.getFeatureId());
    }


}
