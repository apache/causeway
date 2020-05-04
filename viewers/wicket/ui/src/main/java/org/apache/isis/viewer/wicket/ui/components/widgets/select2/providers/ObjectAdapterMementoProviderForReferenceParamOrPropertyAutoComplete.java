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
package org.apache.isis.viewer.wicket.ui.components.widgets.select2.providers;

import org.apache.isis.core.commons.collections.Can;
import org.apache.isis.core.commons.internal.base._NullSafe;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.specloader.specimpl.PendingParameterModel;
import org.apache.isis.core.webapp.context.memento.ObjectMemento;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.model.models.ScalarParameterModel;

import lombok.NonNull;
import lombok.val;

public class ObjectAdapterMementoProviderForReferenceParamOrPropertyAutoComplete
extends ObjectAdapterMementoProviderAbstract {

    private static final long serialVersionUID = 1L;
    
    private final Can<ObjectMemento> dependentArgMementos;

    public ObjectAdapterMementoProviderForReferenceParamOrPropertyAutoComplete(
            @NonNull ScalarModel model,
            @NonNull Can<ObjectMemento> dependentArgMementos) { 
        
        super(model);
        this.dependentArgMementos = dependentArgMementos;
    }

    @Override
    protected Can<ObjectMemento> obtainMementos(String term) {
        
        val parameterModel = (ScalarParameterModel)getScalarModel();
        
        if (parameterModel.hasAutoComplete()) {
        
            val commonContext = super.getCommonContext();
            
            // recover any pendingArgs
            val pendingArgs = reconstructDependentArgs(parameterModel, dependentArgMementos); 
            return parameterModel
                    .getAutoComplete(pendingArgs, term)
                    .map(commonContext::mementoFor);
            
        }
        
        return Can.empty();
        
    }
    
    private PendingParameterModel reconstructDependentArgs(
            final ScalarParameterModel parameterModel, 
            final Can<ObjectMemento> dependentArgMementos) {
        
        val commonContext = super.getCommonContext();
        val pendingArgsList = _NullSafe.stream(dependentArgMementos)
            .map(commonContext::reconstructObject)
            .map(ManagedObject.class::cast)
            .collect(Can.toCan());
        
       return parameterModel.getPendingParamHead()
            .model(pendingArgsList);
    }

}
