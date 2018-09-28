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

import java.util.List;

import org.apache.isis.applib.services.i18n.TranslationService;
import org.apache.isis.applib.services.wrapper.events.ValidityEvent;
import org.apache.isis.applib.spec.Specification;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetAbstract;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.IdentifiedHolder;
import org.apache.isis.core.metamodel.interactions.ProposedHolder;
import org.apache.isis.core.metamodel.interactions.ValidityContext;
import org.apache.isis.core.metamodel.services.ServicesInjector;
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
            final FacetHolder holder,
            final ServicesInjector servicesInjector) {
        super(type(), holder, Derivation.NOT_DERIVED);
        inject(specifications, servicesInjector);
        this.specifications = specifications;

        final TranslationService translationService = servicesInjector.lookupService(TranslationService.class);
        // sadness: same as in TranslationFactory
        final String translationContext = ((IdentifiedHolder) holder).getIdentifier().toClassAndNameIdentityString();

        specificationEvaluator = new SpecificationEvaluator(translationService, translationContext);
    }

    private static void inject(
            final List specifications, final ServicesInjector servicesInjector) {
        servicesInjector.injectServicesInto(specifications);
    }

    @Override
    public String invalidates(final ValidityContext<? extends ValidityEvent> validityContext) {
        if (!(validityContext instanceof ProposedHolder)) {
            return null;
        }
        final ProposedHolder proposedHolder = (ProposedHolder) validityContext;
        final ManagedObject proposedAdapter = proposedHolder.getProposed();
        if(proposedAdapter == null) {
            return null;
        }
        final Object proposedObject = proposedAdapter.getObject();
        return specificationEvaluator.evaluation()
                .evaluate(specifications, proposedObject)
                .getReason();
    }

    /**
     * For benefit of subclasses.
     */
    protected static List<Specification> specificationsFor(final Class<?>[] values) {
        final List<Specification> specifications = _Lists.newArrayList();
        for (final Class<?> value : values) {
            final Specification specification = newSpecificationElseNull(value);
            if (specification != null) {
                specifications.add(specification);
            }
        }
        return specifications;
    }


    /**
     * For benefit of subclasses.
     */
    protected static Specification newSpecificationElseNull(final Class<?> value) {
        if (!(Specification.class.isAssignableFrom(value))) {
            return null;
        }
        try {
            return (Specification) value.newInstance();
        } catch (final InstantiationException | IllegalAccessException e) {
            return null;
        }
    }

}
