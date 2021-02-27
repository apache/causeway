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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.isis.applib.services.factory.FactoryService;
import org.apache.isis.applib.services.i18n.TranslationService;
import org.apache.isis.applib.spec.Specification;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetAbstract;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.IdentifiedHolder;
import org.apache.isis.core.metamodel.interactions.ProposedHolder;
import org.apache.isis.core.metamodel.interactions.ValidityContext;
import org.apache.isis.core.metamodel.spec.ManagedObject;

public abstract class MustSatisfySpecificationFacetAbstract extends FacetAbstract implements MustSatisfySpecificationFacet {


    public static Class<? extends Facet> type() {
        return MustSatisfySpecificationFacet.class;
    }

    private final List<Specification> specifications;

    /**
     * For testing.
     */
    public List<Specification> getSpecifications() {
        return specifications;
    }

    private final SpecificationEvaluator specificationEvaluator;

    public MustSatisfySpecificationFacetAbstract(
            final List<Specification> specifications,
            final FacetHolder holder) {
        super(type(), holder, Derivation.NOT_DERIVED);

        this.specifications = specifications;

        final TranslationService translationService = getTranslationService();
        // sadness: same as in TranslationFactory
        final String translationContext = ((IdentifiedHolder) holder).getIdentifier().getTranslationContext();

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
    protected static List<Specification> toSpecifications(
            final FactoryService factoryService,
            final Class<? extends Specification>[] classes) {
        List<Specification> specifications = Arrays.stream(classes)
                .map(factoryService::getOrCreate)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        return specifications;
    }


    @Override public void appendAttributesTo(final Map<String, Object> attributeMap) {
        super.appendAttributesTo(attributeMap);
        attributeMap.put("specifications", specifications);
    }
}
