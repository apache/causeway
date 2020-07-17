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

import java.util.Map;

import javax.annotation.Nullable;

import org.apache.isis.core.commons.binding.Bindable;
import org.apache.isis.core.commons.collections.Can;
import org.apache.isis.core.commons.internal.binding._BindableAbstract;
import org.apache.isis.core.commons.internal.binding._Bindables;
import org.apache.isis.core.commons.internal.collections._Maps;
import org.apache.isis.core.metamodel.spec.ManagedObject;

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
    
    public Can<ManagedObject> getParamValues() {
        return paramValues.map(Bindable::getValue);
    }
    
    @NonNull public Bindable<ManagedObject> getBindableParamValue(int paramNr) {
        return paramValues.getElseFail(paramNr);
    }
    
    @NonNull public Bindable<Can<ManagedObject>> getBindableParamChoices(int paramNr) {
        //TODO
        return null;
    }
    
    @NonNull public ManagedObject getActionTarget() {
        return getHead().getTarget();
    }

    @NonNull public ManagedObject getParamValue(int paramNr) {
        return paramValues.getElseFail(paramNr).getValue();
    }

    public void setParamValue(int paramNr, @NonNull ManagedObject newParamValue) {
        paramValues.getElseFail(paramNr).setValue(newParamValue);
    }

    public ManagedObject adaptParamValuePojo(int paramNr, @Nullable Object newParamValuePojo) {
        val paramMeta = head.getMetaModel().getParameters().getElseFail(paramNr);
        val paramSpec = paramMeta.getSpecification();
        val paramValue = newParamValuePojo!=null
                ? ManagedObject.of(paramSpec, newParamValuePojo)
                : ManagedObject.empty(paramSpec);
        return paramValue;
    }

    // -- TODO UNDER CONSTRUCTION ...
    
    @RequiredArgsConstructor(staticName = "of")
    public static class BindableParameter extends _BindableAbstract<ManagedObject> {

        @Getter @NonNull private int paramNr;
        @Getter @NonNull private ParameterNegotiationModel model;

        public String getName() {
            val paramMeta = model.getHead().getMetaModel().getParameters().getElseFail(paramNr);
            return paramMeta.getName();
        }
        
        @Override
        public ManagedObject getValue() {
            return model.getParamValue(paramNr);
        }
        
        @Override
        public void setValue(final ManagedObject newValue) {
            val oldValue = getValue(); 
            model.setParamValue(paramNr, newValue);
        }
        
    }


    
    
}
