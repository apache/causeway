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
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import org.apache.isis.applib.annotation.Title;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.functions._Predicates;
import org.apache.isis.commons.internal.reflection._Reflect.InterfacePolicy;
import org.apache.isis.commons.internal.reflection._Reflect.TypeHierarchyPolicy;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.Evaluators;
import org.apache.isis.core.metamodel.facets.Evaluators.MethodEvaluator;
import org.apache.isis.core.metamodel.facets.ImperativeFacet;
import org.apache.isis.core.metamodel.facets.object.title.TitleFacet;
import org.apache.isis.core.metamodel.facets.object.title.TitleFacetAbstract;
import org.apache.isis.core.metamodel.spec.ManagedObject;

import lombok.Getter;
import lombok.NonNull;
import lombok.val;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class TitleFacetViaTitleAnnotation
extends TitleFacetAbstract
implements ImperativeFacet {

    public static Optional<TitleFacet> create(
            final @NonNull Class<?> cls,
            final @NonNull FacetHolder holder){

        val titleComponents = Evaluators.streamEvaluators(cls,
                Title.class,
                TypeHierarchyPolicy.EXCLUDE,
                InterfacePolicy.INCLUDE)
                .sorted(TitleAnnotationFacetFactory.getSequenceComparator())
                .map(TitleFacetViaTitleAnnotation.TitleComponent::of)
                .collect(Can.toCan());

        if (titleComponents.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(new TitleFacetViaTitleAnnotation(titleComponents, holder));
    }

    @Getter private final Can<TitleComponent> components;

    @Getter(onMethod_ = {@Override}) private final @NonNull Can<Method> methods;

    protected TitleFacetViaTitleAnnotation(final Can<TitleComponent> components, final FacetHolder holder) {
        super(holder);
        this.components = components;

        // if there is just a single component and it happens to be a method (not a field)
        // we can use imperative facet semantics which allows for TitleFacets to be compared by
        // TitleFacetAbstract#semanticEquals(..) in support of more rigorous MM validation
        this.methods = components.isCardinalityOne()
                ? components
                    .stream()
                    .map(TitleComponent::getTitleEvaluator)
                    .filter(MethodEvaluator.class::isInstance)
                    .map(MethodEvaluator.class::cast)
                    .map(MethodEvaluator::getMethod)
                    .findFirst()
                    .map(ImperativeFacet::singleMethod)
                    .orElse(Can.empty())
                : Can.empty();
    }

    @Override
    public Intent getIntent(final Method method) {
        return Intent.UI_HINT;
    }

    @Override
    public String title(final ManagedObject targetAdapter) {
        return title(_Predicates.alwaysFalse(), targetAdapter);
    }

    private String titleOf(final ManagedObject adapter) {
        if (adapter == null) {
            return null;
        }
        return adapter.titleString();
    }

    private static String abbreviated(final String str, final int maxLength) {
        return str.length() < maxLength ? str : str.substring(0, maxLength - 3) + "...";
    }

    @Override
    public String title(
            final Predicate<ManagedObject> skipTitlePartEvaluator,
            final ManagedObject targetAdapter) {
        val pojo = targetAdapter.getPojo();
        if(pojo==null) {
            return "";
        }
        val stringBuilder = new StringBuilder();
        val objectManager = getObjectManager();

        try {
            for (final TitleComponent component : this.components) {
                final Object titlePart = component.getTitleEvaluator().value(pojo);
                if (titlePart == null) {
                    continue;
                }
                // ignore context, if provided
                val titlePartAdapter = objectManager.adapt(titlePart);
                if(skipTitlePartEvaluator != null && skipTitlePartEvaluator.test(titlePartAdapter)) {
                    continue;
                }
                String title = titleOf(titlePartAdapter);
                if (_Strings.isNullOrEmpty(title)) {
                    // ... use the toString() otherwise
                    // (mostly for benefit of testing...)
                    title = titlePart.toString().trim();
                }
                if(_Strings.isNullOrEmpty(title)) {
                    continue;
                }
                stringBuilder.append(component.getPrepend());
                stringBuilder.append(abbreviated(title, component.abbreviateTo));
                stringBuilder.append(component.getAppend());
            }

            return stringBuilder.toString().trim();
        } catch (final RuntimeException ex) {

            val isUnitTesting = super.getMetaModelContext().getSystemEnvironment().isUnitTesting();

            if(!isUnitTesting) {
                log.warn("Title failure", ex);
            }
            return "Failed Title";
        }
    }

    @Override
    public void visitAttributes(final BiConsumer<String, Object> visitor) {
        super.visitAttributes(visitor);
        if(components != null && _Strings.isNotEmpty(components.toString())) {
            visitor.accept("components", components);
        }
    }

    // -- HELPER

    public static class TitleComponent {

        public static TitleComponent of(final Evaluators.Evaluator<Title> titleEvaluator) {
            final Title annotation = titleEvaluator.getAnnotation();
            final String prepend = annotation != null ? annotation.prepend() : " ";
            final String append = annotation != null ? annotation.append() : "";
            final int abbreviateTo = annotation != null ? annotation.abbreviatedTo() : Integer.MAX_VALUE;
            return new TitleComponent(prepend, append, titleEvaluator, abbreviateTo);
        }

        @Getter private final String prepend;
        @Getter private final String append;
        @Getter private final Evaluators.Evaluator<Title> titleEvaluator;
        private final int abbreviateTo;

        private TitleComponent(
                final String prepend,
                final String append,
                final Evaluators.Evaluator<Title> titleEvaluator,
                final int abbreviateTo) {
            super();
            this.prepend = prepend;
            this.append = append;
            this.titleEvaluator = titleEvaluator;
            this.abbreviateTo = abbreviateTo;
        }

        @Override
        public String toString() {
            final List<String> parts = _Lists.newArrayList();
            if(prepend != null && !_Strings.isNullOrEmpty(prepend.trim())) {
                parts.add("prepend=" + prepend);
            }
            if(append != null && !_Strings.isNullOrEmpty(append.trim())) {
                parts.add("append=" + append);
            }
            if(abbreviateTo != Integer.MAX_VALUE) {
                parts.add("abbreviateTo=" + abbreviateTo);
            }
            return String.join(";", parts);
        }
    }
}
