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

import java.util.stream.IntStream;

import javax.annotation.Nullable;

import org.apache.isis.core.commons.binding.Bindable;
import org.apache.isis.core.commons.binding.Observable;
import org.apache.isis.core.commons.collections.Can;
import org.apache.isis.core.commons.internal.binding._BindableAbstract;
import org.apache.isis.core.commons.internal.binding._Bindables;
import org.apache.isis.core.commons.internal.binding._Observables;
import org.apache.isis.core.commons.internal.binding._Observables.LazyObservable;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;

import lombok.Getter;
import lombok.NonNull;
import lombok.val;


/**
 * Model used to negotiate the parameter values of an action by means of an UI dialog. 
 * <p>
 * This supports aspects of UI component binding to pending values and possible choices,
 * as well as validation failures. 
 *  
 * @since 2.0.0
 */
public class ParameterNegotiationModel {
    
    public static ParameterNegotiationModel of(
            @NonNull final ActionInteractionHead head,
            @NonNull final Can<ManagedObject> initialParamValues) {
        return new ParameterNegotiationModel(head, initialParamValues);
                
    }

    @Getter private final ActionInteractionHead head;
    private final Can<ParameterModel> paramModels;
    private final _BindableAbstract<Boolean> validationFeedbackActive;
    private final LazyObservable<String> observableActionValidation;
    
    private ParameterNegotiationModel(
            @NonNull final ActionInteractionHead head,
            @NonNull final Can<ManagedObject> initialParamValues) {
        this.head = head;
        this.validationFeedbackActive = _Bindables.forValue(false);
        
        val paramNrIterator = IntStream.range(0, initialParamValues.size()).iterator();
        this.paramModels = initialParamValues
                .map(initialValue->new ParameterModel(paramNrIterator.nextInt(), this, initialValue));
        
        this.observableActionValidation = _Observables.forFactory(()->
            validationFeedbackActive.getValue()
            ? head.getMetaModel()
                    .isArgumentSetValidForAction(head, getParamValues(), InteractionInitiatedBy.USER)
                    .getReason()
            : (String)null);
    }
    
    // -- ACTION SPECIFIC

    @NonNull public Can<ManagedObject> getParamValues() {
        return paramModels.map(ParameterModel::getValue);
    }
    
    @NonNull public ManagedObject getActionTarget() {
        return getHead().getTarget();
    }
    
    @NonNull public Observable<String> getObservableActionValidation() {
        return observableActionValidation;
    }
    
    /**
     * Whether validation feedback is activated. Activates once user attempts to 'submit' an action.
     */
    @NonNull public Observable<Boolean> getObservableValidationFeedbackActive() {
        return validationFeedbackActive;
    }
    
    public boolean isActionInvocationVetoed() {
        activateValidationFeedback();
        paramModels.forEach(ParameterModel::invalidateChoicesAndValidation);
        observableActionValidation.invalidate();
        
        if(observableActionValidation.getValue()!=null) {
            return true; // action invocation is vetoed by action validation
        }
        if(paramModels.stream()
                .anyMatch(pm->pm.getObservableParamValidation().getValue()!=null)) {
            return true; // action invocation is vetoed by param validation
        }
        return false;
    }

    // -- PARAMETER SPECIFIC
    
    @NonNull public ObjectActionParameter getParamMetamodel(int paramNr) {
        return paramModels.getElseFail(paramNr).getMetaModel();
    }
    
    @NonNull public Bindable<ManagedObject> getBindableParamValue(int paramNr) {
        return paramModels.getElseFail(paramNr).getBindableParamValue();
    }
    
    @NonNull public Observable<Can<ManagedObject>> getObservableParamChoices(int paramNr) {
        return paramModels.getElseFail(paramNr).getObservableParamChoices();
    }
    
    @NonNull public Observable<String> getObservableParamValidation(int paramNr) {
        return paramModels.getElseFail(paramNr).getObservableParamValidation();
    }
    
    @NonNull public Bindable<String> getBindableParamSearchArgument(int paramNr) {
        return paramModels.getElseFail(paramNr).getBindableParamSearchArgument();
    }

    // -- RATHER INTERNAL ... 
    
    @NonNull public ManagedObject getParamValue(int paramNr) {
        return paramModels.getElseFail(paramNr).getValue();
    }

    public void setParamValue(int paramNr, @NonNull ManagedObject newParamValue) {
        paramModels.getElseFail(paramNr).getBindableParamValue().setValue(newParamValue);
    }

    @NonNull public ManagedObject adaptParamValuePojo(int paramNr, @Nullable Object newParamValuePojo) {
        val paramMeta = getParamMetamodel(paramNr);
        val paramSpec = paramMeta.getSpecification();
        val paramValue = newParamValuePojo!=null
                ? ManagedObject.of(paramSpec, newParamValuePojo)
                : ManagedObject.empty(paramSpec);
        return paramValue;
    }

    /**
     * exposed for testing
     */
    public void activateValidationFeedback() {
        validationFeedbackActive.setValue(true);
    }
    
    private void onNewParamValue() {
        paramModels.forEach(ParameterModel::invalidateChoicesAndValidation);
        observableActionValidation.invalidate();
    }
    
    // -- INTERNAL HOLDER OF PARAMETER BINDABLES
    
    private static class ParameterModel {

        @Getter private final int paramNr;
        @Getter @NonNull private final ObjectActionParameter metaModel;
        @Getter @NonNull private final ParameterNegotiationModel model;
        @Getter @NonNull private final _BindableAbstract<ManagedObject> bindableParamValue;
        @Getter @NonNull private final LazyObservable<String> observableParamValidation;
        @Getter @NonNull private final _BindableAbstract<String> bindableParamSearchArgument;
        @Getter @NonNull private final LazyObservable<Can<ManagedObject>> observableParamChoices;
        
        private ParameterModel(
                int paramNr, 
                @NonNull ParameterNegotiationModel model,
                @NonNull ManagedObject initialValue) {
            
            this.paramNr = paramNr;
            this.metaModel = model.getHead().getMetaModel().getParameters().getElseFail(paramNr);
            this.model = model;
            
            bindableParamValue = _Bindables.forValue(initialValue);
            bindableParamValue.addListener((e,o,n)->{
                getModel().onNewParamValue();
            });
            
            
            // has either autoComplete, choices, or none
            observableParamChoices = metaModel.hasAutoComplete()
            ? _Observables.forFactory(()->
                getMetaModel().getAutoComplete(
                        getModel(), 
                        getBindableParamSearchArgument().getValue(), 
                        InteractionInitiatedBy.USER))
            : metaModel.hasChoices() 
                ? _Observables.forFactory(()->
                    getMetaModel().getChoices(getModel(), InteractionInitiatedBy.USER))
                : _Observables.forFactory(Can::empty);

            // if has autoComplete, then activate the search argument        
            bindableParamSearchArgument = _Bindables.forValue(null);
            if(metaModel.hasAutoComplete()) {
                bindableParamSearchArgument.addListener((e,o,n)->{
                    observableParamChoices.invalidate();
                });
            }
            
            // validate this parameter, but only when validationFeedback has been activated
            observableParamValidation = _Observables.forFactory(()->
                isValidationFeedbackActive()
                ? getMetaModel()
                        .isValid(getModel().head, getModel().getParamValues(), InteractionInitiatedBy.USER)
                        .getReason()
                : (String)null); 
        }
        
        @SuppressWarnings("unused")
        public String getName() {
            return getMetaModel().getName();
        }
        
        public @NonNull ManagedObject getValue() {
            return bindableParamValue.getValue();
        }
        
        public void invalidateChoicesAndValidation() {
            observableParamChoices.invalidate();
            observableParamValidation.invalidate();
        }
        
        private boolean isValidationFeedbackActive() {
            return getModel().getObservableValidationFeedbackActive().getValue();
        }
        
    }




    
    
}
