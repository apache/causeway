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
package org.apache.causeway.core.metamodel.spec.impl;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.applib.annotation.Collection;
import org.apache.causeway.applib.annotation.CollectionLayout;
import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.commons.internal.reflection._GenericResolver.ResolvedType;
import org.apache.causeway.core.metamodel.commons.ToString;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.facetapi.FeatureType;
import org.apache.causeway.core.metamodel.facets.FacetedMethod;
import org.apache.causeway.core.metamodel.facets.collections.CollectionFacet;
import org.apache.causeway.core.metamodel.facets.propcoll.accessor.PropertyOrCollectionAccessorFacet;
import org.apache.causeway.core.metamodel.interactions.CollectionUsabilityContext;
import org.apache.causeway.core.metamodel.interactions.CollectionVisibilityContext;
import org.apache.causeway.core.metamodel.interactions.InteractionUtils;
import org.apache.causeway.core.metamodel.interactions.UsabilityContext;
import org.apache.causeway.core.metamodel.interactions.VisibilityContext;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.causeway.core.metamodel.util.Facets;

import lombok.Getter;

class OneToManyAssociationDefault
extends ObjectAssociationAbstract
implements OneToManyAssociation {
    private static final long serialVersionUID = 1L;

    public static OneToManyAssociationDefault forMethod(final FacetedMethod facetedMethod) {
        return new OneToManyAssociationDefault(
                facetedMethod.getFeatureIdentifier(),
                facetedMethod,
                ((SpecificationLoaderInternal)facetedMethod.getMetaModelContext().getSpecificationLoader())
                    .loadSpecification(facetedMethod.resolvedType().elementType()));
    }

    protected OneToManyAssociationDefault(
            final Identifier featureIdentifier,
            final FacetedMethod facetedMethod,
            final ObjectSpecification objectSpec) {
        super(featureIdentifier, facetedMethod, FeatureType.COLLECTION, objectSpec);
    }

    // -- UNDERLYING TYPE

    @Getter(onMethod_={@Override}, lazy = true)
    private final ResolvedType typeOfAnyCardinality = resolveTypeOfAnyCardinality();
    private ResolvedType resolveTypeOfAnyCardinality() {
        return Facets.typeOfAnyCardinality(getFacetHolder())
                .orElseThrow(()->_Exceptions.unrecoverable(
                        "framework bug: non-scalar feature must have a TypeOfFacet"));
    }

    // -- visible, usable

    @Override
    public VisibilityContext createVisibleInteractionContext(
            final ManagedObject ownerAdapter,
            final InteractionInitiatedBy interactionInitiatedBy,
            final Where where) {

        return new CollectionVisibilityContext(
                headFor(ownerAdapter), getFeatureIdentifier(), interactionInitiatedBy, where,
                InteractionUtils.renderPolicy(ownerAdapter));
    }

    @Override
    public UsabilityContext createUsableInteractionContext(
            final ManagedObject ownerAdapter,
            final InteractionInitiatedBy interactionInitiatedBy,
            final Where where) {
        return new CollectionUsabilityContext(
                headFor(ownerAdapter), getFeatureIdentifier(), interactionInitiatedBy, where,
                InteractionUtils.renderPolicy(ownerAdapter));
    }

    // -- get, isEmpty, add, clear

    @Override
    public ManagedObject get(
            final ManagedObject ownerAdapter,
            final InteractionInitiatedBy interactionInitiatedBy) {

        var accessor = getFacet(PropertyOrCollectionAccessorFacet.class);
        var collection = accessor.getAssociationValueAsPojo(ownerAdapter, interactionInitiatedBy);
        if (collection == null) return null;

        return getObjectManager().adapt(collection, this::getElementType);
    }

    @Override
    public boolean isEmpty(
            final ManagedObject ownerAdapter,
            final InteractionInitiatedBy interactionInitiatedBy) {
        // REVIEW should we be able to determine if a collection is empty
        // without loading it?
        final ManagedObject collection = get(ownerAdapter, interactionInitiatedBy);
        return CollectionFacet.elementCount(collection) == 0;
    }

    // -- defaults
    @Override
    public ManagedObject getDefault(final ManagedObject ownerAdapter) {
        return null;
    }

    @Override
    public void toDefault(final ManagedObject ownerAdapter) {
    }

    // -- choices & autoComplete

    @Override
    public Can<ManagedObject> getChoices(
            final ManagedObject ownerAdapter,
            final InteractionInitiatedBy interactionInitiatedBy) {
        return Can.empty();
    }

    @Override
    public boolean hasChoices() {
        return false;
    }

    @Override
    public boolean hasAutoComplete() {
        return false;
    }

    @Override
    public Can<ManagedObject> getAutoComplete(
            final ManagedObject object,
            final String searchArg,
            final InteractionInitiatedBy interactionInitiatedBy) {

        return Can.empty();
    }

    @Override
    public int getAutoCompleteMinLength() {
        return 0; // n/a
    }

    @Getter(lazy=true, onMethod_ = {@Override})
    private final boolean explicitlyAnnotated = calculateIsExplicitlyAnnotated();

    // -- toString

    @Override
    public String toString() {
        final ToString str = new ToString(this);
        str.append(super.toString());
        str.append(",");
        str.append("type", getElementType() == null
                ? "unknown"
                : getElementType().getShortIdentifier());
        return str.toString();
    }

    // -- HELPER

    private boolean calculateIsExplicitlyAnnotated() {
        var methodFacade = getFacetedMethod().methodFacade();
        return methodFacade.synthesize(Collection.class).isPresent()
                || methodFacade.synthesize(CollectionLayout.class).isPresent();
    }

}
