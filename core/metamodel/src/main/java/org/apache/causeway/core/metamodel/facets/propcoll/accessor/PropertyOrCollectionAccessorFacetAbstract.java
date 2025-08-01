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
package org.apache.causeway.core.metamodel.facets.propcoll.accessor;

import java.util.function.BiConsumer;

import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.facetapi.FacetAbstract;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;

public abstract class PropertyOrCollectionAccessorFacetAbstract
extends FacetAbstract
implements PropertyOrCollectionAccessorFacet {

    private final ObjectSpecification declaringType;

    private static final Class<? extends Facet> type() {
        return PropertyOrCollectionAccessorFacet.class;
    }

    public PropertyOrCollectionAccessorFacetAbstract(
            final ObjectSpecification declaringType,
            final FacetHolder holder) {
        super(type(), holder);
        this.declaringType = declaringType;
    }

    @Override
    public ObjectSpecification getDeclaringType() {
        return declaringType;
    }

    @Override
    public void visitAttributes(final BiConsumer<String, Object> visitor) {
        super.visitAttributes(visitor);
        visitor.accept("declaringType", declaringType);
    }

    protected final boolean isConfiguredToFilterForVisibility() {
        return getConfiguration().core().metaModel().filterVisibility();
    }

}