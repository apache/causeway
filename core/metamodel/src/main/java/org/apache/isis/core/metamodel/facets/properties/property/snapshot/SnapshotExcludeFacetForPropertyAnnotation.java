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

package org.apache.isis.core.metamodel.facets.properties.property.snapshot;

import java.util.Optional;

import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.Snapshot;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.propcoll.memserexcl.SnapshotExcludeFacet;
import org.apache.isis.core.metamodel.facets.propcoll.memserexcl.SnapshotExcludeFacetAbstract;

public class SnapshotExcludeFacetForPropertyAnnotation
extends SnapshotExcludeFacetAbstract {

    public static Optional<SnapshotExcludeFacet> create(
            final Optional<Property> propertyIfAny,
            final FacetHolder holder) {

        return propertyIfAny
                .map(Property::snapshot)
                .filter(snapshot -> snapshot == Snapshot.EXCLUDED)
                .map(snapshot -> new SnapshotExcludeFacetForPropertyAnnotation(holder));
    }

    private SnapshotExcludeFacetForPropertyAnnotation(final FacetHolder holder) {
        super(holder);
    }

}
