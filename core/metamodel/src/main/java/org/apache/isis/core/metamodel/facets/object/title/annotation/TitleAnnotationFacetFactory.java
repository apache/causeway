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

package org.apache.isis.core.metamodel.facets.object.title.annotation;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

import org.apache.isis.applib.annotation.Title;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.metamodel.adapter.ObjectAdapterProvider;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facetapi.MetaModelValidatorRefiner;
import org.apache.isis.core.metamodel.facets.Annotations;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.MethodFinderUtils;
import org.apache.isis.core.metamodel.facets.fallback.FallbackFacetFactory;
import org.apache.isis.core.metamodel.methodutils.MethodScope;
import org.apache.isis.core.metamodel.services.ServicesInjector;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorComposite;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorVisiting;
import org.apache.isis.core.metamodel.specloader.validator.ValidationFailures;

public class TitleAnnotationFacetFactory extends FacetFactoryAbstract implements MetaModelValidatorRefiner {

    private static final String TITLE_METHOD_NAME = "title";


    public TitleAnnotationFacetFactory() {
        super(FeatureType.OBJECTS_ONLY);
    }

    /**
     * If no method tagged with {@link Title} annotation then will use Facets
     * provided by {@link FallbackFacetFactory} instead.
     */
    @Override
    public void process(final ProcessClassContext processClassContext) {
        final Class<?> cls = processClassContext.getCls();
        final FacetHolder facetHolder = processClassContext.getFacetHolder();

        final List<Annotations.Evaluator<Title>> evaluators = Annotations.getEvaluators(cls, Title.class);
        if (evaluators.isEmpty()) {
            return;
        }

        sort(evaluators);
        final List<TitleFacetViaTitleAnnotation.TitleComponent> titleComponents = 
                _Lists.transform(evaluators, TitleFacetViaTitleAnnotation.TitleComponent.FROM_EVALUATORS);
        FacetUtil.addFacet(new TitleFacetViaTitleAnnotation(titleComponents, facetHolder, adapterProvider));
    }

    public static void sort(final List<Annotations.Evaluator<Title>> evaluators) {
        Collections.sort(evaluators, new Comparator<Annotations.Evaluator<Title>>() {
            Comparator<String> comparator = new SequenceComparator();

            @Override
            public int compare(final Annotations.Evaluator<Title> o1, final Annotations.Evaluator<Title> o2) {
                final Title a1 = o1.getAnnotation();
                final Title a2 = o2.getAnnotation();
                return comparator.compare(a1.sequence(), a2.sequence());
            }
        });
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


    /**
     * Violation if there is a class that has both a <tt>title()</tt> method and also any non-inherited method
     * annotated with <tt>@Title</tt>.
     *
     * <p>
     * If there are only inherited methods annotated with <tt>@Title</tt> then this is <i>not</i> a violation; but
     * (from the implementation of {@link org.apache.isis.core.metamodel.facets.object.title.methods.TitleFacetViaMethodsFactory} the imperative <tt>title()</tt> method will take
     * precedence.
     */
    @Override
    public void refineMetaModelValidator(MetaModelValidatorComposite metaModelValidator, IsisConfiguration configuration) {
        metaModelValidator.add(new MetaModelValidatorVisiting(new MetaModelValidatorVisiting.Visitor() {

            @Override
            public boolean visit(ObjectSpecification objectSpec, ValidationFailures validationFailures) {
                final Class<?> cls = objectSpec.getCorrespondingClass();

                final Method titleMethod = MethodFinderUtils.findMethod(cls, MethodScope.OBJECT, TITLE_METHOD_NAME, String.class, null);
                if (titleMethod == null) {
                    return true;
                }

                // determine if cls contains an @Title annotated method, not inherited from superclass
                final Class<?> supClass = cls.getSuperclass();
                if (supClass == null) {
                    return true;
                }

                final List<Method> methods = methodsWithTitleAnnotation(cls);
                final List<Method> superClassMethods = methodsWithTitleAnnotation(supClass);
                if (methods.size() > superClassMethods.size()) {
                    validationFailures.add(
                            "%s: conflict for determining a strategy for retrieval of title for class, contains a method '%s' and an annotation '@%s'",
                            objectSpec.getIdentifier().getClassName(),
                            TITLE_METHOD_NAME,
                            Title.class.getName());
                }

                return true;
            }

            private List<Method> methodsWithTitleAnnotation(final Class<?> cls) {
                return MethodFinderUtils.findMethodsWithAnnotation(cls, MethodScope.OBJECT, Title.class);
            }

        }));
    }


    @Override
    public void setServicesInjector(final ServicesInjector servicesInjector) {
        super.setServicesInjector(servicesInjector);
        adapterProvider = servicesInjector.getPersistenceSessionServiceInternal();
    }

    ObjectAdapterProvider adapterProvider;

}
