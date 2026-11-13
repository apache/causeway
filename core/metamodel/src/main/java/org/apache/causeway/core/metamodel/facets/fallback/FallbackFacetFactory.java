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
package org.apache.causeway.core.metamodel.facets.fallback;

import java.util.HashMap;
import java.util.Map;

import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.facetapi.FacetedMethod;
import org.apache.causeway.core.metamodel.facetapi.FeatureType;
import org.apache.causeway.core.metamodel.facetapi.TypedFacetHolder;
import org.apache.causeway.core.metamodel.facets.FacetFactoryAbstract;

import jakarta.inject.Inject;

/**
 * Central point for providing some kind of default for any {@link Facet}s
 * required by the Apache Causeway framework itself.
 *
 */
public class FallbackFacetFactory extends FacetFactoryAbstract {

    @SuppressWarnings("unused")
    private static final Map<Class<?>, Integer> TYPICAL_LENGTHS_BY_CLASS = new HashMap<Class<?>, Integer>() {
        private static final long serialVersionUID = 1L;
        {
            putTypicalLength(byte.class, Byte.class, 3);
            putTypicalLength(short.class, Short.class, 5);
            putTypicalLength(int.class, Integer.class, 10);
            putTypicalLength(long.class, Long.class, 20);
            putTypicalLength(float.class, Float.class, 20);
            putTypicalLength(double.class, Double.class, 20);
            putTypicalLength(char.class, Character.class, 1);
            putTypicalLength(boolean.class, Boolean.class, 1);
        }

        private void putTypicalLength(final Class<?> primitiveClass, final Class<?> wrapperClass, final int length) {
            put(primitiveClass, Integer.valueOf(length));
            put(wrapperClass, Integer.valueOf(length));
        }
    };

    @Inject
    public FallbackFacetFactory(final MetaModelContext mmc) {
        super(mmc, FeatureType.EVERYTHING);
    }

    @Override
    public void process(final ProcessClassContext processClassContext) {
        var facetHolder = processClassContext.facetHolder();

        new TitleFacetNone(facetHolder);
        new PagedFacetFromConfiguration(
                getConfiguration().applib().annotation().domainObjectLayout().paged(),
                facetHolder);
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {
        final FacetedMethod facetedMethod = processMethodContext.facetHolder();

        new NamedFacetFallbackFromMemberName(facetedMethod);

        final FeatureType featureType = facetedMethod.featureType();
        if (featureType.isProperty()) {
            new MaxLengthFacetUnlimited(facetedMethod);
            new MultiLineFacetNone(facetedMethod);
            new LabelAtFacetFromLayoutConfiguration(
                    getConfiguration().applib().annotation().propertyLayout().labelPosition(),
                    facetedMethod);
        }
        if (featureType.isAction()) {
            // none
        }
        if (featureType.isCollection()) {
            new PagedFacetFromConfiguration(
                    getConfiguration().applib().annotation().collectionLayout().paged(),
                    facetedMethod);
        }
    }

    @Override
    public void processParams(final ProcessParameterContext processParameterContext) {
        final TypedFacetHolder typedHolder = processParameterContext.facetHolder();
        if (typedHolder.featureType().isActionParameter()) {
            new MultiLineFacetNone(typedHolder);
            new MaxLengthFacetUnlimited(typedHolder);
            new LabelAtFacetFromLayoutConfiguration(
                    getConfiguration().applib().annotation().parameterLayout().labelPosition(),
                    typedHolder);
        }
    }

}
