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
package org.apache.isis.extensions.secman.integration.permissions;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotations.PriorityPrecedence;
import org.apache.isis.applib.services.appfeat.ApplicationFeatureId;
import org.apache.isis.commons.internal.assertions._Assert;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.config.IsisConfiguration;
import org.apache.isis.core.config.IsisConfiguration.Extensions.Secman;
import org.apache.isis.core.config.IsisConfiguration.Extensions.Secman.PermissionsEvaluationPolicy;
import org.apache.isis.extensions.secman.applib.permission.dom.ApplicationPermissionMode;
import org.apache.isis.extensions.secman.applib.permission.dom.ApplicationPermissionRule;
import org.apache.isis.extensions.secman.applib.permission.dom.ApplicationPermissionValue;
import org.apache.isis.extensions.secman.applib.permission.dom.ApplicationPermissionValueSet;
import org.apache.isis.extensions.secman.applib.permission.spi.PermissionsEvaluationService;

import lombok.NonNull;
import lombok.val;

/**
 * @since 2.0 {@index}
 */
@Service
@Named("isis.ext.secman.PermissionsEvaluationServiceForSecman")
@javax.annotation.Priority(PriorityPrecedence.MIDPOINT)
@Qualifier("Secman")
public class PermissionsEvaluationServiceForSecman
implements PermissionsEvaluationService {

    private static final long serialVersionUID = 1L;

    private final @NonNull PermissionsEvaluationPolicy policy; // serializable

    @Inject
    public PermissionsEvaluationServiceForSecman(final IsisConfiguration isisConfiguration) {
        this.policy = Optional.ofNullable(
                isisConfiguration.getExtensions().getSecman().getPermissionsEvaluationPolicy())
                .orElseGet(()->new Secman().getPermissionsEvaluationPolicy()); // use config defaults as fallback
        _Assert.assertNotNull(policy);
    }

    @Override
    public ApplicationPermissionValueSet.Evaluation evaluate(
            final ApplicationFeatureId targetMemberId,
            final ApplicationPermissionMode mode,
            final Collection<ApplicationPermissionValue> permissionValues) {

        if(_NullSafe.isEmpty(permissionValues)) {
            return null;
        }

        final Collection<ApplicationPermissionValue> ordered = ordered(permissionValues);

        for (final ApplicationPermissionValue permissionValue : ordered) {
            if(permissionValue.implies(targetMemberId, mode)) {
                return new ApplicationPermissionValueSet.Evaluation(permissionValue, true);
            } else if(permissionValue.refutes(targetMemberId, mode)) {
                return new ApplicationPermissionValueSet.Evaluation(permissionValue, false);
            }
        }
        return null;
    }

    /**
     * @implSpec
     *     This implementation relies on the fact that the {@link org.apache.isis.extensions.secman.applib.permission.dom.ApplicationPermissionValue}s are
     * passed through in natural order, with the leading part based on the
     * {@link org.apache.isis.extensions.secman.applib.permission.dom.ApplicationPermissionValue#getRule() rule} and with
     * {@link ApplicationPermissionRule} in turn comparable so that {@link ApplicationPermissionRule#ALLOW allow}
     * is ordered before {@link ApplicationPermissionRule#VETO veto}.
     */
    protected Collection<ApplicationPermissionValue> ordered(
            final Collection<ApplicationPermissionValue> permissionValues) {
        switch (policy) {
        case ALLOW_BEATS_VETO:
            return permissionValues;
        case VETO_BEATS_ALLOW:
            val reversed = _Lists.<ApplicationPermissionValue>newArrayList(permissionValues);
            Collections.reverse(reversed);
            return reversed;
        }
        throw _Exceptions.illegalArgument("PermissionsEvaluationPolicy '%s' not recognised", policy);
    }

}
