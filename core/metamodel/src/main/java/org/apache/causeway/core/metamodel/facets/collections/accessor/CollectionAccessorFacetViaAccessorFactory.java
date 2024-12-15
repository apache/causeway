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
package org.apache.causeway.core.metamodel.facets.collections.accessor;

import jakarta.inject.Inject;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.reflection._GenericResolver.ResolvedMethod;
import org.apache.causeway.commons.semantics.AccessorSemantics;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.facetapi.FeatureType;
import org.apache.causeway.core.metamodel.facets.AccessorFacetFactoryAbstract;
import org.apache.causeway.core.metamodel.facets.FacetedMethod;
import org.apache.causeway.core.metamodel.facets.propcoll.accessor.PropertyOrCollectionAccessorFacet;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;

public class CollectionAccessorFacetViaAccessorFactory
extends AccessorFacetFactoryAbstract {

    private static final Can<String> PREFIXES = Can.empty();

    @Inject
    public CollectionAccessorFacetViaAccessorFactory(final MetaModelContext mmc) {
        super(mmc, FeatureType.COLLECTIONS_ONLY, PREFIXES);
    }

    @Override
    public boolean isAssociationAccessor(final ResolvedMethod method) {
        return AccessorSemantics.isCollectionAccessor(method);
    }

    @Override
    protected PropertyOrCollectionAccessorFacet createFacet(
        final ObjectSpecification typeSpec, final ResolvedMethod accessorMethod, final FacetedMethod facetHolder) {
        return new CollectionAccessorFacetViaAccessor(typeSpec, accessorMethod, facetHolder);
    }

}
