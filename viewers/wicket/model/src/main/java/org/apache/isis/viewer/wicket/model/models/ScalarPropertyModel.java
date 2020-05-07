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
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facets.object.parseable.ParseableFacet;
import org.apache.isis.core.metamodel.facets.object.viewmodel.ViewModelFacet;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ManagedObjects;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.core.webapp.context.memento.ObjectMemento;
import org.apache.isis.viewer.common.model.feature.PropertyUiModel;
import org.apache.isis.viewer.wicket.model.mementos.PropertyMemento;

import lombok.val;

public class ScalarPropertyModel 
extends ScalarModel 
implements PropertyUiModel {
    
    private static final long serialVersionUID = 1L;
    
    private final PropertyMemento propertyMemento;

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
    
    public ScalarPropertyModel copyHaving(
            EntityModel.Mode mode, 
            EntityModel.RenderingHint renderingHint) {
        return new ScalarPropertyModel(
                getParentUiModel(), 
                propertyMemento,
                mode,
                renderingHint);
    }
    
    private void getAndStore(final EntityModel parentEntityModel) {
        final ObjectMemento parentAdapterMemento = parentEntityModel.getObjectAdapterMemento();
        final OneToOneAssociation property = propertyMemento.getProperty(getSpecificationLoader());
        final ManagedObject parentAdapter = super.getCommonContext().reconstructObject(parentAdapterMemento); 
        setObjectFromPropertyIfVisible(ScalarPropertyModel.this, property, parentAdapter);
    }
    
    private transient OneToOneAssociation property;
    
    @Override
    public OneToOneAssociation getMetaModel() {
        if(property==null) {
            property = propertyMemento.getProperty(getSpecificationLoader()); 
        }
        return property;  
    }

        

    @Override
    public ObjectSpecification getScalarTypeSpec() {
        ObjectSpecId type = propertyMemento.getType();
        return getSpecificationLoader().lookupBySpecIdElseLoad(type);
    }

    @Override
    public String getIdentifier() {
        return propertyMemento.getIdentifier();
    }

    @Override
    public String getCssClass() {
        final String objectSpecId =
                getParentUiModel().getTypeOfSpecification().getSpecId().asString().replace(".", "-");
        final String propertyId = getIdentifier();
        return "isis-" + objectSpecId + "-" + propertyId;
    }

    @Override
    public boolean whetherHidden(final Where where) {
        final ManagedObject parentAdapter = getParentUiModel().load();
        try {
            final Consent visibility = getMetaModel().isVisible(parentAdapter, InteractionInitiatedBy.USER, where);
            return visibility.isVetoed();
        } catch (final Exception ex) {
            return true; // will be hidden
        }
    }

    @Override
    public String whetherDisabled(final Where where) {
        final ManagedObject parentAdapter = getParentUiModel().load();
        try {
            final Consent usable = getMetaModel().isUsable(parentAdapter, InteractionInitiatedBy.USER, where);
            return usable.isAllowed() ? null : usable.getReason();
        } catch (final Exception ex) {
            return ex.getLocalizedMessage();
        }
    }

    @Override
    public String parseAndValidate(final String proposedPojoAsStr) {
        final OneToOneAssociation property = getMetaModel();
        ParseableFacet parseableFacet = property.getFacet(ParseableFacet.class);
        if (parseableFacet == null) {
            parseableFacet = property.getSpecification().getFacet(ParseableFacet.class);
        }
        try {
            final ManagedObject parentAdapter = getParentUiModel().load();
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
        final ManagedObject parentAdapter = getParentUiModel().load();
        try {
            final Consent valid = getMetaModel().isAssociationValid(parentAdapter, proposedAdapter,
                    InteractionInitiatedBy.USER);
            return valid.isAllowed() ? null : valid.getReason();
        } catch (final Exception ex) {
            return ex.getLocalizedMessage();
        }
    }

    @Override
    public boolean isRequired() {
        return isRequired(getMetaModel());
    }

    @Override
    public <T extends Facet> T getFacet(final Class<T> facetType) {
        return getMetaModel().getFacet(facetType);
    }

    public void reset() {
        val parentAdapter = getParentUiModel().load();
        setObjectFromPropertyIfVisible(this, getMetaModel(), parentAdapter);
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
        return getName() + ": " + propertyMemento.toString();
    }
    
    public String getReasonInvalidIfAny() {
        val adapter = getParentUiModel().load();
        val associate = getObject();
        Consent validity = getMetaModel().isAssociationValid(adapter, associate, InteractionInitiatedBy.USER);
        return validity.isAllowed() ? null : validity.getReason();
    }
    
    /**
     * Apply changes to the underlying adapter (possibly returning a new adapter).
     *
     * @return adapter, which may be different from the original (if a {@link ViewModelFacet#isCloneable(Object) cloneable} view model, for example.
     */
    public ManagedObject applyValue(ManagedObject adapter) {
        val property = getMetaModel();

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

        return ManagedObjects.copyIfClonable(adapter);

    }
    
    @Override
    protected List<ObjectAction> calcAssociatedActions() {
        val parentAdapter = getParentUiModel().load();
        return ObjectAction.Util.findForAssociation(parentAdapter, getMetaModel());
    }


    
}
