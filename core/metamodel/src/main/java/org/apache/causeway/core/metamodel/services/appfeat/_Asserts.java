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
package org.apache.causeway.core.metamodel.services.appfeat;

import org.apache.causeway.applib.services.appfeat.ApplicationFeatureId;
import org.apache.causeway.applib.services.appfeat.ApplicationFeatureSort;

final class _Asserts {

    public static void ensureNamespace(final ApplicationFeatureId feature) {
        if(feature.getSort() != ApplicationFeatureSort.NAMESPACE) {
            throw new IllegalStateException("Can only be called for a package; " + feature.toString());
        }
    }

    public static void ensureNamespaceOrType(final ApplicationFeatureId applicationFeatureId) {
        if(applicationFeatureId.getSort() != ApplicationFeatureSort.NAMESPACE
                && applicationFeatureId.getSort() != ApplicationFeatureSort.TYPE) {
            throw new IllegalStateException("Can only be called for a package or a class; " + applicationFeatureId.toString());
        }
    }

    public static void ensureType(final ApplicationFeatureId feature) {
        if(feature.getSort() != ApplicationFeatureSort.TYPE) {
            throw new IllegalStateException("Can only be called for a class; " + feature.toString());
        }
    }

    public static void ensureMember(final ApplicationFeatureId feature) {
        if(feature.getSort() != ApplicationFeatureSort.MEMBER) {
            throw new IllegalStateException("Can only be called for a member; " + feature.toString());
        }
    }

}
