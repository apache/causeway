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

package org.apache.isis.core.metamodel.facets.objectvalue.mustsatisfyspec;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.BiConsumer;

import org.apache.isis.applib.services.factory.FactoryService;
import org.apache.isis.applib.services.i18n.TranslationContext;
import org.apache.isis.applib.services.i18n.TranslationService;
import org.apache.isis.applib.spec.Specification;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetAbstract;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.interactions.ProposedHolder;
import org.apache.isis.core.metamodel.interactions.ValidityContext;
import org.apache.isis.core.metamodel.spec.ManagedObject;

import lombok.Getter;
import lombok.NonNull;

public abstract class MustSatisfySpecificationFacetAbstract
extends FacetAbstract
implements MustSatisfySpecificationFacet {

    private static final Class<? extends Facet> type() {
        return MustSatisfySpecificationFacet.class;
    }

    @Getter //for testing
    private final @NonNull Can<Specification> specifications;

    private final SpecificationEvaluator specificationEvaluator;

    public MustSatisfySpecificationFacetAbstract(
            final Can<Specification> specifications,
            final FacetHolder holder) {
        super(type(), holder);

        this.specifications = specifications;

        final TranslationService translationService = getTranslationService();
        // sadness: same as in TranslationFactory
        final TranslationContext translationContext = TranslationContext.forTranslationContextHolder(
                holder.getFeatureIdentifier()); // .getTranslationContext();

        specificationEvaluator = new SpecificationEvaluator(translationService, translationContext);
    }

    @Override
    public String invalidates(final ValidityContext validityContext) {
        if (!(validityContext instanceof ProposedHolder)) {
            return null;
        }
        final ProposedHolder proposedHolder = (ProposedHolder) validityContext;
        final ManagedObject proposedAdapter = proposedHolder.getProposed();
        if(proposedAdapter == null) {
            return null;
        }
        final Object proposedObject = proposedAdapter.getPojo();
        return specificationEvaluator.evaluation()
                .evaluate(specifications, proposedObject)
                .getReason();
    }

    /**
     * For benefit of subclasses.
     */
    protected static Can<Specification> toSpecifications(
            final FactoryService factoryService,
            final Class<? extends Specification>[] classes) {
        return Arrays.stream(classes)
                .map(factoryService::getOrCreate)
                .collect(Can.toCan());
    }

    @Override
    public void visitAttributes(final BiConsumer<String, Object> visitor) {
        super.visitAttributes(visitor);
        visitor.accept("specifications", specifications);
    }

    @Override
    public boolean semanticEquals(final @NonNull Facet other) {
        return other instanceof MustSatisfySpecificationFacetAbstract
                ? Objects.equals(
                        this
                            .specifications.map(Specification::getClass),
                        ((MustSatisfySpecificationFacetAbstract)other)
                            .specifications.map(Specification::getClass))
                : false;
    }


}
