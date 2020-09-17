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

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.core.metamodel.facets.object.viewmodel.ViewModelFacet;
import org.apache.isis.core.metamodel.interactions.managed.InteractionVeto;
import org.apache.isis.core.metamodel.interactions.managed.ManagedProperty;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ManagedObjects;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
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
            PropertyMemento propertyMemento,
            EntityModel.Mode mode, 
            EntityModel.RenderingHint renderingHint) {
        
        super(parentEntityModel, propertyMemento, mode, renderingHint);
        this.propertyMemento = propertyMemento;
        reset();
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
    
    private transient OneToOneAssociation property;
    
    @Override
    public OneToOneAssociation getMetaModel() {
        if(property==null) {
            property = propertyMemento.getProperty(getSpecificationLoader()); 
        }
        return property;  
    }

    private transient ManagedProperty managedProperty;
    
    public ManagedProperty getManagedProperty() {
        if(managedProperty==null) {
            val owner = getParentUiModel().getObject();
            val where = this.getRenderingHint().asWhere();
            managedProperty = ManagedProperty.of(owner, getMetaModel(), where); 
        }
        return managedProperty;  
    } 
    
    @Override
    public ObjectSpecification getScalarTypeSpec() {
        return getMetaModel().getSpecification();
    }

    @Override
    public String getIdentifier() {
        return getMetaModel().getIdentifier().toNameIdentityString();
    }
    
    @Override
    public String getCssClass() {
        return getMetaModel().getCssClass("isis-");
    }

    @Override
    public boolean whetherHidden() {
        return getManagedProperty()
                .checkVisibility()
                .isPresent();
    }

    @Override
    public String whetherDisabled() {
        return getManagedProperty()
                .checkUsability()
                .map(InteractionVeto::getReason)
                .orElse(null);
    }

    @Override
    public String validate(final ManagedObject proposedNewValue) {
        return getManagedProperty()
                .checkValidity(proposedNewValue)
                .map(InteractionVeto::getReason)
                .orElse(null);
    }

    public void reset() {
        val propertyValue = getManagedProperty().getPropertyValue();
        val presentationValue = ManagedObjects.isNullOrUnspecifiedOrEmpty(propertyValue)
                ? null
                : propertyValue;
        
        this.setObject(presentationValue);
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
        val associate = getObject();
        return validate(associate);
    }
    
    /**
     * Apply changes to the underlying adapter (possibly returning a new adapter).
     *
     * @return adapter, which may be different from the original
     *  (specifically, if operating on a {@link ViewModelFacet#isCloneable(Object) cloneable} view model, for example.
     */
    public ManagedObject applyValue() {
        
        val proposedNewValue = getObject();
        getManagedProperty().modifyProperty(proposedNewValue);
        return getManagedProperty().getOwner();

    }
    
    @Override
    protected Can<ObjectAction> calcAssociatedActions() {
        return getManagedProperty().getAssociatedActions();
    }
    
}
