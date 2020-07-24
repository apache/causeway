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
import org.apache.isis.core.commons.internal.base._Lazy;
import org.apache.isis.core.commons.internal.binding._BindableAbstract;
import org.apache.isis.core.commons.internal.binding._Bindables;
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
    
    private ParameterNegotiationModel(
            @NonNull final ActionInteractionHead head,
            @NonNull final Can<ManagedObject> initialParamValues) {
        this.head = head;
        val paramNrIterator = IntStream.range(0, initialParamValues.size()).iterator();
        this.paramModels = initialParamValues
                .map(initialValue->new ParameterModel(paramNrIterator.nextInt(), this, initialValue));
        this.validationFeedbackActive = _Bindables.forValue(false);
    }
    
    // -- ACTION SPECIFIC

    @NonNull public Can<ManagedObject> getParamValues() {
        return paramModels.map(ParameterModel::getValue);
    }
    
    @NonNull public ManagedObject getActionTarget() {
        return getHead().getTarget();
    }
    
    @NonNull public Observable<String> getObservableActionValidation() {
        // TODO listen to any user initiated submit attempt then validate the action
        // this also turns on validationFeedback
        return _Bindables.forValue(null);
    }
    
    /**
     * Whether validation feedback is activated. Activates once user attempts to 'submit' an action.
     */
    @NonNull public Observable<Boolean> getObservableValidationFeedbackActive() {
        return validationFeedbackActive;
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

    public void submit(@NonNull ActionInteraction actionInteraction) {
        validationFeedbackActive.setValue(true);
        // TODO validate pendingArgs
        // TODO only if all is sound, invoke the action
    }
    
    private void onNewParamValue() {
        paramModels.forEach(ParameterModel::invalidateChoicesAndValidation);
    }
    
    // -- INTERNAL HOLDER OF PARAMETER BINDABLES
    
    private static class ParameterModel {

        @Getter private final int paramNr;
        @Getter @NonNull private final ObjectActionParameter metaModel;
        @Getter @NonNull private final ParameterNegotiationModel model;
        @Getter @NonNull private final _BindableAbstract<ManagedObject> bindableParamValue;
        @Getter @NonNull private final _BindableAbstract<String> observableParamValidation;
        @Getter @NonNull private final _BindableAbstract<String> bindableParamSearchArgument;
        @Getter @NonNull private final _BindableAbstract<Can<ManagedObject>> observableParamChoices;
        
        private final _Lazy<Can<ManagedObject>> paramChoices = _Lazy.threadSafe(()->
            getMetaModel().getChoices(getModel(), InteractionInitiatedBy.USER));
        
        private final _Lazy<Can<ManagedObject>> autoCompleteChoices = _Lazy.threadSafe(()->
            getMetaModel().getAutoComplete(getModel(), getBindableParamSearchArgument().getValue(), InteractionInitiatedBy.USER));
        
        private final _Lazy<String> validationMessage = _Lazy.threadSafe(()->
            getMetaModel().isValid(getModel().head, getValue(), InteractionInitiatedBy.USER)); // TODO consider all params
        
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
            
            
            bindableParamSearchArgument = _Bindables.forValue(null);
            bindableParamSearchArgument.addListener((e,o,n)->{
                autoCompleteChoices.clear();
                //TODO also trigger updates to be propagated to UI
            });

            // has either autoComplete, choices, or none
            observableParamChoices = metaModel.hasAutoComplete()
            ? new _BindableAbstract<Can<ManagedObject>>() {
                @Override
                public Can<ManagedObject> getValue() {
                    return autoCompleteChoices.get();
                }
            }
            : metaModel.hasChoices() 
                ? new _BindableAbstract<Can<ManagedObject>>() {
                    @Override
                    public Can<ManagedObject> getValue() {
                        return paramChoices.get();
                    }
                }
                : _Bindables.forValue(Can.empty());
            
            // TODO listen to any user initiated value changes, then validate the corresponding parameter
            // but only after validationFeedback has been turned on
            observableParamValidation = _Bindables.forValue(null);
            
        }
        
        public String getName() {
            return getMetaModel().getName();
        }
        
        public @NonNull ManagedObject getValue() {
            return bindableParamValue.getValue();
        }
        
        public void invalidateChoicesAndValidation() {
            paramChoices.clear();
            autoCompleteChoices.clear();
            validationMessage.clear();
            //TODO also trigger updates to be propagated to UI
        }
        
    }




    
    
}
