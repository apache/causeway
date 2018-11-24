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

package org.apache.isis.core.metamodel.facets;

import java.util.Map;

import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetAbstract;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;

public abstract class SingleClassValueFacetAbstract extends FacetAbstract implements SingleClassValueFacet {

    private final Class<?> value;
    private final SpecificationLoader specificationLoader;

    public SingleClassValueFacetAbstract(
            final Class<? extends Facet> facetType,
            final FacetHolder holder,
            final Class<?> value,
            final SpecificationLoader specificationLoader) {
        super(facetType, holder, Derivation.NOT_DERIVED);
        this.value = value;
        this.specificationLoader = specificationLoader;
    }

    @Override
    public Class<?> value() {
        return value;
    }

    /**
     * The {@link ObjectSpecification} of the {@link #value()}.
     */
    @Override
    public ObjectSpecification valueSpec() {
        final Class<?> valueType = value();
        return valueType != null ? getSpecificationLoader().loadSpecification(valueType) : null;
    }

    private SpecificationLoader getSpecificationLoader() {
        return specificationLoader;
    }

    @Override public void appendAttributesTo(final Map<String, Object> attributeMap) {
        super.appendAttributesTo(attributeMap);
        attributeMap.put("value", value());
    }

}
