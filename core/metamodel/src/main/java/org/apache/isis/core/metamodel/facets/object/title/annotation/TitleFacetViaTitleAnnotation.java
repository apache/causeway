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

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.apache.isis.applib.annotation.Title;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.compare._Comparators;
import org.apache.isis.commons.internal.functions._Predicates;
import org.apache.isis.commons.internal.reflection._Annotations;
import org.apache.isis.commons.internal.reflection._Reflect.InterfacePolicy;
import org.apache.isis.commons.internal.reflection._Reflect.TypeHierarchyPolicy;
import org.apache.isis.core.config.progmodel.ProgrammingModelConstants.ObjectSupportMethod;
import org.apache.isis.core.config.progmodel.ProgrammingModelConstants.Validation;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.Evaluators;
import org.apache.isis.core.metamodel.facets.Evaluators.MethodEvaluator;
import org.apache.isis.core.metamodel.facets.ImperativeFacet;
import org.apache.isis.core.metamodel.facets.object.title.TitleFacet;
import org.apache.isis.core.metamodel.facets.object.title.TitleFacetAbstract;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.specloader.validator.ValidationFailure;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class TitleFacetViaTitleAnnotation
extends TitleFacetAbstract
implements ImperativeFacet {

    public static Optional<TitleFacet> create(
            final @NonNull Class<?> cls,
            final @NonNull FacetHolder holder){

        val titles = new ArrayDeque<Title>();

        val titleComponents = Evaluators
                .streamEvaluators(cls,
                    annotatedElement->isTitleComponent(annotatedElement, titles::addLast),
                    TypeHierarchyPolicy.EXCLUDE,
                    InterfacePolicy.INCLUDE)
                .filter(evaluator->!isATitleProvidingObjectSupportMethod(
                        evaluator, holder, titles::removeLast))
                .map(evaluator->TitleComponent.of(evaluator, titles.removeFirst()))
              .collect(Can.toCan())
              .sorted(TitleComponent::compareTo);

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
                if(skipTitlePartEvaluator != null
                        && skipTitlePartEvaluator.test(titlePartAdapter)) {
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

    private String titleOf(final ManagedObject adapter) {
        if (adapter == null) {
            return null;
        }
        return adapter.titleString();
    }

    private static String abbreviated(final String str, final int maxLength) {
        return str.length() < maxLength ? str : str.substring(0, maxLength - 3) + "...";
    }

    private static boolean isTitleComponent(
            final AnnotatedElement annotatedElement,
            final Consumer<Title> onTitleFound){
        return _Annotations
                .synthesizeInherited(annotatedElement, Title.class)
                .map(title->{onTitleFound.accept(title); return true;})
                .orElse(false);
    }

    private static boolean isATitleProvidingObjectSupportMethod(
            final Evaluators.Evaluator evaluator,
            final FacetHolder facetHolder,
            final Runnable onTrue) {
        if(ObjectSupportMethod.TITLE.getMethodNames().contains(evaluator.name())) {
            ValidationFailure.raise(facetHolder,
                    Validation.CONFLICTING_TITLE_STRATEGIES
                    .getMessage(facetHolder.getFeatureIdentifier()));
            onTrue.run();
            return true;
        }
        return false;
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class TitleComponent
    implements Comparable<TitleComponent> {

        public static TitleComponent of(
                final Evaluators.Evaluator titleEvaluator,
                final Title annotation) {

            final String deweyOrdinal = annotation != null ? annotation.sequence() : "1.0";
            final String prepend = annotation != null ? annotation.prepend() : " ";
            final String append = annotation != null ? annotation.append() : "";
            final int abbreviateTo = annotation != null ? annotation.abbreviatedTo() : Integer.MAX_VALUE;
            return new TitleComponent(titleEvaluator, deweyOrdinal, prepend, append, abbreviateTo);
        }

        @Getter private final Evaluators.Evaluator titleEvaluator;
        @Getter private final String deweyOrdinal;
        @Getter private final String prepend;
        @Getter private final String append;
        private final int abbreviateTo;

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

        @Override
        public int compareTo(final TitleComponent other) {
            return _Comparators.deweyOrderCompare(this.getDeweyOrdinal(), other.getDeweyOrdinal());
        }
    }
}
