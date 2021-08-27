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

package org.apache.isis.core.metamodel.specloader.specimpl;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.annotation.Collection;
import org.apache.isis.applib.annotation.CollectionLayout;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.reflection._Annotations;
import org.apache.isis.core.metamodel.commons.ToString;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.FacetedMethod;
import org.apache.isis.core.metamodel.facets.collections.CollectionFacet;
import org.apache.isis.core.metamodel.facets.collparam.semantics.CollectionSemantics;
import org.apache.isis.core.metamodel.facets.collparam.semantics.CollectionSemanticsFacet;
import org.apache.isis.core.metamodel.facets.propcoll.accessor.PropertyOrCollectionAccessorFacet;
import org.apache.isis.core.metamodel.interactions.CollectionUsabilityContext;
import org.apache.isis.core.metamodel.interactions.CollectionVisibilityContext;
import org.apache.isis.core.metamodel.interactions.UsabilityContext;
import org.apache.isis.core.metamodel.interactions.VisibilityContext;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;

import lombok.val;

public class OneToManyAssociationDefault
extends ObjectAssociationAbstract
implements OneToManyAssociation {

    public static OneToManyAssociationDefault forMethod(final FacetedMethod facetedMethod) {
        return new OneToManyAssociationDefault(
                facetedMethod.getFeatureIdentifier(),
                facetedMethod,
                facetedMethod.getMetaModelContext().getSpecificationLoader()
                    .loadSpecification(facetedMethod.getType()));
    }

    protected OneToManyAssociationDefault(
            final Identifier featureIdentifier,
            final FacetedMethod facetedMethod,
            final ObjectSpecification objectSpec) {
        super(featureIdentifier, facetedMethod, FeatureType.COLLECTION, objectSpec);
    }

    @Override
    public CollectionSemantics getCollectionSemantics() {
        final CollectionSemanticsFacet facet = getFacet(CollectionSemanticsFacet.class);
        return facet != null ? facet.value() : CollectionSemantics.OTHER_IMPLEMENTATION;
    }

    // -- visible, usable

    @Override
    public VisibilityContext createVisibleInteractionContext(
            final ManagedObject ownerAdapter,
            final InteractionInitiatedBy interactionInitiatedBy,
            final Where where) {
        return new CollectionVisibilityContext(
                headFor(ownerAdapter), getFeatureIdentifier(), interactionInitiatedBy, where);
    }


    @Override
    public UsabilityContext createUsableInteractionContext(
            final ManagedObject ownerAdapter,
            final InteractionInitiatedBy interactionInitiatedBy,
            final Where where) {
        return new CollectionUsabilityContext(
                headFor(ownerAdapter), getFeatureIdentifier(), interactionInitiatedBy, where);
    }




    // -- get, isEmpty, add, clear

    @Override
    public ManagedObject get(
            final ManagedObject ownerAdapter,
            final InteractionInitiatedBy interactionInitiatedBy) {

        final PropertyOrCollectionAccessorFacet accessor = getFacet(PropertyOrCollectionAccessorFacet.class);
        final Object collection = accessor.getProperty(ownerAdapter, interactionInitiatedBy);
        if (collection == null) {
            return null;
        }

        val objectManager = super.getObjectManager();

        super.getServiceInjector().injectServicesInto(collection);

        return objectManager.adapt(collection);
    }

    @Override
    public boolean isEmpty(
            final ManagedObject parentAdapter,
            final InteractionInitiatedBy interactionInitiatedBy) {
        // REVIEW should we be able to determine if a collection is empty
        // without loading it?
        final ManagedObject collection = get(parentAdapter, interactionInitiatedBy);
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

    @Override
    public boolean isExplicitlyAnnotated() {
        //FIXME[ISIS-2774] memoize or even better, make this a final field
        val javaMethod = getFacetedMethod().getMethod();
        return _Annotations.synthesize(javaMethod, Collection.class).isPresent()
                || _Annotations.synthesize(javaMethod, CollectionLayout.class).isPresent();
    }

    // -- toString

    @Override
    public String toString() {
        final ToString str = new ToString(this);
        str.append(super.toString());
        str.append(",");
        str.append("type", getSpecification() == null ? "unknown" : getSpecification().getShortIdentifier());
        return str.toString();
    }


}
