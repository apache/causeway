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
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.services.command.Command;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.commons.ToString;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.consent.InteractionResult;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.FacetedMethod;
import org.apache.isis.core.metamodel.facets.all.described.ColumnDescribedFacet;
import org.apache.isis.core.metamodel.facets.all.named.ColumnNamedFacet;
import org.apache.isis.core.metamodel.facets.objectvalue.mandatory.MandatoryFacet;
import org.apache.isis.core.metamodel.facets.param.autocomplete.MinLengthUtil;
import org.apache.isis.core.metamodel.facets.propcoll.accessor.PropertyOrCollectionAccessorFacet;
import org.apache.isis.core.metamodel.facets.properties.autocomplete.PropertyAutoCompleteFacet;
import org.apache.isis.core.metamodel.facets.properties.choices.PropertyChoicesFacet;
import org.apache.isis.core.metamodel.facets.properties.defaults.PropertyDefaultFacet;
import org.apache.isis.core.metamodel.facets.properties.update.clear.PropertyClearFacet;
import org.apache.isis.core.metamodel.facets.properties.update.init.PropertyInitializationFacet;
import org.apache.isis.core.metamodel.facets.properties.update.modify.PropertySetterFacet;
import org.apache.isis.core.metamodel.interactions.InteractionHead;
import org.apache.isis.core.metamodel.interactions.InteractionUtils;
import org.apache.isis.core.metamodel.interactions.PropertyModifyContext;
import org.apache.isis.core.metamodel.interactions.PropertyUsabilityContext;
import org.apache.isis.core.metamodel.interactions.PropertyVisibilityContext;
import org.apache.isis.core.metamodel.interactions.UsabilityContext;
import org.apache.isis.core.metamodel.interactions.ValidityContext;
import org.apache.isis.core.metamodel.interactions.VisibilityContext;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ManagedObjects.EntityUtil;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;

import lombok.val;

public class OneToOneAssociationDefault
extends ObjectAssociationAbstract
implements OneToOneAssociation {

    public static OneToOneAssociationDefault forMethod(final FacetedMethod facetedMethod) {
        return new OneToOneAssociationDefault(
                facetedMethod.getFeatureIdentifier(),
                facetedMethod,
                facetedMethod.getMetaModelContext().getSpecificationLoader()
                    .loadSpecification(facetedMethod.getType()));
    }

    protected OneToOneAssociationDefault(
            final Identifier featureIdentifier,
            final FacetedMethod facetedMethod,
            final ObjectSpecification objectSpec) {

        super(featureIdentifier, facetedMethod, FeatureType.PROPERTY, objectSpec);
    }

    // -- COLUMN NAMED

    @Override
    public String getColumnFriendlyName() {
        return lookupFacet(ColumnNamedFacet.class)
        .map(ColumnNamedFacet::translated)
        //we have a facet-post-processor to ensure following code path is unreachable,
        // however, we keep it in support of JUnit testing
        .orElseGet(()->getFeatureIdentifier().getMemberNaturalName());
    }

    // -- COLUMN DESCRIBED AS

    @Override
    public String getColumnDescription() {
        return lookupFacet(ColumnDescribedFacet.class)
        .map(ColumnDescribedFacet::translated)
        .orElseGet(() -> null);
    }

    // -- VISIBLE, USABLE

    @Override
    public VisibilityContext createVisibleInteractionContext(
            final ManagedObject ownerAdapter,
            final InteractionInitiatedBy interactionInitiatedBy,
            final Where where) {
        return new PropertyVisibilityContext(
                headFor(ownerAdapter), getFeatureIdentifier(), interactionInitiatedBy, where);
    }


    @Override
    public UsabilityContext createUsableInteractionContext(
            final ManagedObject ownerAdapter,
            final InteractionInitiatedBy interactionInitiatedBy,
            final Where where) {
        return new PropertyUsabilityContext(
                headFor(ownerAdapter), getFeatureIdentifier(), interactionInitiatedBy, where);
    }

    // -- VALIDITY

    private ValidityContext createValidateInteractionContext(
            final ManagedObject ownerAdapter,
            final ManagedObject proposedToReferenceAdapter,
            final InteractionInitiatedBy interactionInitiatedBy) {
        return new PropertyModifyContext(
                headFor(ownerAdapter), getFeatureIdentifier(), proposedToReferenceAdapter,
                interactionInitiatedBy);
    }

    @Override
    public Consent isAssociationValid(
            final ManagedObject ownerAdapter,
            final ManagedObject proposedAdapter,
            final InteractionInitiatedBy interactionInitiatedBy) {
        return isAssociationValidResult(ownerAdapter, proposedAdapter, interactionInitiatedBy).createConsent();
    }

    private InteractionResult isAssociationValidResult(
            final ManagedObject ownerAdapter,
            final ManagedObject proposedToReferenceAdapter,
            final InteractionInitiatedBy interactionInitiatedBy) {
        final ValidityContext validityContext =
                createValidateInteractionContext(
                        ownerAdapter, proposedToReferenceAdapter, interactionInitiatedBy);
        return InteractionUtils.isValidResult(this, validityContext);
    }

    // -- INIT

    @Override
    public void initAssociation(
            final ManagedObject ownerAdapter,
            final ManagedObject referencedAdapter) {

        final PropertyInitializationFacet initializerFacet = getFacet(PropertyInitializationFacet.class);
        if (initializerFacet != null) {
            initializerFacet.initProperty(ownerAdapter, referencedAdapter);
        }
    }

    // -- ACCESS (get, isEmpty)

    @Override
    public ManagedObject get(
            final ManagedObject ownerAdapter,
            final InteractionInitiatedBy interactionInitiatedBy) {

        val propertyOrCollectionAccessorFacet = getFacet(PropertyOrCollectionAccessorFacet.class);
        val referencedPojo =
                propertyOrCollectionAccessorFacet.getProperty(ownerAdapter, interactionInitiatedBy);

        if (referencedPojo == null) {
            return null;
        }

        return getObjectManager().adapt(referencedPojo);
    }

    @Override
    public boolean isEmpty(final ManagedObject ownerAdapter, final InteractionInitiatedBy interactionInitiatedBy) {
        return get(ownerAdapter, interactionInitiatedBy) == null;
    }

    // -- ACCESS (set)

    /**
     * Sets up the {@link Command}, then delegates to the appropriate facet
     * ({@link PropertySetterFacet} or {@link PropertyClearFacet}).
     */
    @Override
    public ManagedObject set(
            final ManagedObject ownerAdapter,
            final ManagedObject newReferencedAdapter,
            final InteractionInitiatedBy interactionInitiatedBy) {

        setupCommand(InteractionHead.regular(ownerAdapter), newReferencedAdapter);

        if (newReferencedAdapter != null) {
            return setValue(ownerAdapter, newReferencedAdapter, interactionInitiatedBy);
        } else {
            return clearValue(ownerAdapter, interactionInitiatedBy);
        }
    }

    private ManagedObject setValue(
            final ManagedObject ownerAdapter,
            final ManagedObject newReferencedAdapter,
            final InteractionInitiatedBy interactionInitiatedBy) {

        val propertySetterFacet = getFacet(PropertySetterFacet.class);
        if (propertySetterFacet == null) {
            throw _Exceptions.unexpectedCodeReach();
        }

        EntityUtil.requiresWhenFirstIsBookmarkableSecondIsAttached(ownerAdapter, newReferencedAdapter);

        return propertySetterFacet.setProperty(this, ownerAdapter, newReferencedAdapter, interactionInitiatedBy);
    }

    private ManagedObject clearValue(
            final ManagedObject ownerAdapter,
            final InteractionInitiatedBy interactionInitiatedBy) {

        val propertyClearFacet = getFacet(PropertyClearFacet.class);

        if (propertyClearFacet == null) {
            throw _Exceptions.unexpectedCodeReach();
        }

        return propertyClearFacet.clearProperty(this, ownerAdapter, interactionInitiatedBy);
    }

    // -- DEFAULTS

    @Override
    public ManagedObject getDefault(final ManagedObject ownerAdapter) {
        PropertyDefaultFacet propertyDefaultFacet = lookupNonFallbackFacet(PropertyDefaultFacet.class)
                .orElse(null);
        // if no default on the association, attempt to find a default on the
        // specification (eg an int should
        // default to 0).
        if (propertyDefaultFacet == null) {
            propertyDefaultFacet = this.getSpecification().getFacet(PropertyDefaultFacet.class);
        }
        if (propertyDefaultFacet == null) {
            return null;
        }
        return propertyDefaultFacet.getDefault(ownerAdapter);
    }

    @Override
    public void toDefault(final ManagedObject ownerAdapter) {
        // default only mandatory fields
        if (!MandatoryFacet.isMandatory(this)) {
            return;
        }

        final ManagedObject defaultValue = getDefault(ownerAdapter);
        if (defaultValue != null) {
            initAssociation(ownerAdapter, defaultValue);
        }
    }

    // -- CHOICES AND AUTO-COMPLETE

    @Override
    public boolean hasChoices() {
        return getFacet(PropertyChoicesFacet.class) != null;
    }

    @Override
    public Can<ManagedObject> getChoices(
            final ManagedObject ownerAdapter,
            final InteractionInitiatedBy interactionInitiatedBy) {

        val propertyChoicesFacet = getFacet(PropertyChoicesFacet.class);
        if (propertyChoicesFacet == null) {
            return Can.empty();
        }

        return propertyChoicesFacet.getChoices(
                ownerAdapter,
                interactionInitiatedBy);
    }


    @Override
    public boolean hasAutoComplete() {
        final PropertyAutoCompleteFacet propertyAutoCompleteFacet = getFacet(PropertyAutoCompleteFacet.class);
        return propertyAutoCompleteFacet != null;
    }

    @Override
    public Can<ManagedObject> getAutoComplete(
            final ManagedObject ownerAdapter,
            final String searchArg,
            final InteractionInitiatedBy interactionInitiatedBy) {

        final PropertyAutoCompleteFacet propertyAutoCompleteFacet = getFacet(PropertyAutoCompleteFacet.class);
        final Object[] pojoOptions = propertyAutoCompleteFacet
                .autoComplete(ownerAdapter, searchArg, interactionInitiatedBy);

        val adapters = _NullSafe.stream(pojoOptions)
                .map(getObjectManager()::adapt)
                .collect(Can.toCan());
        return adapters;
    }

    @Override
    public int getAutoCompleteMinLength() {
        final PropertyAutoCompleteFacet propertyAutoCompleteFacet = getFacet(PropertyAutoCompleteFacet.class);
        return propertyAutoCompleteFacet != null? propertyAutoCompleteFacet.getMinLength(): MinLengthUtil.MIN_LENGTH_DEFAULT;
    }

    /**
     * Internal API
     */
    public void setupCommand(
            final InteractionHead head,
            final ManagedObject valueAdapterOrNull) {

        setupCommand(head, interactionId ->
            getCommandDtoFactory()
                .asCommandDto(interactionId, Can.ofSingleton(head), this, valueAdapterOrNull));
    }

    // -- OBJECT CONTRACT

    @Override
    public String toString() {
        final ToString str = new ToString(this);
        str.append(super.toString());
        str.setAddComma();
        str.append("persisted", !isNotPersisted());
        str.append("type", getSpecification().getShortIdentifier());
        return str.toString();
    }


}
