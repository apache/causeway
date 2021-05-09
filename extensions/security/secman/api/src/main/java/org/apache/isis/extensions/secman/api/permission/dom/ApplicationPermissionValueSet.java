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
import java.util.Collections;
import java.util.List;

import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.services.appfeat.ApplicationFeatureId;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.collections._Multimaps;
import org.apache.isis.extensions.secman.api.IsisModuleExtSecmanApi;
import org.apache.isis.extensions.secman.api.permission.spi.PermissionsEvaluationService;

/**
 * A serializable value object representing a set of (anonymized)
 * {@link ApplicationPermissionValue permission}s.
 *
 * <p>
 *     Intended for value type arithmetic and also for caching.
 * </p>
 *
 * @since 2.0 {@index}
 */
public class ApplicationPermissionValueSet implements Serializable {

    private static final long serialVersionUID = 1L;


    public static abstract class PropertyDomainEvent<T> extends IsisModuleExtSecmanApi.PropertyDomainEvent<ApplicationPermissionValueSet, T> {}

    public static abstract class CollectionDomainEvent<T> extends IsisModuleExtSecmanApi.CollectionDomainEvent<ApplicationPermissionValueSet, T> {}

    public static abstract class ActionDomainEvent extends IsisModuleExtSecmanApi.ActionDomainEvent<ApplicationPermissionValueSet> {}



    // -- values
    private final List<ApplicationPermissionValue> values;
    /**
     * Partitions the {@link ApplicationPermissionValue permissions} by feature and within that orders according to their
     * evaluation precedence.
     *
     * <p>
     *     The following sketches out what is stored:
     * </p>
     * <pre>
     *     com.foo.Bar#bip -> ALLOW, CHANGING
     *                     -> ALLOW, VIEWING
     *                     -> VETO, VIEWING
     *                     -> VETO, CHANGING
     *     com.foo.Bar     -> ALLOW, CHANGING
     *                     -> ALLOW, VIEWING
     *                     -> VETO, VIEWING
     *                     -> VETO, CHANGING
     *     com.foo         -> ALLOW, CHANGING
     *                     -> ALLOW, VIEWING
     *                     -> VETO, VIEWING
     *                     -> VETO, CHANGING
     *     com             -> ALLOW, CHANGING
     *                     -> ALLOW, VIEWING
     *                     -> VETO, VIEWING
     *                     -> VETO, CHANGING
     * </pre>
     *
     * <p>
     *     Note that {@link org.apache.isis.extensions.security.manager.jdo.dom.permission.ApplicationPermissionRule#ALLOW allow} rule
     *     is ordered before {@link org.apache.isis.extensions.security.manager.jdo.dom.permission.ApplicationPermissionRule#VETO veto} rule
     *     meaning that it is checked first and therefore also takes precedence.
     * </p>
     */
    private final _Multimaps.SetMultimap<ApplicationFeatureId, ApplicationPermissionValue> permissionsByFeature =
            _Multimaps.newSortedSetMultimap(
                    Collections.reverseOrder(),
                    null // natural element order
                    );

    /**
     * Note that we require PermissionsEvaluationService to be serializable.
     */
    private PermissionsEvaluationService permissionsEvaluationService;


    // -- constructor

    public ApplicationPermissionValueSet(
            final List<ApplicationPermissionValue> permissionValues,
            final PermissionsEvaluationService permissionsEvaluationService) {

        this.values = Collections.unmodifiableList(_Lists.newArrayList(permissionValues));
        for (final ApplicationPermissionValue permissionValue : permissionValues) {
            final ApplicationFeatureId featureId = permissionValue.getFeatureId();
            permissionsByFeature.putElement(featureId, permissionValue);
        }
        this.permissionsEvaluationService = permissionsEvaluationService;
    }


    // -- grants, evaluate

    public static class Evaluation {
        private final ApplicationPermissionValue permissionValue;
        private final boolean granted;

        public Evaluation(final ApplicationPermissionValue permissionValue, final boolean granted) {
            this.permissionValue = permissionValue;
            this.granted = granted;
        }

        public ApplicationPermissionValue getCause() {
            return permissionValue;
        }

        public boolean isGranted() {
            return granted;
        }
    }

    @Programmatic
    public boolean grants(final ApplicationFeatureId featureId, final ApplicationPermissionMode mode) {
        return evaluate(featureId, mode).isGranted();
    }

    @Programmatic
    public Evaluation evaluate(
            final ApplicationFeatureId featureId,
            final ApplicationPermissionMode mode) {

        for (final ApplicationFeatureId pathId : featureId.getPathIds()) {
            final Collection<ApplicationPermissionValue> permissionValues = permissionsByFeature.get(pathId);
            final Evaluation evaluation = permissionsEvaluationService.evaluate(featureId, mode, permissionValues);
            if(evaluation != null) {
                return evaluation;
            }
        }
        return new Evaluation(null, false);
    }


    // -- equals, hashCode, toString
    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final ApplicationPermissionValueSet that = (ApplicationPermissionValueSet) o;

        return !(values != null ? !values.equals(that.values) : that.values != null);

    }

    @Override
    public int hashCode() {
        return values != null ? values.hashCode() : 0;
    }


    @Override
    public String toString() {
        return "ApplicationPermissionValueSet{" +
                "values=" + values +
                '}';
    }




}
