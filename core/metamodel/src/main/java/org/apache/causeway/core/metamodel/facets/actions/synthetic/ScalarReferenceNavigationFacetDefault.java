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
package org.apache.causeway.core.metamodel.facets.actions.synthetic;

import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.facetapi.FacetAbstract;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;

import lombok.Getter;
import lombok.NonNull;

public class ScalarReferenceNavigationFacetDefault
        extends FacetAbstract
        implements ScalarReferenceNavigationFacet {

    private static Class<? extends Facet> type() {
        return ScalarReferenceNavigationFacet.class;
    }

    @Getter(onMethod_ = @Override)
    private final @NonNull OneToOneAssociation reference;

    public ScalarReferenceNavigationFacetDefault(
            final @NonNull OneToOneAssociation reference,
            final @NonNull FacetHolder holder) {
        super(type(), holder);
        this.reference = reference;
    }

}
