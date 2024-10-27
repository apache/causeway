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
package org.apache.causeway.extensions.secman.integration.permissions;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.apache.causeway.applib.annotation.Programmatic;
import org.apache.causeway.applib.services.appfeat.ApplicationFeatureId;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class ApplicationFeatureIdTransformerV1Compatibility implements ApplicationFeatureIdTransformer {

    private final SpecificationLoader specificationLoader;

    /**
     * local cache.
     */
    private final Map<ApplicationFeatureId, ApplicationFeatureId> transformedByOriginal = new HashMap<>();

    @Programmatic
    @Override
    public ApplicationFeatureId transform(ApplicationFeatureId applicationFeatureId) {
        return transformedByOriginal.computeIfAbsent(applicationFeatureId, this::doTransform);
    }

    private ApplicationFeatureId doTransform(ApplicationFeatureId applicationFeatureId) {
        var logicalTypeName = applicationFeatureId.getLogicalTypeName();
        switch (applicationFeatureId.getSort()) {
            case NAMESPACE:
                return applicationFeatureId;
            case TYPE:
            case MEMBER:
                var logicalTypeNameBasedOnPhysicalName =
                        specificationLoader.specForLogicalTypeName(logicalTypeName)
                        .map(ObjectSpecification::getCorrespondingClass)
                        .map(Class::getName)
                        .orElse(logicalTypeName);
                return applicationFeatureId.withLogicalTypeName(logicalTypeNameBasedOnPhysicalName);
            default:
                // not expected...
                throw new IllegalArgumentException(String.format("ApplicationFeatureId.sort '%s' not recognised", applicationFeatureId.getSort()));
        }
    }

}
