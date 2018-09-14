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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.isis.applib.annotation.LabelPosition;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.core.commons.lang.StringExtensions;
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

    public final static int PAGE_SIZE_STANDALONE_DEFAULT = 25;
    public final static int PAGE_SIZE_PARENTED_DEFAULT = 12;


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

    @Override
    public void process(final ProcessClassContext processClassContext) {
        final FacetHolder facetHolder = processClassContext.getFacetHolder();

        final DescribedAsFacetNone describedAsFacet = new DescribedAsFacetNone(facetHolder);
        final TitleFacetNone titleFacet = new TitleFacetNone(facetHolder);

        final int pagedStandalone = getPagedConfigSetting("standalone", PAGE_SIZE_STANDALONE_DEFAULT);
        final PagedFacetFromConfiguration pagedFacet = new PagedFacetFromConfiguration(pagedStandalone, facetHolder);

        FacetUtil.addFacet(describedAsFacet);
        // commenting these out, think this whole isNoop business is a little bogus
        //FacetUtil.addFacet(new ImmutableFacetNever(holder)); 
        FacetUtil.addFacet(titleFacet);
        FacetUtil.addFacet(pagedFacet);
        
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {
        final List<Facet> facets = _Lists.newArrayList();

        final FacetedMethod facetedMethod = processMethodContext.getFacetHolder();


        final String id = facetedMethod.getIdentifier().getMemberName();
        String defaultName = StringExtensions.asNaturalName2(id);

        facets.add(new NamedFacetDefault(defaultName, facetedMethod));

        facets.add(new DescribedAsFacetNone(facetedMethod));
        facets.add(new HelpFacetNone(facetedMethod));


        final FeatureType featureType = facetedMethod.getFeatureType();
        if (featureType.isProperty()) {
            facets.add(new MaxLengthFacetUnlimited(facetedMethod));
            facets.add(new MultiLineFacetNone(true, facetedMethod));

            facets.add(newPropParamLayoutFacetIfAny(facetedMethod, "propertyLayout"));
        }
        if (featureType.isAction()) {
            facets.add(new ActionDefaultsFacetNone(facetedMethod));
            facets.add(new ActionChoicesFacetNone(facetedMethod));
        }
        if (featureType.isCollection()) {
            facets.add(new PagedFacetFromConfiguration(getPagedConfigSetting("parented", PAGE_SIZE_PARENTED_DEFAULT), facetedMethod));
        }

        FacetUtil.addFacets(facets);
    }

    @Override
    public void processParams(final ProcessParameterContext processParameterContext) {
        final List<Facet> facets = new ArrayList<Facet>();

        final TypedHolder typedHolder = processParameterContext.getFacetHolder();
        if (typedHolder.getFeatureType().isActionParameter()) {
            facets.add(new NamedFacetNone(typedHolder));
            facets.add(new DescribedAsFacetNone(typedHolder));
            facets.add(new HelpFacetNone(typedHolder));
            facets.add(new MultiLineFacetNone(false, typedHolder));

            facets.add(new MaxLengthFacetUnlimited(typedHolder));

            facets.add(newPropParamLayoutFacetIfAny(typedHolder, "parameterLayout"));
        }

        FacetUtil.addFacets(facets);
    }

    private Facet newPropParamLayoutFacetIfAny(final FacetHolder facetHolder, final String layoutKey) {
        final String[] subKeys = new String[]{ "labelPosition", "label"};
        final String propParamLayout = getPropParamLayoutConfigSetting(layoutKey, subKeys);
        if(propParamLayout != null) {
            try {
                final LabelPosition labelPosition = LabelPosition.valueOf(propParamLayout);
                return new LabelAtFacetFromLayoutConfiguration(labelPosition, facetHolder);
            } catch(IllegalArgumentException ex) {
                // ignore
            }
        }
        return null;
    }

    private int getPagedConfigSetting(final String subkey, final int defaultValue) {
        return getConfiguration().getInteger("isis.viewers.paged." + subkey, defaultValue);
    }

    private String getPropParamLayoutConfigSetting(final String layout, String... subkeys) {
        for (String subkey : subkeys) {
            final String layoutKey = "isis.viewers." + layout + "." + subkey;
            final String value = getConfiguration().getString(layoutKey);
            if(value != null) {
                return value;
            }
        }
        return null;
    }



}
