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

import org.apache.isis.applib.Identifier;
import org.apache.isis.core.commons.binding.Bindable;
import org.apache.isis.core.commons.binding.Observable;
import org.apache.isis.core.commons.collections.Can;
import org.apache.isis.core.commons.internal.binding._BindableAbstract;
import org.apache.isis.core.commons.internal.binding._Bindables;
import org.apache.isis.core.commons.internal.binding._Observables;
import org.apache.isis.core.commons.internal.binding._Observables.LazyObservable;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.consent.InteractionResult;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
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
                ? actionValidationMessage()
                : (String)null);
        
        // when activated make sure all validation is reassessed
        this.validationFeedbackActive.addListener((e,o,n)->{
            paramModels.forEach(ParameterModel::invalidateChoicesAndValidation);
            observableActionValidation.invalidate();
        });
    }
    
    // -- ACTION SPECIFIC
    
    public int getParamCount() {
        return paramModels.size();
    }

    @NonNull public Can<ManagedObject> getParamValues() {
        return paramModels
                .map(ParameterModel::getValue)
                .map(Bindable::getValue);
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

    public void setParamValues(@NonNull Can<ManagedObject> paramValues) {
        // allow overflow and underflow
        val valueIterator = paramValues.iterator();
        paramModels.forEach(paramModel->{
            if(!valueIterator.hasNext()) return;
            paramModel.getBindableParamValue().setValue(valueIterator.next());
        });
    }
    
    // -- PARAMETER SPECIFIC
    
    @NonNull public Can<? extends ManagedParameter> getParamModels() {
        return paramModels;
    }
    
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
    
    /** validates all, the action and each individual parameter */ 
    public Consent validateParameterSet() {
        val head = this.getHead();
        return head.getMetaModel().isArgumentSetValid(head, this.getParamValues(), InteractionInitiatedBy.USER);
    }
    
    public Consent validateParameterSetForAction() {
        val head = this.getHead();
        return head.getMetaModel().isArgumentSetValidForAction(head, this.getParamValues(), InteractionInitiatedBy.USER);
    }
    
    public Can<Consent> validateParameterSetForParameters() {
        val head = this.getHead();
        return head.getMetaModel()
                .isArgumentSetValidForParameters(head, this.getParamValues(), InteractionInitiatedBy.USER)
                .stream()
                .map(InteractionResult::createConsent)
                .collect(Can.toCan());
    }
    
    
    @NonNull public ManagedObject getParamValue(int paramNr) {
        return paramModels.getElseFail(paramNr).getValue().getValue();
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
    
    private String actionValidationMessage() {
        val validityConsentForAction = this.validateParameterSetForAction();
        if(validityConsentForAction!=null && validityConsentForAction.isVetoed()) {
            return validityConsentForAction.getReason(); 
        }
        return null;
    }
    
    // -- INTERNAL HOLDER OF PARAMETER BINDABLES
    
    private static class ParameterModel implements ManagedParameter {

        @Getter(onMethod_ = {@Override}) private final int paramNr;
        @Getter(onMethod_ = {@Override}) @NonNull private final ObjectActionParameter metaModel;
        @Getter(onMethod_ = {@Override}) @NonNull private final ParameterNegotiationModel negotiationModel;
        @Getter @NonNull private final _BindableAbstract<ManagedObject> bindableParamValue;
        @Getter @NonNull private final LazyObservable<String> observableParamValidation;
        @Getter @NonNull private final _BindableAbstract<String> bindableParamSearchArgument;
        @Getter @NonNull private final LazyObservable<Can<ManagedObject>> observableParamChoices;
        
        private ParameterModel(
                int paramNr, 
                @NonNull ParameterNegotiationModel negotiationModel,
                @NonNull ManagedObject initialValue) {
            
            this.paramNr = paramNr;
            this.metaModel = negotiationModel.getHead().getMetaModel().getParameters().getElseFail(paramNr);
            this.negotiationModel = negotiationModel;
            
            bindableParamValue = _Bindables.forValue(initialValue);
            bindableParamValue.addListener((e,o,n)->{
                getNegotiationModel().onNewParamValue();
            });
            
            
            // has either autoComplete, choices, or none
            observableParamChoices = metaModel.hasAutoComplete()
            ? _Observables.forFactory(()->
                getMetaModel().getAutoComplete(
                        getNegotiationModel(), 
                        getBindableParamSearchArgument().getValue(), 
                        InteractionInitiatedBy.USER))
            : metaModel.hasChoices() 
                ? _Observables.forFactory(()->
                    getMetaModel().getChoices(getNegotiationModel(), InteractionInitiatedBy.USER))
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
                        .isValid(getNegotiationModel().head, getNegotiationModel().getParamValues(), InteractionInitiatedBy.USER)
                        .getReason()
                : (String)null); 
        }
        
//        public @NonNull ManagedObject getValue() {
//            return bindableParamValue.getValue();
//        }
        
        public void invalidateChoicesAndValidation() {
            observableParamChoices.invalidate();
            observableParamValidation.invalidate();
        }
        
        private boolean isValidationFeedbackActive() {
            return getNegotiationModel().getObservableValidationFeedbackActive().getValue();
        }
        
        // -- MANAGED PARAMETER 

        @Override
        public Identifier getIdentifier() {
            return getMetaModel().getIdentifier();
        }

        @Override
        public String getDisplayLabel() {
            return getMetaModel().getName();
        }

        @Override
        public ObjectSpecification getSpecification() {
            return getMetaModel().getSpecification();
        }

        @Override
        public Bindable<ManagedObject> getValue() {
            return bindableParamValue;
        }

        @Override
        public Observable<String> getValidationMessage() {
            return observableParamValidation;
        }

        @Override
        public Bindable<String> getSearchArgument() {
            return bindableParamSearchArgument;
        }

        @Override
        public Observable<Can<ManagedObject>> getChoices() {
            return observableParamChoices;
        }
        
    }

    
}
