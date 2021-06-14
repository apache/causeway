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

package org.apache.isis.core.metamodel.facets.properties.defaults.fromtype;

import java.util.function.BiConsumer;

import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetAbstract;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.object.defaults.DefaultedFacet;
import org.apache.isis.core.metamodel.facets.properties.defaults.PropertyDefaultFacet;
import org.apache.isis.core.metamodel.spec.ManagedObject;

import lombok.NonNull;

public class PropertyDefaultFacetDerivedFromDefaultedFacet
extends FacetAbstract
implements PropertyDefaultFacet {

    private final @NonNull DefaultedFacet typeFacet;

    public PropertyDefaultFacetDerivedFromDefaultedFacet(
            final DefaultedFacet typeFacet, final FacetHolder holder) {

        super(PropertyDefaultFacet.class, holder);
        this.typeFacet = typeFacet;
    }

    @Override
    public ManagedObject getDefault(final ManagedObject inObject) {
        if (getFacetHolder() == null) {
            return null;
        }
        final Object typeFacetDefault = typeFacet.getDefault();
        if (typeFacetDefault == null) {
            return null;
        }
        return getObjectManager().adapt(typeFacetDefault);
    }

    @Override
    public void visitAttributes(final BiConsumer<String, Object> visitor) {
        super.visitAttributes(visitor);
        visitor.accept("typeFacet", typeFacet);
    }

    @Override
    public boolean semanticEquals(final @NonNull Facet other) {
        return other instanceof PropertyDefaultFacetDerivedFromDefaultedFacet
                ? this.typeFacet
                        .semanticEquals(((PropertyDefaultFacetDerivedFromDefaultedFacet)other).typeFacet)
                : false;
    }

}
