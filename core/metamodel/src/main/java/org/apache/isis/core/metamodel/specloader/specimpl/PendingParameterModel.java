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
package org.apache.isis.core.metamodel.specloader.specimpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.apache.isis.core.commons.binding.Bindable;
import org.apache.isis.core.commons.binding.ChangeListener;
import org.apache.isis.core.commons.binding.InvalidationListener;
import org.apache.isis.core.commons.binding.Observable;
import org.apache.isis.core.commons.collections.Can;
import org.apache.isis.core.commons.internal.collections._Maps;
import org.apache.isis.core.metamodel.interactions.managed.ActionInteractionHead;
import org.apache.isis.core.metamodel.spec.ManagedObject;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;


/**
 * Model used to negotiate the paramValues of an action by means of an UI dialog.
 *  
 * @since 2.0.0
 */
@Getter 
@RequiredArgsConstructor(staticName = "of")
public class PendingParameterModel {

    @NonNull private final ActionInteractionHead head;
    @NonNull private Can<ManagedObject> paramValues;
    
    // -- SHORTCUTS
    
    @NonNull public ManagedObject getActionTarget() {
        return getHead().getTarget();
    }

    @NonNull public ManagedObject getParamValue(int paramNum) {
        return paramValues.getElseFail(paramNum);
    }

    public void setParamValue(int paramNr, @NonNull ManagedObject newParamValue) {
        paramValues = paramValues.replace(paramNr, newParamValue);
    }

    public ManagedObject adaptParamValuePojo(int paramNr, @Nullable Object newParamValuePojo) {
        val paramMeta = head.getMetaModel().getParameters().getElseFail(paramNr);
        val paramSpec = paramMeta.getSpecification();
        val paramValue = newParamValuePojo!=null
                ? ManagedObject.of(paramSpec, newParamValuePojo)
                : ManagedObject.empty(paramSpec);
        return paramValue;
    }

    @RequiredArgsConstructor(staticName = "of")
    public static class BindableManagedObject implements Bindable<ManagedObject> {

        @Getter @NonNull private int paramNr;
        @Getter @NonNull private PendingParameterModel model;
        
        public Object getBean() {
            return model;
        }

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
            changeListeners.forEach(listener->{
                listener.changed(this, oldValue, newValue);
            }); 
        }
        
        private final List<ChangeListener<? super ManagedObject>> changeListeners = new ArrayList<>();
//        private final List<InvalidationListener> invalidationListeners = new ArrayList<>();

        @Override
        public void addListener(ChangeListener<? super ManagedObject> listener) {
            changeListeners.add(listener);
        }

        @Override
        public void removeListener(ChangeListener<? super ManagedObject> listener) {
            changeListeners.remove(listener);
        }

        @Override
        public void unbind() {
            // TODO Auto-generated method stub
            
        }

        @Override
        public boolean isBound() {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public void bind(Observable<? extends ManagedObject> observable) {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void bindBidirectional(Bindable<ManagedObject> other) {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void unbindBidirectional(Bindable<ManagedObject> other) {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void addListener(InvalidationListener listener) {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void removeListener(InvalidationListener listener) {
            // TODO Auto-generated method stub
            
        }

        
    }
    
    private final Map<Integer, BindableManagedObject> 
        bindableParamValueByParamNr = _Maps.newConcurrentHashMap();
    
    public BindableManagedObject getBindableParamValue(final int paramNr) {
        return bindableParamValueByParamNr
                .computeIfAbsent(paramNr, __->
                    BindableManagedObject.of(paramNr, this));
    }
    
}
