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
package org.apache.isis.extensions.secman.api.permission.dom;

import java.io.Serializable;
import java.util.Collection;

import org.apache.isis.applib.services.appfeat.ApplicationFeatureId;

/**
 * Strategy for determining which permission should apply when there are multiple that apply for a particular target
 * feature Id, and which may conflict with each other.
 *
 * <p>
 *     All implementations of this interface must be {@link java.io.Serializable}, because
 *     an instance is serialized into {@link org.apache.isis.extensions.secman.api.permission.ApplicationPermissionValueSet}.
 * </p>
 *
 * @since 2.0 {@index}
 */
public interface PermissionsEvaluationService extends Serializable {

    /**
     * @param targetMemberId - the target (member) feature to be evaluated
     * @param mode - the mode required, ie {@link org.apache.isis.extensions.secman.api.permission.ApplicationPermissionMode#VIEWING viewing} or {@link org.apache.isis.extensions.secman.api.permission.ApplicationPermissionMode#CHANGING changing}.
     * @param permissionValues - permissions to evaluate, guaranteed to passed through in natural order, as per {@link org.apache.isis.extensions.secman.api.permission.ApplicationPermissionValue.Comparators#natural()}.
     */
    ApplicationPermissionValueSet.Evaluation evaluate(
            final ApplicationFeatureId targetMemberId,
            final ApplicationPermissionMode mode,
            final Collection<ApplicationPermissionValue> permissionValues);

}
