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

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.annotation.Programmatic;
import org.apache.causeway.applib.services.appfeat.ApplicationFeatureId;

import org.apache.causeway.extensions.secman.applib.permission.dom.ApplicationPermissionValue;

import org.springframework.stereotype.Service;

import javax.annotation.Priority;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Provides an SPI used by {@link PermissionsEvaluationServiceForSecman} that
 * pre-processes {@link ApplicationFeatureId}s before evaluating them.
 *
 * <p>
 *     The primary use case is to enable backward compatibility; rather than evaluating using the
 *     logicalTypeName#memberId, an alternative transformer could use the packageName#memberId (as was done in v1).
 * </p>
 *
 */
public interface ApplicationFeatureIdTransformer {

    @Programmatic
    ApplicationFeatureId transform(ApplicationFeatureId applicationFeatureId);

    @Programmatic
    default Collection<ApplicationPermissionValue> transform(Collection<ApplicationPermissionValue> permissionValues) {
        return permissionValues.stream()
                .map(apv -> apv.withFeatureId(transform(apv.getFeatureId())))
                .collect(Collectors.toList());
    }

    @Service
    @Priority(PriorityPrecedence.LATE)
    class Identity implements ApplicationFeatureIdTransformer {

        @Programmatic
        @Override
        public ApplicationFeatureId transform(ApplicationFeatureId applicationFeatureId) {
            return applicationFeatureId;
        }

        @Programmatic
        @Override
        public Collection<ApplicationPermissionValue> transform(Collection<ApplicationPermissionValue> permissionValues) {
            return permissionValues;
        }
    }
}
