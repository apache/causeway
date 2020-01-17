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

import java.util.Collections;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.services.command.Command;
import org.apache.isis.applib.services.command.CommandContext;
import org.apache.isis.core.commons.collections.Can;
import org.apache.isis.core.commons.internal.base._NullSafe;
import org.apache.isis.core.metamodel.commons.ToString;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.consent.InteractionResult;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.FacetedMethod;
import org.apache.isis.core.metamodel.facets.actions.action.invocation.CommandUtil;
import org.apache.isis.core.metamodel.facets.objectvalue.mandatory.MandatoryFacet;
import org.apache.isis.core.metamodel.facets.param.autocomplete.MinLengthUtil;
import org.apache.isis.core.metamodel.facets.propcoll.accessor.PropertyOrCollectionAccessorFacet;
import org.apache.isis.core.metamodel.facets.properties.autocomplete.PropertyAutoCompleteFacet;
import org.apache.isis.core.metamodel.facets.properties.choices.PropertyChoicesFacet;
import org.apache.isis.core.metamodel.facets.properties.defaults.PropertyDefaultFacet;
import org.apache.isis.core.metamodel.facets.properties.update.clear.PropertyClearFacet;
import org.apache.isis.core.metamodel.facets.properties.update.init.PropertyInitializationFacet;
import org.apache.isis.core.metamodel.facets.properties.update.modify.PropertySetterFacet;
import org.apache.isis.core.metamodel.interactions.InteractionUtils;
import org.apache.isis.core.metamodel.interactions.PropertyModifyContext;
import org.apache.isis.core.metamodel.interactions.PropertyUsabilityContext;
import org.apache.isis.core.metamodel.interactions.PropertyVisibilityContext;
import org.apache.isis.core.metamodel.interactions.UsabilityContext;
import org.apache.isis.core.metamodel.interactions.ValidityContext;
import org.apache.isis.core.metamodel.interactions.VisibilityContext;
import org.apache.isis.core.metamodel.services.command.CommandDtoServiceInternal;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.schema.cmd.v2.CommandDto;

import lombok.val;

public class OneToOneAssociationDefault extends ObjectAssociationAbstract implements OneToOneAssociation {

    public OneToOneAssociationDefault(final FacetedMethod facetedMethod) {
        this(facetedMethod, facetedMethod.getMetaModelContext()
                .getSpecificationLoader().loadSpecification(facetedMethod.getType()));
    }

    protected OneToOneAssociationDefault(
            final FacetedMethod facetedMethod,
            final ObjectSpecification objectSpec) {

        super(facetedMethod, FeatureType.PROPERTY, objectSpec);
    }

    // -- visible, usable

    @Override
    public VisibilityContext<?> createVisibleInteractionContext(
            final ManagedObject ownerAdapter, final InteractionInitiatedBy interactionInitiatedBy,
            Where where) {
        return new PropertyVisibilityContext(ownerAdapter, getIdentifier(), interactionInitiatedBy, where);
    }


    @Override
    public UsabilityContext<?> createUsableInteractionContext(
            final ManagedObject ownerAdapter, final InteractionInitiatedBy interactionInitiatedBy,
            Where where) {
        return new PropertyUsabilityContext(ownerAdapter, getIdentifier(), interactionInitiatedBy, where);
    }



    // -- Validity
    private ValidityContext<?> createValidateInteractionContext(
            final ManagedObject ownerAdapter,
            final ManagedObject proposedToReferenceAdapter,
            final InteractionInitiatedBy interactionInitiatedBy) {
        return new PropertyModifyContext(ownerAdapter, getIdentifier(), proposedToReferenceAdapter,
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
        final ValidityContext<?> validityContext =
                createValidateInteractionContext(
                        ownerAdapter, proposedToReferenceAdapter, interactionInitiatedBy
                        );
        return InteractionUtils.isValidResult(this, validityContext);
    }



    // -- init
    @Override
    public void initAssociation(
            final ManagedObject ownerAdapter, 
            final ManagedObject referencedAdapter) {
        
        final PropertyInitializationFacet initializerFacet = getFacet(PropertyInitializationFacet.class);
        if (initializerFacet != null) {
            initializerFacet.initProperty(ownerAdapter, referencedAdapter);
        }
    }



    // -- Access (get, isEmpty)

    @Override
    public ManagedObject get(
            final ManagedObject ownerAdapter,
            final InteractionInitiatedBy interactionInitiatedBy) {

        final PropertyOrCollectionAccessorFacet facet = getFacet(PropertyOrCollectionAccessorFacet.class);
        final Object referencedPojo =
                facet.getProperty(ownerAdapter, interactionInitiatedBy);

        if (referencedPojo == null) {
            return null;
        }

        return getObjectManager().adapt(referencedPojo);
    }

    @Override
    public boolean isEmpty(final ManagedObject ownerAdapter, final InteractionInitiatedBy interactionInitiatedBy) {
        return get(ownerAdapter, interactionInitiatedBy) == null;
    }

    // -- Set
    @Override
    public void set(
            final ManagedObject ownerAdapter,
            final ManagedObject newReferencedAdapter) {
        set(ownerAdapter, newReferencedAdapter, InteractionInitiatedBy.USER);
    }

    /**
     * Sets up the {@link Command}, then delegates to the appropriate facet
     * ({@link PropertySetterFacet} or {@link PropertyClearFacet}).
     */
    @Override
    public void set(
            final ManagedObject ownerAdapter,
            final ManagedObject newReferencedAdapter,
            final InteractionInitiatedBy interactionInitiatedBy) {

        setupCommand(ownerAdapter, newReferencedAdapter);

        setInternal(ownerAdapter, newReferencedAdapter, interactionInitiatedBy);
    }

    private void setInternal(
            final ManagedObject ownerAdapter,
            final ManagedObject newReferencedAdapter,
            final InteractionInitiatedBy interactionInitiatedBy) {
        if (newReferencedAdapter != null) {
            setValue(ownerAdapter, newReferencedAdapter, interactionInitiatedBy);
        } else {
            clearValue(ownerAdapter, interactionInitiatedBy);
        }
    }

    private void setValue(
            final ManagedObject ownerAdapter,
            final ManagedObject newReferencedAdapter,
            final InteractionInitiatedBy interactionInitiatedBy) {

        val propertySetterFacet = getFacet(PropertySetterFacet.class);
        if (propertySetterFacet == null) {
            return;
        }
        
        ManagedObject._whenFirstIsBookmarkable_ensureSecondIsAsWell(ownerAdapter, newReferencedAdapter);

        propertySetterFacet.setProperty(this, ownerAdapter, newReferencedAdapter, interactionInitiatedBy);
    }

    private void clearValue(
            final ManagedObject ownerAdapter,
            final InteractionInitiatedBy interactionInitiatedBy) {
        
        final PropertyClearFacet facet = getFacet(PropertyClearFacet.class);
        facet.clearProperty(this, ownerAdapter, interactionInitiatedBy);
    }



    // -- defaults
    @Override
    public ManagedObject getDefault(final ManagedObject ownerAdapter) {
        PropertyDefaultFacet propertyDefaultFacet = getFacet(PropertyDefaultFacet.class);
        // if no default on the association, attempt to find a default on the
        // specification (eg an int should
        // default to 0).
        if (propertyDefaultFacet == null || propertyDefaultFacet.isFallback()) {
            propertyDefaultFacet = this.getSpecification().getFacet(PropertyDefaultFacet.class);
        }
        if (propertyDefaultFacet == null) {
            return null;
        }
        return propertyDefaultFacet.getDefault(ownerAdapter);
    }

    @Override
    public void toDefault(final ManagedObject ownerAdapter) {
        // don't default optional fields
        final MandatoryFacet mandatoryFacet = getFacet(MandatoryFacet.class);
        if (mandatoryFacet != null && mandatoryFacet.isInvertedSemantics()) {
            return;
        }

        final ManagedObject defaultValue = getDefault(ownerAdapter);
        if (defaultValue != null) {
            initAssociation(ownerAdapter, defaultValue);
        }
    }



    // -- choices and autoComplete
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
            return null;
        }

        final Object[] pojoOptions = propertyChoicesFacet.getChoices(
                ownerAdapter,
                interactionInitiatedBy);

        val adapters = _NullSafe.stream(pojoOptions)
                .map(getObjectManager()::adapt)
                .collect(Can.toCan());
        return adapters;
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
            final ManagedObject targetAdapter,
            final ManagedObject valueAdapterOrNull) {

        final CommandContext commandContext = getCommandContext();
        final Command command = commandContext.getCommand();

        if (command.getExecutor() != Command.Executor.USER) {
            return;
        }

        setupCommandTarget(targetAdapter, valueAdapterOrNull);
        setupCommandMemberIdentifier();
        setupCommandMementoAndExecutionContext(targetAdapter, valueAdapterOrNull);
    }

    private void setupCommandTarget(
            final ManagedObject targetAdapter,
            final ManagedObject valueAdapter) {

        final String arguments = CommandUtil.argDescriptionFor(valueAdapter);
        setupCommandTarget(targetAdapter, arguments);
    }

    private void setupCommandMementoAndExecutionContext(
            final ManagedObject targetAdapter,
            final ManagedObject valueAdapterOrNull) {

        final CommandDtoServiceInternal commandDtoServiceInternal = getCommandDtoService();
        final CommandDto dto = commandDtoServiceInternal.asCommandDto(
                Collections.singletonList(targetAdapter), this, valueAdapterOrNull);

        setupCommandDtoAndExecutionContext(dto);

    }


    // -- toString

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
