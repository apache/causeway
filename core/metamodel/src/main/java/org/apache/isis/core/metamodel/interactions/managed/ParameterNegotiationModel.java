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

import javax.annotation.Nullable;

import org.apache.isis.core.commons.binding.Bindable;
import org.apache.isis.core.commons.binding.Observable;
import org.apache.isis.core.commons.collections.Can;
import org.apache.isis.core.commons.internal.binding._Bindables;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;


/**
 * Model used to negotiate the parameter values of an action by means of an UI dialog. 
 * <p>
 * This supports aspects of UI component binding to pending values and possible choices,
 * as well as validation failures. 
 *  
 * @since 2.0.0
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ParameterNegotiationModel {
    
    public static ParameterNegotiationModel of(
            @NonNull final ActionInteractionHead head,
            @NonNull final Can<ManagedObject> initialParamValues) {
        return new ParameterNegotiationModel(
                head, 
                initialParamValues.map(_Bindables::forValue));
    }
    
    @Getter private final ActionInteractionHead head;
    private final Can<Bindable<ManagedObject>> paramValues;

    @NonNull public ManagedObject getActionTarget() {
        return getHead().getTarget();
    }
    
    @NonNull public ObjectActionParameter getParamMetamodel(int paramNr) {
        return head.getMetaModel().getParameters().getElseFail(paramNr);
    }
    
    @NonNull public Can<ManagedObject> getParamValues() {
        return paramValues.map(Bindable::getValue);
    }
    
    @NonNull public Bindable<ManagedObject> getBindableParamValue(int paramNr) {
        return paramValues.getElseFail(paramNr);
    }
    
    @NonNull public Observable<Can<ManagedObject>> getObservableParamChoices(int paramNr) {
        val choices = getParamMetamodel(paramNr).getChoices(this, InteractionInitiatedBy.USER);
        //TODO well not quite, also need to listen to any param value changes, that invalidate these choices
        return _Bindables.forValue(choices);
    }
    
    @NonNull public Observable<String> getObservableParamValidation(int paramNr) {
        // TODO listen to any user initiated value changes, then validate the corresponding parameter
        // but only after validationFeedback has been turned on
        return _Bindables.forValue(null);
    }
    
    @NonNull public Bindable<String> getBindableParamSearchArgument(int paramNr) {
        // TODO any changes should trigger an update to observableParamChoices
        return _Bindables.forValue(null);
    }
    
    @NonNull public Observable<String> getObservableActionValidation() {
        // TODO listen to any user initiated submit attempt then validate the action
        // this also turns on validationFeedback
        return _Bindables.forValue(null);
    }
    
    /**
     * Whether validation feedback is activated or not. Activates once user attempts to 'submit' an action.
     */
    @NonNull public Observable<Boolean> getObservableValidationFeedbackState() {
        // TODO listen to any user initiated submit attempt then activate validationFeedback
        return _Bindables.forValue(false);
    }

    // -- RATHER INTERNAL ... 
    
    @NonNull public ManagedObject getParamValue(int paramNr) {
        return paramValues.getElseFail(paramNr).getValue();
    }

    public void setParamValue(int paramNr, @NonNull ManagedObject newParamValue) {
        paramValues.getElseFail(paramNr).setValue(newParamValue);
    }

    @NonNull public ManagedObject adaptParamValuePojo(int paramNr, @Nullable Object newParamValuePojo) {
        val paramMeta = getParamMetamodel(paramNr);
        val paramSpec = paramMeta.getSpecification();
        val paramValue = newParamValuePojo!=null
                ? ManagedObject.of(paramSpec, newParamValuePojo)
                : ManagedObject.empty(paramSpec);
        return paramValue;
    }

    // -- TODO UNDER CONSTRUCTION ...
    
//    @RequiredArgsConstructor(staticName = "of")
//    public static class BindableParameter extends _BindableAbstract<ManagedObject> {
//
//        @Getter private final int paramNr;
//        @Getter @NonNull private final ParameterNegotiationModel model;
//
//        public String getName() {
//            val paramMeta = model.getParamMetamodel(paramNr);
//            return paramMeta.getName();
//        }
//        
//        @Override
//        public ManagedObject getValue() {
//            return model.getParamValue(paramNr);
//        }
//        
//        @Override
//        public void setValue(final ManagedObject newValue) {
//            val oldValue = getValue(); 
//            model.setParamValue(paramNr, newValue);
//        }
//        
//    }

    
    
}
