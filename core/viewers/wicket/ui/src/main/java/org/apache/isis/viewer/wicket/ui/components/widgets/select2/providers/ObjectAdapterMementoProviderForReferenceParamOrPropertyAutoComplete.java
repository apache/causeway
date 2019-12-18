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

import java.util.ArrayList;
import java.util.List;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.metamodel.spec.ManagedObject;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.webapp.context.memento.ObjectMemento;

import lombok.val;

public class ObjectAdapterMementoProviderForReferenceParamOrPropertyAutoComplete
extends ObjectAdapterMementoProviderAbstract {

    private static final long serialVersionUID = 1L;
    
    private final ArrayList<ObjectMemento> dependentArgMementos;

    public ObjectAdapterMementoProviderForReferenceParamOrPropertyAutoComplete(
            ScalarModel model,
            ArrayList<ObjectMemento> dependentArgMementos) { 
        
        super(model);
        this.dependentArgMementos = dependentArgMementos;
    }

    @Override
    protected List<ObjectMemento> obtainMementos(String term) {
        
        val commonContext = super.getCommonContext();
        
        val autoCompleteChoices = _Lists.<ManagedObject>newArrayList();
        if (getScalarModel().hasAutoComplete()) {
            // recover any pendingArgs
            val pendingArgs = reconstructDependentArgs(dependentArgMementos); 
            val autoCompleteAdapters = getScalarModel()
                    .getAutoComplete(pendingArgs, term, commonContext.getAuthenticationSession());
            autoCompleteChoices.addAll(autoCompleteAdapters);
        }
        
        return _Lists.map(autoCompleteChoices, commonContext::mementoFor);
        
    }
    
    private Can<ManagedObject> reconstructDependentArgs(List<ObjectMemento> dependentArgMementos) {
        val commonContext = super.getCommonContext();
        val pendingArgsStream = _NullSafe.stream(dependentArgMementos)
            .map(commonContext::reconstructObject)
            .map(ManagedObject.class::cast);
        
        return Can.ofStream(pendingArgsStream);
    }

}
