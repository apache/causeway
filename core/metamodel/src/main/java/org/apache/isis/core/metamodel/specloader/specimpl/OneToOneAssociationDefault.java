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
import java.util.List;
import java.util.stream.Collectors;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.services.command.Command;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.commons.util.ToString;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
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
import org.apache.isis.core.metamodel.services.ServicesInjector;
import org.apache.isis.core.metamodel.services.command.CommandDtoServiceInternal;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.schema.cmd.v1.CommandDto;

public class OneToOneAssociationDefault extends ObjectAssociationAbstract implements OneToOneAssociation {

    public OneToOneAssociationDefault(
            final FacetedMethod facetedMethod,
            final ServicesInjector servicesInjector) {
        this(facetedMethod,
                getSpecification(servicesInjector.getSpecificationLoader(), facetedMethod.getType()),
                servicesInjector);
    }

    protected OneToOneAssociationDefault(
            final FacetedMethod facetedMethod,
            final ObjectSpecification objectSpec,
            final ServicesInjector servicesInjector) {
        super(facetedMethod, FeatureType.PROPERTY, objectSpec, servicesInjector);
    }

    // -- visible, usable

    @Override
    public VisibilityContext<?> createVisibleInteractionContext(
            final ObjectAdapter ownerAdapter, final InteractionInitiatedBy interactionInitiatedBy,
            Where where) {
        return new PropertyVisibilityContext(ownerAdapter, getIdentifier(), interactionInitiatedBy, where);
    }


    @Override
    public UsabilityContext<?> createUsableInteractionContext(
            final ObjectAdapter ownerAdapter, final InteractionInitiatedBy interactionInitiatedBy,
            Where where) {
        return new PropertyUsabilityContext(ownerAdapter, getIdentifier(), interactionInitiatedBy, where);
    }



    // -- Validity
    private ValidityContext<?> createValidateInteractionContext(
            final ObjectAdapter ownerAdapter,
            final ObjectAdapter proposedToReferenceAdapter,
            final InteractionInitiatedBy interactionInitiatedBy) {
        return new PropertyModifyContext(ownerAdapter, getIdentifier(), proposedToReferenceAdapter,
                interactionInitiatedBy);
    }

    @Override
    public Consent isAssociationValid(
            final ObjectAdapter ownerAdapter,
            final ObjectAdapter proposedAdapter,
            final InteractionInitiatedBy interactionInitiatedBy) {
        return isAssociationValidResult(ownerAdapter, proposedAdapter, interactionInitiatedBy).createConsent();
    }

    private InteractionResult isAssociationValidResult(
            final ObjectAdapter ownerAdapter,
            final ObjectAdapter proposedToReferenceAdapter,
            final InteractionInitiatedBy interactionInitiatedBy) {
        final ValidityContext<?> validityContext =
                createValidateInteractionContext(
                        ownerAdapter, proposedToReferenceAdapter, interactionInitiatedBy
                        );
        return InteractionUtils.isValidResult(this, validityContext);
    }



    // -- init
    @Override
    public void initAssociation(final ObjectAdapter ownerAdapter, final ObjectAdapter referencedAdapter) {
        final PropertyInitializationFacet initializerFacet = getFacet(PropertyInitializationFacet.class);
        if (initializerFacet != null) {
            initializerFacet.initProperty(ownerAdapter, referencedAdapter);
        }
    }



    // -- Access (get, isEmpty)

    @Override
    public ObjectAdapter get(
            final ObjectAdapter ownerAdapter,
            final InteractionInitiatedBy interactionInitiatedBy) {
        final PropertyOrCollectionAccessorFacet facet = getFacet(PropertyOrCollectionAccessorFacet.class);
        final Object referencedPojo =
                facet.getProperty(ownerAdapter, interactionInitiatedBy);

        if (referencedPojo == null) {
            return null;
        }

        return getObjectAdapterProvider().adapterFor(referencedPojo);
    }

    @Override
    public boolean isEmpty(final ObjectAdapter ownerAdapter, final InteractionInitiatedBy interactionInitiatedBy) {
        return get(ownerAdapter, interactionInitiatedBy) == null;
    }

    // -- Set
    @Override
    public void set(
            final ObjectAdapter ownerAdapter,
            final ObjectAdapter newReferencedAdapter) {
        set(ownerAdapter, newReferencedAdapter, InteractionInitiatedBy.USER);
    }

    /**
     * Sets up the {@link Command}, then delegates to the appropriate facet
     * ({@link PropertySetterFacet} or {@link PropertyClearFacet}).
     */
    @Override
    public void set(
            final ObjectAdapter ownerAdapter,
            final ObjectAdapter newReferencedAdapter,
            final InteractionInitiatedBy interactionInitiatedBy) {

        setupCommand(ownerAdapter, newReferencedAdapter);

        setInternal(ownerAdapter, newReferencedAdapter, interactionInitiatedBy);
    }

    private void setInternal(
            final ObjectAdapter ownerAdapter,
            final ObjectAdapter newReferencedAdapter,
            final InteractionInitiatedBy interactionInitiatedBy) {
        if (newReferencedAdapter != null) {
            setValue(ownerAdapter, newReferencedAdapter, interactionInitiatedBy);
        } else {
            clearValue(ownerAdapter, interactionInitiatedBy);
        }
    }

    private void setValue(
            final ObjectAdapter ownerAdapter,
            final ObjectAdapter newReferencedAdapter,
            final InteractionInitiatedBy interactionInitiatedBy) {

        final PropertySetterFacet setterFacet = getFacet(PropertySetterFacet.class);
        if (setterFacet == null) {
            return;
        }

        if (    ownerAdapter.representsPersistent() &&
                newReferencedAdapter != null &&
                newReferencedAdapter.isTransient() &&
                !newReferencedAdapter.getSpecification().isParented()) {

            // TODO: I've never seen this exception, and in any case DataNucleus supports persistence-by-reachability; so probably not required
            throw new IsisException("can't set a reference to a transient object from a persistent one: " + newReferencedAdapter.titleString(null) + " (transient)");
        }

        setterFacet.setProperty(this, ownerAdapter, newReferencedAdapter, interactionInitiatedBy);
    }

    private void clearValue(
            final ObjectAdapter ownerAdapter,
            final InteractionInitiatedBy interactionInitiatedBy) {
        final PropertyClearFacet facet = getFacet(PropertyClearFacet.class);
        facet.clearProperty(this, ownerAdapter, interactionInitiatedBy);
    }



    // -- defaults
    @Override
    public ObjectAdapter getDefault(final ObjectAdapter ownerAdapter) {
        PropertyDefaultFacet propertyDefaultFacet = getFacet(PropertyDefaultFacet.class);
        // if no default on the association, attempt to find a default on the
        // specification (eg an int should
        // default to 0).
        if (propertyDefaultFacet == null || propertyDefaultFacet.isNoop()) {
            propertyDefaultFacet = this.getSpecification().getFacet(PropertyDefaultFacet.class);
        }
        if (propertyDefaultFacet == null) {
            return null;
        }
        return propertyDefaultFacet.getDefault(ownerAdapter);
    }

    @Override
    public void toDefault(final ObjectAdapter ownerAdapter) {
        // don't default optional fields
        final MandatoryFacet mandatoryFacet = getFacet(MandatoryFacet.class);
        if (mandatoryFacet != null && mandatoryFacet.isInvertedSemantics()) {
            return;
        }

        final ObjectAdapter defaultValue = getDefault(ownerAdapter);
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
    public ObjectAdapter[] getChoices(
            final ObjectAdapter ownerAdapter,
            final InteractionInitiatedBy interactionInitiatedBy) {
        final PropertyChoicesFacet propertyChoicesFacet = getFacet(PropertyChoicesFacet.class);
        if (propertyChoicesFacet == null) {
            return null;
        }
        final Object[] pojoOptions = propertyChoicesFacet.getChoices(
                ownerAdapter,
                getSpecificationLoader(),
                interactionInitiatedBy);
        List<ObjectAdapter> adapters = _NullSafe.stream(pojoOptions)
                .map( ObjectAdapter.Functions.adapterForUsing( getObjectAdapterProvider() ) )
                .collect(Collectors.toList());
        return adapters.toArray(new ObjectAdapter[]{});
    }


    @Override
    public boolean hasAutoComplete() {
        final PropertyAutoCompleteFacet propertyAutoCompleteFacet = getFacet(PropertyAutoCompleteFacet.class);
        return propertyAutoCompleteFacet != null;
    }

    @Override
    public ObjectAdapter[] getAutoComplete(
            final ObjectAdapter ownerAdapter,
            final String searchArg,
            final InteractionInitiatedBy interactionInitiatedBy) {
        final PropertyAutoCompleteFacet propertyAutoCompleteFacet = getFacet(PropertyAutoCompleteFacet.class);
        final Object[] pojoOptions = propertyAutoCompleteFacet.autoComplete(ownerAdapter, searchArg,
                interactionInitiatedBy);
        if (pojoOptions != null) {
            final ObjectAdapter[] options = new ObjectAdapter[pojoOptions.length];
            for (int i = 0; i < options.length; i++) {
                options[i] = getObjectAdapterProvider().adapterFor(pojoOptions[i]);
            }
            return options;
        }
        return null;
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
            final ObjectAdapter targetAdapter,
            final ObjectAdapter valueAdapterOrNull) {

        setupCommandTarget(targetAdapter, valueAdapterOrNull);
        setupCommandMemberIdentifier();
        setupCommandMementoAndExecutionContext(targetAdapter, valueAdapterOrNull);
    }

    private void setupCommandTarget(
            final ObjectAdapter targetAdapter,
            final ObjectAdapter valueAdapter) {

        final String arguments = CommandUtil.argDescriptionFor(valueAdapter);
        setupCommandTarget(targetAdapter, arguments);
    }

    private void setupCommandMementoAndExecutionContext(
            final ObjectAdapter targetAdapter,
            final ObjectAdapter valueAdapterOrNull) {

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
