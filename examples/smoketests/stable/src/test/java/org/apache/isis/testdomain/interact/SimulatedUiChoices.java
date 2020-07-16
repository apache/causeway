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
package org.apache.isis.testdomain.interact;

import org.apache.isis.core.commons.binding.Bindable;
import org.apache.isis.core.commons.collections.Can;
import org.apache.isis.core.commons.internal.binding._Bindables;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.interactions.managed.ParameterNegotiationModel;
import org.apache.isis.core.metamodel.spec.ManagedObject;

import lombok.val;

public class SimulatedUiChoices {

    private final Bindable<Can<ManagedObject>> choiceBox = 
            _Bindables.empty();

    private final Bindable<ManagedObject> selectedItem = 
            _Bindables.empty();
    
    public void bind(ParameterNegotiationModel pendingArgs, int paramNr) {

        val actionMeta = pendingArgs.getHead().getMetaModel();
        val paramMetaList = actionMeta.getParameters();
        val paramMeta = paramMetaList.getElseFail(paramNr);

        val choices = paramMeta.getChoices(pendingArgs, InteractionInitiatedBy.USER);

        choiceBox.setValue(choices.stream()
                .collect(Can.toCan()));
        
        selectedItem.addListener((e,o,n)->{
            // propagate changes from UI to backend
            pendingArgs.setParamValue(paramNr, n); // does not trigger change listeners
        });

        pendingArgs.getBindableParamValue(paramNr).addListener((e, o, n)->{
            // propagate changes from backend to UI
            //TODO disable change-listeners
            selectedItem.setValue(n); // does trigger change listeners
            //TODO enable change-listeners
        });
        
    }

    public void simulateChoiceSelect(int choiceIndex) {
        selectedItem.setValue(choiceBox.getValue().getElseFail(choiceIndex));
    }

    public ManagedObject getValue() {
        return selectedItem.getValue(); 
    }


}
