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
package org.apache.isis.extensions.secman.applib.permission.dom.mixins;

import javax.inject.Inject;

import org.apache.isis.applib.annotations.MemberSupport;
import org.apache.isis.applib.annotations.Property;
import org.apache.isis.applib.annotations.PropertyLayout;
import org.apache.isis.applib.annotations.Where;
import org.apache.isis.applib.services.appfeat.ApplicationFeatureId;
import org.apache.isis.applib.services.appfeat.ApplicationFeatureRepository;
import org.apache.isis.applib.services.appfeatui.ApplicationFeatureViewModel;
import org.apache.isis.applib.services.factory.FactoryService;
import org.apache.isis.extensions.secman.applib.IsisModuleExtSecmanApplib;
import org.apache.isis.extensions.secman.applib.permission.dom.ApplicationPermission;

import lombok.RequiredArgsConstructor;

@Property(
        domainEvent = ApplicationPermission_feature.PropertyDomainEvent.class
)
@PropertyLayout(
        fieldSetId="feature", sequence = "4",
        hidden=Where.REFERENCES_PARENT
)
@RequiredArgsConstructor
public class ApplicationPermission_feature {

    public static class PropertyDomainEvent
            extends IsisModuleExtSecmanApplib.PropertyDomainEvent<ApplicationPermission_feature, ApplicationFeatureViewModel> {}

    final ApplicationPermission target;

    @Inject FactoryService factory;
    @Inject ApplicationFeatureRepository featureRepository;

    @MemberSupport public ApplicationFeatureViewModel prop(final ApplicationPermission permission) {
        if(permission.getFeatureSort() == null) {
            return null;
        }
        final ApplicationFeatureId featureId = getFeatureId(permission);
        return ApplicationFeatureViewModel.newViewModel(featureId, featureRepository, factory);
    }

    private static ApplicationFeatureId getFeatureId(final ApplicationPermission permission) {
        return ApplicationFeatureId.newFeature(permission.getFeatureSort(), permission.getFeatureFqn());
    }


}
