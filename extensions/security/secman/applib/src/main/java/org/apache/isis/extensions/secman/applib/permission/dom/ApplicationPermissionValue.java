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
package org.apache.isis.extensions.secman.applib.permission.dom;

import java.io.Serializable;
import java.util.Comparator;

import org.apache.isis.applib.annotations.Programmatic;
import org.apache.isis.applib.services.appfeat.ApplicationFeatureId;
import org.apache.isis.applib.util.ObjectContracts;
import org.apache.isis.applib.util.ToString;

/**
 * A serializable value object representing an (anonymized)
 * {@link ApplicationPermission}.
 *
 * <p>
 *     Intended for value type arithmetic and also for caching.  No user/role information is held because that information
 *     is not required to perform the arithmetic.
 * </p>
 *
 * @since 2.0 {@index}
 */
public class ApplicationPermissionValue implements Comparable<ApplicationPermissionValue>, Serializable {

    private static final long serialVersionUID = 1L;

    // -- constructor
    public ApplicationPermissionValue(
            final ApplicationFeatureId featureId,
            final ApplicationPermissionRule rule,
            final ApplicationPermissionMode mode) {
        this.featureId = featureId;
        this.rule = rule;
        this.mode = mode;
    }


    // -- featureId
    private final ApplicationFeatureId featureId;
    @Programmatic
    public ApplicationFeatureId getFeatureId() {
        return featureId;
    }


    // -- rule
    private final ApplicationPermissionRule rule;
    @Programmatic
    public ApplicationPermissionRule getRule() {
        return rule;
    }


    // -- mode
    private final ApplicationPermissionMode mode;
    @Programmatic
    public ApplicationPermissionMode getMode() {
        return mode;
    }


    // -- implies, refutes
    @Programmatic
    public boolean implies(final ApplicationFeatureId featureId, final ApplicationPermissionMode mode) {
        if(getRule() != ApplicationPermissionRule.ALLOW) {
            // only allow rules can imply
            return false;
        }
        if(getMode() == ApplicationPermissionMode.VIEWING && mode == ApplicationPermissionMode.CHANGING) {
            // an "allow viewing" permission does not imply ability to change
            return false;
        }

        // determine if this permission is on the path (ie the feature or one of its parents)
        return onPathOf(featureId);
    }

    @Programmatic
    public boolean refutes(final ApplicationFeatureId featureId, final ApplicationPermissionMode mode) {
        if(getRule() != ApplicationPermissionRule.VETO) {
            // only veto rules can refute
            return false;
        }
        if(getMode() == ApplicationPermissionMode.CHANGING && mode == ApplicationPermissionMode.VIEWING) {
            // an "veto changing" permission does not refute ability to view
            return false;
        }
        // determine if this permission is on the path (ie the feature or one of its parents)
        return onPathOf(featureId);
    }

    private boolean onPathOf(final ApplicationFeatureId featureId) {

        for (final ApplicationFeatureId pathId : featureId.getPathIds()) {
            if(getFeatureId().equals(pathId)) {
                return true;
            }
        }

        return false;
    }



    // -- Comparators
    public static final class Comparators {
        private Comparators(){}
        public static Comparator<ApplicationPermissionValue> natural() {
            return new ApplicationPermissionValueComparator();
        }

        static class ApplicationPermissionValueComparator implements Comparator<ApplicationPermissionValue>, Serializable {
            private static final long serialVersionUID = 1L;

            @Override
            public int compare(final ApplicationPermissionValue o1, final ApplicationPermissionValue o2) {
                return o1.compareTo(o2);
            }
        }
    }


    // -- CONTRACT

    private static final Comparator<ApplicationPermissionValue> comparator =
            Comparator.comparing(ApplicationPermissionValue::getRule)
            .thenComparing(ApplicationPermissionValue::getMode)
            .thenComparing(ApplicationPermissionValue::getFeatureId);

    private static final ToString<ApplicationPermissionValue> toString =
            ObjectContracts.toString("name", ApplicationPermissionValue::getRule)
            .thenToString("mode", ApplicationPermissionValue::getMode)
            .thenToString("featureId", ApplicationPermissionValue::getFeatureId);


    @Override
    public int compareTo(final ApplicationPermissionValue o) {
        return comparator.compare(this, o);
    }

    @Override
    public boolean equals(final Object o) {
        // not using because trying to be efficient.  Premature optimization?
        // return ObjectContracts.equals(this, obj, propertyNames);
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final ApplicationPermissionValue that = (ApplicationPermissionValue) o;

        if (featureId != null ? !featureId.equals(that.featureId) : that.featureId != null) return false;
        if (mode != that.mode) return false;
        if (rule != that.rule) return false;

        return true;
    }

    @Override
    public int hashCode() {
        // not using because trying to be efficient.  Premature optimization?
        // return ObjectContracts.hashCode(this, propertyNames);
        int result = featureId != null ? featureId.hashCode() : 0;
        result = 31 * result + (rule != null ? rule.hashCode() : 0);
        result = 31 * result + (mode != null ? mode.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return toString.toString(this);
    }


}
