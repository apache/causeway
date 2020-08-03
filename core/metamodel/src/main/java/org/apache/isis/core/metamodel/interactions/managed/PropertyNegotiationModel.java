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
package org.apache.isis.core.metamodel.interactions.managed;

import org.apache.isis.core.commons.binding.Bindable;
import org.apache.isis.core.commons.binding.Observable;
import org.apache.isis.core.commons.collections.Can;
import org.apache.isis.core.commons.internal.binding._BindableAbstract;
import org.apache.isis.core.commons.internal.binding._Bindables;
import org.apache.isis.core.commons.internal.binding._Observables;
import org.apache.isis.core.commons.internal.binding._Observables.LazyObservable;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ManagedObjects;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

import lombok.NonNull;
import lombok.val;

public class PropertyNegotiationModel implements ManagedValue {
    
    @NonNull private final _BindableAbstract<ManagedObject> proposedValue;
    @NonNull private final LazyObservable<String> validation;
    @NonNull private final _BindableAbstract<String> searchArgument;
    @NonNull private final LazyObservable<Can<ManagedObject>> choices;
    
    @NonNull private final ManagedProperty managedProperty;
    
    PropertyNegotiationModel(ManagedProperty managedProperty) {
        this.managedProperty = managedProperty;
        val propMeta = managedProperty.getMetaModel();
        
        validationFeedbackActive = _Bindables.forValue(false);
        
        val currentValue = managedProperty.getPropertyValue();
        val defaultValue = ManagedObjects.isNullOrUnspecifiedOrEmpty(currentValue)
            ? propMeta.getDefault(managedProperty.getOwner())
            : currentValue;
        
        proposedValue = _Bindables.forValue(defaultValue);
        proposedValue.addListener((e,o,n)->{
            invalidateChoicesAndValidation();
        });
        
        // has either autoComplete, choices, or none
        choices = propMeta.hasAutoComplete()
        ? _Observables.forFactory(()->
            propMeta.getAutoComplete(
                    managedProperty.getOwner(),
                    getSearchArgument().getValue(), 
                    InteractionInitiatedBy.USER))
        : propMeta.hasChoices() 
            ? _Observables.forFactory(()->
                propMeta.getChoices(managedProperty.getOwner(), InteractionInitiatedBy.USER))
            : _Observables.forFactory(Can::empty);

        // if has autoComplete, then activate the search argument        
        searchArgument = _Bindables.forValue(null);
        if(propMeta.hasAutoComplete()) {
            searchArgument.addListener((e,o,n)->{
                choices.invalidate();
            });
        }
        
        // validate this parameter, but only when validationFeedback has been activated
        validation = _Observables.forFactory(()->
            isValidationFeedbackActive()
            ? managedProperty.checkValidity(getValue().getValue())
                    .map(InteractionVeto::getReason)
                    .orElse(null)
            : (String)null); 
    }

    @Override
    public ObjectSpecification getSpecification() {
        return managedProperty.getSpecification();
    }
    
    @Override
    public Bindable<ManagedObject> getValue() {
        return proposedValue;
    }

    @Override
    public Observable<String> getValidationMessage() {
        return validation;
    }

    @Override
    public Bindable<String> getSearchArgument() {
        return searchArgument;
    }

    @Override
    public Observable<Can<ManagedObject>> getChoices() {
        return choices;
    }
    
    // -- VALIDATION
    
    @NonNull private final _BindableAbstract<Boolean> validationFeedbackActive;
    
    /**
     * Whether validation feedback is activated. Activates once user attempts to 'submit' an action.
     */
    @NonNull public Observable<Boolean> getObservableValidationFeedbackActive() {
        return validationFeedbackActive;
    }
    
    private boolean isValidationFeedbackActive() {
        return validationFeedbackActive.getValue();
    }
    
    public void invalidateChoicesAndValidation() {
        choices.invalidate();
        validation.invalidate();
    }
    
    // -- SUBMISSION

    public void submit() {
        managedProperty.modifyProperty(getValue().getValue());
    }



}
