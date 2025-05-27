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
package org.apache.causeway.core.metamodel.facets.object.title.annotation;

import java.lang.reflect.AnnotatedElement;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.apache.causeway.applib.annotation.Title;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.internal.collections._Lists;
import org.apache.causeway.commons.internal.compare._Comparators;
import org.apache.causeway.commons.internal.reflection._Annotations;
import org.apache.causeway.commons.internal.reflection._MethodFacades;
import org.apache.causeway.commons.internal.reflection._MethodFacades.MethodFacade;
import org.apache.causeway.commons.internal.reflection._Reflect.InterfacePolicy;
import org.apache.causeway.commons.internal.reflection._Reflect.TypeHierarchyPolicy;
import org.apache.causeway.core.config.progmodel.ProgrammingModelConstants.ObjectSupportMethod;
import org.apache.causeway.core.config.progmodel.ProgrammingModelConstants.MessageTemplate;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.Evaluators;
import org.apache.causeway.core.metamodel.facets.Evaluators.MethodEvaluator;
import org.apache.causeway.core.metamodel.facets.ImperativeFacet;
import org.apache.causeway.core.metamodel.facets.object.title.TitleFacet;
import org.apache.causeway.core.metamodel.facets.object.title.TitleFacetAbstract;
import org.apache.causeway.core.metamodel.facets.object.title.TitleRenderRequest;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.specloader.validator.ValidationFailure;

import lombok.AccessLevel;
import lombok.Getter;
import org.jspecify.annotations.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TitleFacetViaTitleAnnotation
extends TitleFacetAbstract
implements ImperativeFacet {

    public static Optional<TitleFacet> create(
            final @NonNull Class<?> cls,
            final @NonNull FacetHolder holder){

        var titles = new ArrayDeque<Title>();

        var titleComponents = Evaluators
                .streamEvaluators(cls,
                    annotatedElement->isTitleComponent(annotatedElement, titles::addLast),
                    TypeHierarchyPolicy.EXCLUDE,
                    InterfacePolicy.INCLUDE)
                .filter(evaluator->!isATitleProvidingObjectSupportMethod(
                        evaluator, holder, titles::removeLast))
                .map(evaluator->TitleComponent.of(evaluator, titles.removeFirst()))
              .collect(Can.toCan())
              // fixes type hierarchy deep search duplicates
              //.distinct((a, b)->a.getTitleEvaluator().name().equals(b.getTitleEvaluator().name()))
              .sorted(TitleComponent::compareTo);

        if (titleComponents.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(new TitleFacetViaTitleAnnotation(titleComponents, holder));
    }

    @Getter private final Can<TitleComponent> components;
    @Getter(onMethod_ = {@Override}) private final @NonNull Can<MethodFacade> methods;

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
                    .map(_MethodFacades::regular)
                    .map(ImperativeFacet::singleMethod)
                    .orElse(Can.empty())
                : Can.empty();
    }

    @Override
    public Intent getIntent() {
        return Intent.UI_HINT;
    }

    @Override
    public String title(final TitleRenderRequest titleRenderRequest) {

        final ManagedObject targetAdapter = titleRenderRequest.object();

        var pojo = targetAdapter.getPojo();
        if(pojo==null) return "";

        var stringBuilder = new StringBuilder();
        var objectManager = getObjectManager();

        try {
            for (final TitleComponent component : this.components) {
                final Object titlePart = component.getTitleEvaluator().value(pojo);
                if (titlePart == null) {
                    continue;
                }
                // ignore context, if provided
                var titlePartAdapter = objectManager.adapt(titlePart);
                if(titleRenderRequest.skipTitlePartEvaluator().test(titlePartAdapter)) {
                    continue;
                }

                //TODO propagate the feature titleRenderRequest
                //component.titleEvaluator.name();

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
            var isUnitTesting = super.getMetaModelContext().getSystemEnvironment().isUnitTesting();
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
        return adapter.getTitle();
    }

    private static String abbreviated(final String str, final int maxLength) {
        return str.length() < maxLength ? str : str.substring(0, maxLength - 3) + "...";
    }

    private static boolean isTitleComponent(
            final AnnotatedElement annotatedElement,
            final Consumer<Title> onTitleFound){
        return _Annotations
                .synthesize(annotatedElement, Title.class)
                .map(title->{onTitleFound.accept(title); return true;})
                .orElse(false);
    }

    private static boolean isATitleProvidingObjectSupportMethod(
            final Evaluators.Evaluator evaluator,
            final FacetHolder facetHolder,
            final Runnable onTrue) {
        if(ObjectSupportMethod.TITLE.getMethodNames().contains(evaluator.name())) {
            ValidationFailure.raise(facetHolder,
                    MessageTemplate.CONFLICTING_TITLE_STRATEGIES
                        .builder()
                        .addVariablesFor(facetHolder.getFeatureIdentifier())
                        .buildMessage());
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
            parts.add("evaluator=" + titleEvaluator.name());
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
