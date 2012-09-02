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

package org.apache.isis.core.progmodel.facets.fallback;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.FacetedMethod;
import org.apache.isis.core.metamodel.facets.TypedHolder;

/**
 * Central point for providing some kind of default for any {@link Facet}s
 * required by the Apache Isis framework itself.
 * 
 */
public class FallbackFacetFactory extends FacetFactoryAbstract {

    @SuppressWarnings("unused")
    private final static Map<Class<?>, Integer> TYPICAL_LENGTHS_BY_CLASS = new HashMap<Class<?>, Integer>() {
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

    public FallbackFacetFactory() {
        super(FeatureType.EVERYTHING);
    }

    public boolean recognizes(final Method method) {
        return false;
    }

    @Override
    public void process(final ProcessClassContext processClassContaxt) {
        final FacetHolder facetHolder = processClassContaxt.getFacetHolder();

        final DescribedAsFacetNone describedAsFacet = new DescribedAsFacetNone(facetHolder);
        final NotPersistableFacetNull notPersistableFacet = new NotPersistableFacetNull(facetHolder);
        final TitleFacetNone titleFacet = new TitleFacetNone(facetHolder);

        final Facet[] facets = new Facet[] { describedAsFacet,
                // commenting these out, think this whole isNoop business is a
                // little bogus
                // new ImmutableFacetNever(holder),
                notPersistableFacet, titleFacet, };
        FacetUtil.addFacets(facets);
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {
        final List<Facet> facets = new ArrayList<Facet>();

        if (processMethodContext.getFacetHolder() instanceof FacetedMethod) {
            facets.add(new NamedFacetNone(processMethodContext.getFacetHolder()));
            facets.add(new DescribedAsFacetNone(processMethodContext.getFacetHolder()));
            facets.add(new HelpFacetNone(processMethodContext.getFacetHolder()));

            final FacetedMethod facetedMethod = processMethodContext.getFacetHolder();
            final FeatureType featureType = facetedMethod.getFeatureType();
            if (featureType.isProperty()) {
                facets.add(new MaxLengthFacetUnlimited(processMethodContext.getFacetHolder()));
                facets.add(new MultiLineFacetNone(true, processMethodContext.getFacetHolder()));
            }
            if (featureType.isAction()) {
                facets.add(new ActionDefaultsFacetNone(processMethodContext.getFacetHolder()));
                facets.add(new ActionChoicesFacetNone(processMethodContext.getFacetHolder()));
            }
        }

        FacetUtil.addFacets(facets);
    }

    @Override
    public void processParams(final ProcessParameterContext processParameterContext) {
        final List<Facet> facets = new ArrayList<Facet>();

        if (processParameterContext.getFacetHolder() instanceof TypedHolder) {

            final TypedHolder typedHolder = processParameterContext.getFacetHolder();
            if (typedHolder.getFeatureType().isActionParameter()) {
                facets.add(new NamedFacetNone(processParameterContext.getFacetHolder()));
                facets.add(new DescribedAsFacetNone(processParameterContext.getFacetHolder()));
                facets.add(new HelpFacetNone(processParameterContext.getFacetHolder()));
                facets.add(new MultiLineFacetNone(false, processParameterContext.getFacetHolder()));

                facets.add(new MaxLengthFacetUnlimited(processParameterContext.getFacetHolder()));
            }
        }

        FacetUtil.addFacets(facets);
    }

}
