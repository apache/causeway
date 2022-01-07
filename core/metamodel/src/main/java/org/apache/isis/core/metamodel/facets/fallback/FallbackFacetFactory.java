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
package org.apache.isis.core.metamodel.facets.fallback;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.apache.isis.applib.annotations.LabelPosition;
import org.apache.isis.core.config.IsisConfiguration;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.FacetedMethod;
import org.apache.isis.core.metamodel.facets.TypedHolder;

import lombok.val;

/**
 * Central point for providing some kind of default for any {@link Facet}s
 * required by the Apache Isis framework itself.
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

        val facetHolder = processClassContext.getFacetHolder();

        addFacet(new TitleFacetNone(facetHolder));
        addFacet(new PagedFacetFromConfiguration(
                        getConfiguration().getApplib().getAnnotation().getDomainObjectLayout().getPaged(),
                        facetHolder));
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {

        final FacetedMethod facetedMethod = processMethodContext.getFacetHolder();

        addFacet(new NamedFacetFallbackFromMemberName(facetedMethod));

        final FeatureType featureType = facetedMethod.getFeatureType();
        if (featureType.isProperty()) {
            addFacet(new MaxLengthFacetUnlimited(facetedMethod));
            addFacet(new MultiLineFacetNone(facetedMethod));
            addFacet(newPropParamLayoutFacetIfAny(facetedMethod, "propertyLayout", getConfiguration().getApplib().getAnnotation().getPropertyLayout()));
        }
        if (featureType.isAction()) {
            // none
        }
        if (featureType.isCollection()) {
            addFacet(
                    new PagedFacetFromConfiguration(
                            getConfiguration().getApplib().getAnnotation().getCollectionLayout().getPaged(),
                            facetedMethod));
        }

    }

    @Override
    public void processParams(final ProcessParameterContext processParameterContext) {

        final TypedHolder typedHolder = processParameterContext.getFacetHolder();
        if (typedHolder.getFeatureType().isActionParameter()) {
            addFacet(new MultiLineFacetNone(typedHolder));
            addFacet(new MaxLengthFacetUnlimited(typedHolder));

            addFacet(newPropParamLayoutFacetIfAny(typedHolder, "parameterLayout", getConfiguration().getApplib().getAnnotation().getParameterLayout()));
        }

    }

    private Facet newPropParamLayoutFacetIfAny(final FacetHolder facetHolder, final String layoutKey, final IsisConfiguration.Applib.Annotation.ConfigPropsForPropertyOrParameterLayout configPropsHolder) {
        final LabelPosition labelPosition = from(configPropsHolder);
        return new LabelAtFacetFromLayoutConfiguration(labelPosition, facetHolder);
    }

    private static LabelPosition from(final IsisConfiguration.Applib.Annotation.ConfigPropsForPropertyOrParameterLayout configPropsHolder) {
        return configPropsHolder.getLabelPosition();
    }
}
