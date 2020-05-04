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
package org.apache.isis.viewer.wicket.model.models;

import java.util.List;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.core.commons.collections.Can;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.object.parseable.ParseableFacet;
import org.apache.isis.core.metamodel.facets.object.viewmodel.ViewModelFacet;
import org.apache.isis.core.metamodel.facets.objectvalue.fileaccept.FileAcceptFacet;
import org.apache.isis.core.metamodel.facets.objectvalue.typicallen.TypicalLengthFacet;
import org.apache.isis.core.metamodel.facets.value.bigdecimal.BigDecimalValueFacet;
import org.apache.isis.core.metamodel.facets.value.string.StringValueSemanticsProvider;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.core.metamodel.specloader.specimpl.PendingParameterModel;
import org.apache.isis.core.webapp.context.memento.ObjectMemento;
import org.apache.isis.viewer.wicket.model.mementos.PropertyMemento;

import lombok.Getter;
import lombok.val;

public class ScalarPropertyModel extends ScalarModel {
    
    private static final long serialVersionUID = 1L;
    
    @Getter private final PropertyMemento propertyMemento;

    /**
     * Creates a model representing a property of a parent object, with the
     * {@link #getObject() value of this model} to be current value of the
     * property.
     */
    public ScalarPropertyModel(
            EntityModel parentEntityModel, 
            PropertyMemento pm,
            EntityModel.Mode mode, 
            EntityModel.RenderingHint renderingHint) {
        
        super(parentEntityModel, pm, mode, renderingHint);
        this.propertyMemento = pm;
        reset();
        getAndStore(parentEntityModel);
    }
    
    private void getAndStore(final EntityModel parentEntityModel) {
        final ObjectMemento parentAdapterMemento = parentEntityModel.getObjectAdapterMemento();
        final OneToOneAssociation property = propertyMemento.getProperty(getSpecificationLoader());
        final ManagedObject parentAdapter = super.getCommonContext().reconstructObject(parentAdapterMemento); 
        setObjectFromPropertyIfVisible(ScalarPropertyModel.this, property, parentAdapter);
    }
    

    @Override
    public String getName() {
        return getPropertyMemento().getProperty(getSpecificationLoader()).getName();
    }

    @Override
    public ObjectSpecification getScalarTypeSpec() {
        ObjectSpecId type = getPropertyMemento().getType();
        return getSpecificationLoader().lookupBySpecIdElseLoad(type);
    }

    @Override
    public String getIdentifier() {
        return getPropertyMemento().getIdentifier();
    }

    @Override
    public String getCssClass() {
        final String objectSpecId =
                getParentEntityModel().getTypeOfSpecification().getSpecId().asString().replace(".", "-");
        final String propertyId = getIdentifier();
        return "isis-" + objectSpecId + "-" + propertyId;
    }

    @Override
    public boolean whetherHidden(final Where where) {
        final ManagedObject parentAdapter = getParentEntityModel().load();
        final OneToOneAssociation property = getPropertyMemento().getProperty(getSpecificationLoader());
        try {
            final Consent visibility = property.isVisible(parentAdapter, InteractionInitiatedBy.USER, where);
            return visibility.isVetoed();
        } catch (final Exception ex) {
            return true; // will be hidden
        }
    }

    @Override
    public String whetherDisabled(final Where where) {
        final ManagedObject parentAdapter = getParentEntityModel().load();
        final OneToOneAssociation property = getPropertyMemento().getProperty(getSpecificationLoader());
        try {
            final Consent usable = property.isUsable(parentAdapter, InteractionInitiatedBy.USER, where);
            return usable.isAllowed() ? null : usable.getReason();
        } catch (final Exception ex) {
            return ex.getLocalizedMessage();
        }
    }

    @Override
    public String parseAndValidate(final String proposedPojoAsStr) {
        final OneToOneAssociation property = getPropertyMemento().getProperty(getSpecificationLoader());
        ParseableFacet parseableFacet = property.getFacet(ParseableFacet.class);
        if (parseableFacet == null) {
            parseableFacet = property.getSpecification().getFacet(ParseableFacet.class);
        }
        try {
            final ManagedObject parentAdapter = getParentEntityModel().load();
            final ManagedObject currentValue = property.get(parentAdapter, InteractionInitiatedBy.USER);
            final ManagedObject proposedAdapter =
                    parseableFacet.parseTextEntry(currentValue, proposedPojoAsStr, InteractionInitiatedBy.USER);
            final Consent valid = property.isAssociationValid(parentAdapter, proposedAdapter,
                    InteractionInitiatedBy.USER);
            return valid.isAllowed() ? null : valid.getReason();
//        } catch (final ConcurrencyException ex) {
//            // disregard concurrency exceptions because will pick up at the IFormValidator level rather
//            // than each individual property.
//            return null;
        } catch (final Exception ex) {
            return ex.getLocalizedMessage();
        }
    }

    @Override
    public String validate(final ManagedObject proposedAdapter) {
        final ManagedObject parentAdapter = getParentEntityModel().load();
        final OneToOneAssociation property = getPropertyMemento().getProperty(getSpecificationLoader());
        try {
            final Consent valid = property.isAssociationValid(parentAdapter, proposedAdapter,
                    InteractionInitiatedBy.USER);
            return valid.isAllowed() ? null : valid.getReason();
        } catch (final Exception ex) {
            return ex.getLocalizedMessage();
        }
    }

    @Override
    public boolean isRequired() {
        final FacetHolder facetHolder = getPropertyMemento().getProperty(getSpecificationLoader());
        return isRequired(facetHolder);
    }

    @Override
    public <T extends Facet> T getFacet(final Class<T> facetType) {
        final FacetHolder facetHolder = getPropertyMemento().getProperty(getSpecificationLoader());
        return facetHolder.getFacet(facetType);
    }

    @Override
    public ManagedObject getDefault(
            final PendingParameterModel pendingArgs /*not used*/) {

        final PropertyMemento propertyMemento = getPropertyMemento();
        final OneToOneAssociation property = propertyMemento
                .getProperty(getSpecificationLoader());
        ManagedObject parentAdapter = getParentEntityModel().load();
        return property.getDefault(parentAdapter);
    }

    @Override
    public boolean hasChoices() {
        final PropertyMemento propertyMemento = getPropertyMemento();
        final OneToOneAssociation property = propertyMemento.getProperty(getSpecificationLoader());
        return property.hasChoices();
    }

    @Override
    public Can<ManagedObject> getChoices(
            final PendingParameterModel pendingArgs /*not used on properties*/) { 

        final PropertyMemento propertyMemento = getPropertyMemento();
        final OneToOneAssociation property = propertyMemento
                .getProperty(getSpecificationLoader());
        ManagedObject parentAdapter = getParentEntityModel().load();
        final Can<ManagedObject> choices = property.getChoices(
                parentAdapter,
                InteractionInitiatedBy.USER);

        return choices;
    }

    @Override
    public boolean hasAutoComplete() {
        final PropertyMemento propertyMemento = getPropertyMemento();
        final OneToOneAssociation property = propertyMemento.getProperty(getSpecificationLoader());
        return property.hasAutoComplete();
    }

    @Override
    public Can<ManagedObject> getAutoComplete(
            final PendingParameterModel pendingArgs, /*not used on properties*/
            final String searchArg) {

        final PropertyMemento propertyMemento = getPropertyMemento();
        final OneToOneAssociation property = propertyMemento.getProperty(getSpecificationLoader());
        final ManagedObject parentAdapter =
                getParentEntityModel().load();
        final Can<ManagedObject> choices =
                property.getAutoComplete(
                        parentAdapter, 
                        searchArg,
                        InteractionInitiatedBy.USER);
        return choices;
    }

    @Override
    public int getAutoCompleteOrChoicesMinLength() {

        if (hasAutoComplete()) {
            final PropertyMemento propertyMemento = getPropertyMemento();
            final OneToOneAssociation property = propertyMemento.getProperty(getSpecificationLoader());
            return property.getAutoCompleteMinLength();
        } else {
            return 0;
        }
    }

    @Override
    public String getDescribedAs() {
        final PropertyMemento propertyMemento = getPropertyMemento();
        final OneToOneAssociation property = propertyMemento.getProperty(getSpecificationLoader());
        return property.getDescription();
    }

    @Override
    public Integer getLength() {
        final PropertyMemento propertyMemento = getPropertyMemento();
        final OneToOneAssociation property = propertyMemento.getProperty(getSpecificationLoader());
        final BigDecimalValueFacet facet = property.getFacet(BigDecimalValueFacet.class);
        return facet != null? facet.getPrecision(): null;
    }

    @Override
    public Integer getScale() {
        final PropertyMemento propertyMemento = getPropertyMemento();
        final OneToOneAssociation property = propertyMemento.getProperty(getSpecificationLoader());
        final BigDecimalValueFacet facet = property.getFacet(BigDecimalValueFacet.class);
        return facet != null? facet.getScale(): null;
    }

    @Override
    public int getTypicalLength() {
        final PropertyMemento propertyMemento = getPropertyMemento();
        final OneToOneAssociation property = propertyMemento.getProperty(getSpecificationLoader());
        final TypicalLengthFacet facet = property.getFacet(TypicalLengthFacet.class);
        return facet != null? facet.value() : StringValueSemanticsProvider.TYPICAL_LENGTH;
    }

    @Override
    public String getFileAccept() {
        final PropertyMemento propertyMemento = getPropertyMemento();
        final OneToOneAssociation property = propertyMemento.getProperty(getSpecificationLoader());
        final FileAcceptFacet facet = property.getFacet(FileAcceptFacet.class);
        return facet != null? facet.value(): null;
    }

    public void reset() {
        final OneToOneAssociation property = getPropertyMemento().getProperty(getSpecificationLoader());

        val parentAdapter = getParentEntityModel().load();

        setObjectFromPropertyIfVisible(this, property, parentAdapter);
    }

    @Override
    public ManagedObject load() {
        return loadFromSuper();
    }

    @Override
    public boolean isCollection() {
        return false;
    }

    @Override
    public String toStringOf() {
        return getName() + ": " + getPropertyMemento().toString();
    }
    
    public String getReasonInvalidIfAny() {
        val property = getPropertyMemento().getProperty(getSpecificationLoader());
        val adapter = getParentEntityModel().load();
        val associate = getObject();
        Consent validity = property.isAssociationValid(adapter, associate, InteractionInitiatedBy.USER);
        return validity.isAllowed() ? null : validity.getReason();
    }
    
    /**
     * Apply changes to the underlying adapter (possibly returning a new adapter).
     *
     * @return adapter, which may be different from the original (if a {@link ViewModelFacet#isCloneable(Object) cloneable} view model, for example.
     */
    public ManagedObject applyValue(ManagedObject adapter) {
        val property = getPropertyMemento().getProperty(getSpecificationLoader());

        //
        // previously there was a guard here to only apply changes provided:
        //
        // property.containsDoOpFacet(NotPersistedFacet.class) == null
        //
        // however, that logic is wrong; although a property may not be directly
        // persisted so far as JDO is concerned, it may be indirectly persisted
        // as the result of business logic in the setter.
        //
        // for example, see ExampleTaggableEntity (in isisaddons-module-tags).
        //

        //
        // previously (prior to XML layouts and 'single property' edits) we also used to check if
        // the property was disabled, using:
        //
        // if(property.containsDoOpFacet(DisabledFacet.class)) {
        //    // skip, as per comments above
        //    return;
        // }
        //
        // However, this would seem to be wrong, because the presence of a DisabledFacet doesn't necessarily mean
        // that the property is disabled (its disabledReason(...) might return null).
        //
        // In any case, the only code that calls this method already does the check, so think this is safe
        // to just remove.

        val associate = getObject();
        property.set(adapter, associate, InteractionInitiatedBy.USER);

        final ViewModelFacet recreatableObjectFacet = adapter.getSpecification().getFacet(ViewModelFacet.class);
        if(recreatableObjectFacet != null) {
            final Object viewModel = adapter.getPojo();
            final boolean cloneable = recreatableObjectFacet.isCloneable(viewModel);
            if(cloneable) {
                //XXX lombok issue, no val
                Object newViewModelPojo = recreatableObjectFacet.clone(viewModel);
                adapter = super.getPojoToAdapter().apply(newViewModelPojo);
            }
        }

        return adapter;
    }
    
    @Override
    protected List<ObjectAction> calcAssociatedActions() {

        final EntityModel parentEntityModel1 = this.getParentEntityModel();
        final ManagedObject parentAdapter = parentEntityModel1.load();

        final OneToOneAssociation oneToOneAssociation =
                this.getPropertyMemento().getProperty(this.getSpecificationLoader());

        return ObjectAction.Util.findForAssociation(parentAdapter, oneToOneAssociation);
    }
    
}
