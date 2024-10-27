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
package org.apache.causeway.core.metamodel.facets.collections.collection;

import java.util.Optional;

import javax.inject.Inject;

import org.apache.causeway.applib.annotation.Collection;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.commons.semantics.CollectionSemantics;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.facetapi.FeatureType;
import org.apache.causeway.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.causeway.core.metamodel.facets.FacetedMethod;
import org.apache.causeway.core.metamodel.facets.actcoll.typeof.TypeOfFacet;
import org.apache.causeway.core.metamodel.facets.actions.contributing.ContributingFacetAbstract;
import org.apache.causeway.core.metamodel.facets.actions.semantics.ActionSemanticsFacetAbstract;
import org.apache.causeway.core.metamodel.facets.collections.collection.modify.CollectionDomainEventFacet;
import org.apache.causeway.core.metamodel.facets.collections.collection.typeof.TypeOfFacetForCollectionAnnotation;
import org.apache.causeway.core.metamodel.facets.propcoll.accessor.PropertyOrCollectionAccessorFacet;
import org.apache.causeway.core.metamodel.specloader.validator.ValidationFailureUtils;

public class CollectionAnnotationFacetFactory
extends FacetFactoryAbstract {

    @Inject
    public CollectionAnnotationFacetFactory(final MetaModelContext mmc) {
        super(mmc, FeatureType.COLLECTIONS_AND_ACTIONS);
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {

        var collectionIfAny = collectionIfAny(processMethodContext);

        if(processMethodContext.isMixinMain()) {
            collectionIfAny.ifPresent(collection->{
                inferMixinSort(collection, processMethodContext.getFacetHolder());
            });
        }

        processDomainEvent(processMethodContext, collectionIfAny);
        processTypeOf(processMethodContext, collectionIfAny);
    }

    Optional<Collection> collectionIfAny(final ProcessMethodContext processMethodContext) {
        return processMethodContext
            .synthesizeOnMethodOrMixinType(
                    Collection.class,
                    () -> ValidationFailureUtils
                    .raiseAmbiguousMixinAnnotations(processMethodContext.getFacetHolder(), Collection.class));
    }

    void inferMixinSort(final Collection collection, final FacetedMethod facetedMethod) {
        /* if @Collection detected on method or type level infer:
         * @Action(semantics=SAFE) */
        addFacet(new ActionSemanticsFacetAbstract(SemanticsOf.SAFE, facetedMethod) {});
        addFacet(ContributingFacetAbstract.createAsCollection(facetedMethod));
    }

    void processDomainEvent(final ProcessMethodContext processMethodContext, final Optional<Collection> collectionIfAny) {

        var cls = processMethodContext.getCls();
        var holder = processMethodContext.getFacetHolder();

        var getterFacetIfAny = holder.lookupFacet(PropertyOrCollectionAccessorFacet.class);

        final boolean isCollection = getterFacetIfAny.isPresent()
                || (processMethodContext.isMixinMain()
                        && collectionIfAny.isPresent());

        if(!isCollection) return; // bale out if method is not representing a collection (no matter mixed-in or not)

        //
        // Set up CollectionDomainEventFacet, which will act as the hiding/disabling/validating advisor
        //

        // search for @Collection(domainEvent=...)
        addFacet(
            CollectionDomainEventFacet
                .create(collectionIfAny, cls, holder));
    }

    void processTypeOf(final ProcessMethodContext processMethodContext, final Optional<Collection> collectionIfAny) {

        var facetHolder = processMethodContext.getFacetHolder();
        var method = processMethodContext.getMethod();

        var methodReturnType = method.getReturnType();
        CollectionSemantics.valueOf(methodReturnType)
        .ifPresent(collectionType->{
            addFacetIfPresent(
                    // check for @Collection(typeOf=...)
                    TypeOfFacetForCollectionAnnotation
                    .create(collectionIfAny, collectionType, facetHolder)
                    .or(
                        // else infer from return type
                        ()-> TypeOfFacet.inferFromMethodReturnType(
                                method,
                                facetHolder))
                );

        });
    }

}
