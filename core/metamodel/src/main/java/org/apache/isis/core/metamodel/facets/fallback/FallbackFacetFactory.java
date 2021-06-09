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

import org.apache.isis.applib.annotation.LabelPosition;
import org.apache.isis.core.config.IsisConfiguration;
import org.apache.isis.core.metamodel.commons.StringExtensions;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
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

    public FallbackFacetFactory() {
        super(FeatureType.EVERYTHING);
    }

    @Override
    public void process(final ProcessClassContext processClassContext) {
        final FacetHolder facetHolder = processClassContext.getFacetHolder();

        final DescribedAsFacetNone describedAsFacet = new DescribedAsFacetNone(facetHolder);
        final TitleFacetNone titleFacet = new TitleFacetNone(facetHolder);

        final int pagedStandalone = getConfiguration().getApplib().getAnnotation().getDomainObjectLayout().getPaged();
        final PagedFacetFromConfiguration pagedFacet = new PagedFacetFromConfiguration(pagedStandalone, facetHolder);

        addFacetIfPresent(describedAsFacet);
        // commenting these out, think this whole isNoop business is a little bogus
        //FacetUtil.addFacet(new ImmutableFacetNever(holder));
        addFacetIfPresent(titleFacet);
        addFacetIfPresent(pagedFacet);

    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {

        final FacetedMethod facetedMethod = processMethodContext.getFacetHolder();


        final String id = facetedMethod.getIdentifier().getMemberLogicalName();
        String defaultName = StringExtensions.asNaturalName2(id);

        addFacetIfPresent(new NamedFacetDefault(defaultName, facetedMethod));

        addFacetIfPresent(new DescribedAsFacetNone(facetedMethod));
        addFacetIfPresent(new HelpFacetNone(facetedMethod));


        final FeatureType featureType = facetedMethod.getFeatureType();
        if (featureType.isProperty()) {
            addFacetIfPresent(new MaxLengthFacetUnlimited(facetedMethod));
            addFacetIfPresent(new MultiLineFacetNone(facetedMethod));

            addFacetIfPresent(newPropParamLayoutFacetIfAny(facetedMethod, "propertyLayout", getConfiguration().getApplib().getAnnotation().getPropertyLayout()));
        }
        if (featureType.isAction()) {
            addFacetIfPresent(new ActionDefaultsFacetNone(facetedMethod));
            addFacetIfPresent(new ActionChoicesFacetNone(facetedMethod));
        }
        if (featureType.isCollection()) {
            addFacetIfPresent(new PagedFacetFromConfiguration(getConfiguration().getApplib().getAnnotation().getCollectionLayout().getPaged(), facetedMethod));
        }

    }

    @Override
    public void processParams(final ProcessParameterContext processParameterContext) {

        final TypedHolder typedHolder = processParameterContext.getFacetHolder();
        if (typedHolder.getFeatureType().isActionParameter()) {
            addFacetIfPresent(new NamedFacetNone(typedHolder));
            addFacetIfPresent(new DescribedAsFacetNone(typedHolder));
            addFacetIfPresent(new HelpFacetNone(typedHolder));
            addFacetIfPresent(new MultiLineFacetNone(typedHolder));

            addFacetIfPresent(new MaxLengthFacetUnlimited(typedHolder));

            addFacetIfPresent(newPropParamLayoutFacetIfAny(typedHolder, "parameterLayout", getConfiguration().getApplib().getAnnotation().getParameterLayout()));
        }

    }

    private Facet newPropParamLayoutFacetIfAny(final FacetHolder facetHolder, final String layoutKey, IsisConfiguration.Applib.Annotation.ConfigPropsForPropertyOrParameterLayout configPropsHolder) {
        final LabelPosition labelPosition = from(configPropsHolder);
        return new LabelAtFacetFromLayoutConfiguration(labelPosition, facetHolder);
    }

    private static LabelPosition from(IsisConfiguration.Applib.Annotation.ConfigPropsForPropertyOrParameterLayout configPropsHolder) {
        return configPropsHolder.getLabelPosition();
    }
}
