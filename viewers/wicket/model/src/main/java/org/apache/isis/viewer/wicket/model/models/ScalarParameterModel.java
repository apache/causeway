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

import javax.annotation.Nullable;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.core.commons.collections.Can;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.interactions.managed.ManagedAction;
import org.apache.isis.core.metamodel.interactions.managed.ParameterNegotiationModel;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ManagedObjects;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.viewer.common.model.feature.ParameterUiModel;
import org.apache.isis.viewer.wicket.model.mementos.ActionParameterMemento;

import lombok.Getter;
import lombok.Setter;
import lombok.val;

public class ScalarParameterModel
extends ScalarModel
implements ParameterUiModel {

    private static final long serialVersionUID = 1L;
    
    private final ActionParameterMemento paramMemento;
    
    @Getter(onMethod = @__(@Override)) 
    @Setter(onMethod = @__(@Override))
    private transient ParameterNegotiationModel pendingParameterModel;

    /**
     * Creates a model representing an action parameter of an action of a parent
     * object, with the {@link #getObject() value of this model} to be default
     * value (if any) of that action parameter.
     */
    public ScalarParameterModel(EntityModel parentEntityModel, ActionParameterMemento paramMemento) {
        super(parentEntityModel, paramMemento);
        this.paramMemento = paramMemento;
    }
    
    private transient ObjectActionParameter actionParameter;
    
    @Override
    public ObjectActionParameter getMetaModel() {
        if(actionParameter==null) {
            actionParameter = paramMemento.getActionParameter(this::getSpecificationLoader); 
        }
        return actionParameter;  
    }
    
    private transient ManagedAction managedAction;
    
    public ManagedAction getManagedAction() {
        if(managedAction==null) {
            val actionOwner = getParentUiModel().load();
            managedAction = ManagedAction.of(actionOwner, getMetaModel().getAction()); 
        }
        return managedAction;  
    }
    
//    private transient ManagedParameter managedParameter;
//    
//    public ManagedParameter getManagedParameter() {
//        if(managedParameter==null) {
//            val parameter = getMetaModel();
//            managedParameter = getManagedAction().managedParameter(parameter.getNumber()); 
//        }
//        return managedParameter;  
//    } 
    

    @Override
    public ObjectSpecification getScalarTypeSpec() {
        return getMetaModel().getSpecification();
    }

    @Override
    public String getIdentifier() {
        return "" + getNumber();
    }

    @Override
    public String getCssClass() {
        return getMetaModel().getCssClass("isis-");
    }

    @Override
    public String whetherDisabled(Where where) {
        // always enabled TODO this is not true
        return null;
    }

    @Override
    public boolean whetherHidden(Where where) {
        // always enabled TODO this is not true
        return false;
    }

    @Override
    public String validate(final ManagedObject proposedValue) {
        final ObjectActionParameter parameter = getMetaModel();
        
        val action = parameter.getAction();
        try {
            ManagedObject parentAdapter = getParentUiModel().load();
            
            val head = action.interactionHead(parentAdapter);    
            
            final String invalidReasonIfAny = parameter
                    .isValid(head, proposedValue, InteractionInitiatedBy.USER);
            return invalidReasonIfAny;
        } catch (final Exception ex) {
            return ex.getLocalizedMessage();
        }
    }

    @Override
    public ManagedObject load() {
        return toNonNull(loadFromSuper());
    }

    @Override
    public String toStringOf() {
        return getName() + ": " + paramMemento.toString();
    }
    
    @Override
    protected Can<ObjectAction> calcAssociatedActions() {
        return Can.empty();
    }

    @Override
    public ManagedObject getValue() {
        return toNonNull(getObject());
    }

    @Override
    public void setValue(ManagedObject paramValue) {
        super.setObject(paramValue);
    }
    
    // -- HELPER
    
    private ManagedObject toNonNull(@Nullable ManagedObject adapter) {
        if(adapter == null) {
            adapter = ManagedObject.empty(getMetaModel().getSpecification());
        }
        return ManagedObjects.emptyToDefault(!getMetaModel().isOptional(), adapter);
    }
    

    
}
