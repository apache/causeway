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

package org.apache.isis.core.progmodel.facets.object.title.annotation;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

import org.apache.isis.applib.annotation.Disabled;
import org.apache.isis.applib.annotation.Title;
import org.apache.isis.core.metamodel.adapter.LocalizationProvider;
import org.apache.isis.core.metamodel.adapter.LocalizationProviderAware;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManagerAware;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.Annotations;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.FacetedMethod;
import org.apache.isis.core.metamodel.facets.hide.HiddenFacet;
import org.apache.isis.core.metamodel.methodutils.MethodScope;
import org.apache.isis.core.progmodel.facets.MethodFinderUtils;
import org.apache.isis.core.progmodel.facets.fallback.FallbackFacetFactory;
import org.apache.isis.core.progmodel.facets.members.disable.DisabledFacet;
import org.apache.isis.core.progmodel.facets.members.disable.annotation.DisabledFacetAnnotation;
import org.apache.isis.core.progmodel.facets.object.title.annotation.TitleFacetViaTitleAnnotation.TitleComponent;

public class TitleAnnotationFacetFactory extends FacetFactoryAbstract implements AdapterManagerAware, LocalizationProviderAware {

    private AdapterManager adapterManager;
    private LocalizationProvider localizationProvider;

    public TitleAnnotationFacetFactory() {
        super(FeatureType.OBJECTS_PROPERTIES_ONLY);
    }

    /**
     * If no method tagged with {@link Title} annotation then will use Facets
     * provided by {@link FallbackFacetFactory} instead.
     */
    @Override
    public void process(final ProcessClassContext processClassContext) {
        final Class<?> cls = processClassContext.getCls();
        final FacetHolder facetHolder = processClassContext.getFacetHolder();

        final List<Method> methods = MethodFinderUtils.findMethodsWithAnnotation(cls, MethodScope.OBJECT, Title.class);

        Collections.sort(methods, new Comparator<Method>() {
            Comparator<String> comparator = new SequenceComparator();

            @Override
            public int compare(final Method o1, final Method o2) {
                final Title a1 = o1.getAnnotation(Title.class);
                final Title a2 = o2.getAnnotation(Title.class);
                return comparator.compare(a1.sequence(), a2.sequence());
            }
        });
        if (methods.isEmpty()) {
            return;
        }
        final List<TitleComponent> titleComponents = Lists.transform(methods, TitleComponent.FROM_METHOD);
        FacetUtil.addFacet(new TitleFacetViaTitleAnnotation(titleComponents, facetHolder, adapterManager, localizationProvider));
    }

    /**
     * Any property annotated with <tt>Title</tt> is hidden by default in tables.
     */
    @Override
    public void process(final ProcessMethodContext processMethodContext) {
        final FacetedMethod facetHolder = processMethodContext.getFacetHolder();
        if(facetHolder.containsFacet(HiddenFacet.class)) {
            // don't overwrite any facet already installed
            return;
        }
        // otherwise, install hidden facet if this property annotated with @Title
        final Title annotation = Annotations.getAnnotation(processMethodContext.getMethod(), Title.class);
        FacetUtil.addFacet(create(annotation, facetHolder));
    }

    private Facet create(final Title annotation, final FacetHolder holder) {
        return annotation != null ? new HiddenFacetInTablesInferredFromTitleAnnotation(holder) : null;
    }




    static class SequenceComparator implements Comparator<String> {

        @Override
        public int compare(final String sequence1, final String sequence2) {

            final List<String> components1 = componentsFor(sequence1);
            final List<String> components2 = componentsFor(sequence2);

            final int size1 = components1.size();
            final int size2 = components2.size();

            if (size1 == 0 && size2 == 0) {
                return 0;
            }

            // continue to loop until we run out of components.
            int n = 0;
            while (true) {
                final int length = n + 1;
                // check if run out of components in either side
                if (size1 < length && size2 >= length) {
                    return -1; // o1 before o2
                }
                if (size2 < length && size1 >= length) {
                    return +1; // o2 before o1
                }
                if (size1 < length && size2 < length) {
                    // run out of components
                    return 0;
                }
                // we have this component on each side
                int componentCompare = 0;
                try {
                    final Integer c1 = Integer.valueOf(components1.get(n));
                    final Integer c2 = Integer.valueOf(components2.get(n));
                    componentCompare = c1.compareTo(c2);
                } catch (final NumberFormatException nfe) {
                    // not integers compare as strings
                    componentCompare = components1.get(n).compareTo(components2.get(n));
                }

                if (componentCompare != 0) {
                    return componentCompare;
                }
                // this component is the same; lets look at the next
                n++;
            }
        }

        private static List<String> componentsFor(final String sequence) {
            return Lists.newArrayList(Splitter.on('.').split(sequence));
        }

    }

    @Override
    public void setLocalizationProvider(final LocalizationProvider localizationProvider) {
        this.localizationProvider = localizationProvider;
    }

    @Override
    public void setAdapterManager(final AdapterManager adapterMap) {
        this.adapterManager = adapterMap;
    }
}
