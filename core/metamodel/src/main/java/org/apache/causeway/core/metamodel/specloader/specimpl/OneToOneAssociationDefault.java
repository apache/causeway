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
package org.apache.causeway.core.metamodel.specloader.specimpl;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.applib.annotation.PropertyLayout;
import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.applib.services.command.Command;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.commons.internal.reflection._Annotations;
import org.apache.causeway.core.metamodel.commons.ToString;
import org.apache.causeway.core.metamodel.consent.Consent;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.facetapi.FeatureType;
import org.apache.causeway.core.metamodel.facets.FacetedMethod;
import org.apache.causeway.core.metamodel.facets.objectvalue.mandatory.MandatoryFacet;
import org.apache.causeway.core.metamodel.facets.param.autocomplete.MinLengthUtil;
import org.apache.causeway.core.metamodel.facets.propcoll.accessor.PropertyOrCollectionAccessorFacet;
import org.apache.causeway.core.metamodel.facets.properties.autocomplete.PropertyAutoCompleteFacet;
import org.apache.causeway.core.metamodel.facets.properties.choices.PropertyChoicesFacet;
import org.apache.causeway.core.metamodel.facets.properties.defaults.PropertyDefaultFacet;
import org.apache.causeway.core.metamodel.facets.properties.update.clear.PropertyClearFacet;
import org.apache.causeway.core.metamodel.facets.properties.update.init.PropertyInitializationFacet;
import org.apache.causeway.core.metamodel.facets.properties.update.modify.PropertySetterFacet;
import org.apache.causeway.core.metamodel.interactions.InteractionHead;
import org.apache.causeway.core.metamodel.interactions.InteractionUtils;
import org.apache.causeway.core.metamodel.interactions.PropertyModifyContext;
import org.apache.causeway.core.metamodel.interactions.PropertyUsabilityContext;
import org.apache.causeway.core.metamodel.interactions.PropertyVisibilityContext;
import org.apache.causeway.core.metamodel.interactions.UsabilityContext;
import org.apache.causeway.core.metamodel.interactions.ValidityContext;
import org.apache.causeway.core.metamodel.interactions.VisibilityContext;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.ManagedObjects;
import org.apache.causeway.core.metamodel.object.MmEntityUtil;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;

import lombok.Getter;
import lombok.val;

public class OneToOneAssociationDefault
extends ObjectAssociationAbstract
implements OneToOneAssociation {

    public static OneToOneAssociationDefault forMethod(final FacetedMethod facetedMethod) {
        return new OneToOneAssociationDefault(
                facetedMethod.getFeatureIdentifier(),
                facetedMethod,
                facetedMethod.getMetaModelContext().getSpecificationLoader()
                    .loadSpecification(facetedMethod.getType().getElementType()));
    }

    protected OneToOneAssociationDefault(
            final Identifier featureIdentifier,
            final FacetedMethod facetedMethod,
            final ObjectSpecification objectSpec) {

        super(featureIdentifier, facetedMethod, FeatureType.PROPERTY, objectSpec);
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
            final ManagedObject proposedValue,
            final InteractionInitiatedBy interactionInitiatedBy) {

        val head = headFor(ownerAdapter);

        return new PropertyModifyContext(
                head,
                getFeatureIdentifier(),
                proposedValue,
                ()->getFriendlyName(head::getTarget),
                interactionInitiatedBy);
    }

    @Override
    public Consent isAssociationValid(
            final ManagedObject ownerAdapter,
            final ManagedObject proposedValue,
            final InteractionInitiatedBy interactionInitiatedBy) {
        return InteractionUtils.isValidResult(
                    this,
                    createValidateInteractionContext(
                            ownerAdapter, proposedValue, interactionInitiatedBy))
                .createConsent();
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
            // TODO: perhaps this should instead return ManagedObject.empty(getSpecification()) ?
            //  however, that's a far-reaching change to make.
            return null;
        }

        return getObjectManager().adapt(referencedPojo);
    }

    @Override
    public boolean isEmpty(final ManagedObject ownerAdapter, final InteractionInitiatedBy interactionInitiatedBy) {
        final ManagedObject referencedObject = get(ownerAdapter, interactionInitiatedBy);
        return ManagedObjects.isNullOrUnspecifiedOrEmpty(referencedObject);
    }

    // -- ACCESS (set)

    /**
     * Sets up the {@link Command}, then delegates to the appropriate facet
     * ({@link PropertySetterFacet} or {@link PropertyClearFacet}).
     */
    @Override
    public final ManagedObject set(
            final ManagedObject ownerAdapter,
            final ManagedObject _newValue,
            final InteractionInitiatedBy interactionInitiatedBy) {

        // null to empty
        val newValue = _newValue==null
                ? ManagedObject.empty(getElementType())
                : _newValue;

        // don't setup a command DTO, eg. when called in the context of serialization
        if(!interactionInitiatedBy.isPassThrough()) {
            setupCommand(InteractionHead.regular(ownerAdapter), newValue);
        }

        if (ManagedObjects.isNullOrUnspecifiedOrEmpty(newValue)) {
            return clearValue(ownerAdapter, interactionInitiatedBy);
        } else {
            return setValue(ownerAdapter, newValue, interactionInitiatedBy);
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

        MmEntityUtil.requiresWhenFirstIsBookmarkableSecondIsAlso(ownerAdapter, newReferencedAdapter);

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
            propertyDefaultFacet = this.getElementType().getFacet(PropertyDefaultFacet.class);
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
            final ManagedObject valueAdapter) {

        setupCommand(head, interactionId ->
            getCommandDtoFactory()
                .asCommandDto(interactionId, head, this, valueAdapter));
    }

    @Getter(lazy=true, onMethod_ = {@Override})
    private final boolean explicitlyAnnotated = calculateIsExplicitlyAnnotated();

    // -- OBJECT CONTRACT

    @Override
    public String toString() {
        final ToString str = new ToString(this);
        str.append(super.toString());
        str.setAddComma();
        str.append("persisted", isIncludedWithSnapshots());
        str.append("type", getElementType().getShortIdentifier());
        return str.toString();
    }

    // -- HELPER

    private boolean calculateIsExplicitlyAnnotated() {
        val javaMethod = getFacetedMethod().getMethod();
        return _Annotations.synthesize(javaMethod, Property.class).isPresent()
                || _Annotations.synthesize(javaMethod, PropertyLayout.class).isPresent();
    }


}
