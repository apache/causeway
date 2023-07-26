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

import java.util.Collection;
import java.util.stream.Collectors;

import org.apache.causeway.applib.annotation.Programmatic;
import org.apache.causeway.applib.services.appfeat.ApplicationFeatureId;
import org.apache.causeway.extensions.secman.applib.permission.dom.ApplicationPermissionValue;

/**
 * Provides an SPI used by {@link PermissionsEvaluationServiceForSecman} that
 * pre-processes {@link ApplicationFeatureId}s before evaluating them.
 *
 * <p>
 *     The primary use case is to enable backward compatibility; rather than evaluating using the
 *     logicalTypeName#memberId, an alternative transformer could use the packageName#memberId (as was done in v1).
 *     There is an example of this in the unit tests.
 * </p>
 *
 */
public interface ApplicationFeatureIdTransformer {

    /**
     * Transform the provided {@link ApplicationFeatureId} into a new {@link ApplicationFeatureId} so that it can be
     * evaluated.
     *
     * @param applicationFeatureId - to be transformed
     * @return the transformed {@link ApplicationFeatureId}.
     */
    @Programmatic
    ApplicationFeatureId transform(ApplicationFeatureId applicationFeatureId);

    /**
     * Transform a collection of {@link ApplicationPermissionValue}s, specifically transforming the
     * {@link ApplicationFeatureId} that they relate to (as per {@link ApplicationPermissionValue#getFeatureId()})
     * to some other {@link ApplicationFeatureId}, delegating to {@link #transform(ApplicationFeatureId)}.
     *
     * <p>
     *     This default implementation does not usually need to be overridden (though it could be by an
     *     implementation that wished to cache the transformations, say).
     * </p>
     *
     * @param permissionValues - to be transformed
     * @return the transformed {@link ApplicationPermissionValue}s
     */
    @Programmatic
    default Collection<ApplicationPermissionValue> transform(final Collection<ApplicationPermissionValue> permissionValues) {
        return permissionValues.stream()
                .map(apv -> apv.withFeatureId(transform(apv.getFeatureId())))
                .collect(Collectors.toList());
    }

}
