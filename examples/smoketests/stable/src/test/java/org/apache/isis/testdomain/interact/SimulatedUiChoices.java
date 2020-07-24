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

import java.util.concurrent.atomic.LongAdder;

import org.apache.isis.core.commons.binding.Bindable;
import org.apache.isis.core.commons.collections.Can;
import org.apache.isis.core.commons.internal.binding._Bindables;
import org.apache.isis.core.metamodel.interactions.managed.ParameterNegotiationModel;
import org.apache.isis.core.metamodel.spec.ManagedObject;

import lombok.Getter;

public class SimulatedUiChoices extends HasParameterValidation {

    private final Bindable<Can<ManagedObject>> choiceBox = _Bindables.empty();
    private final Bindable<ManagedObject> selectedItem = _Bindables.empty();
    
    @Getter private final LongAdder choiceBoxUpdateEventCount = new LongAdder();
    @Getter private final LongAdder selectedItemUpdateEventCount = new LongAdder();
    
    public void bind(ParameterNegotiationModel pendingArgs, int paramNr) {
        choiceBox.bind(pendingArgs.getObservableParamChoices(paramNr));
        choiceBox.addListener((e,o,n)->{
            choiceBoxUpdateEventCount.increment();
        });
        selectedItem.bindBidirectional(pendingArgs.getBindableParamValue(paramNr));
        super.bind(pendingArgs, paramNr);
        
        selectedItem.addListener((e,o,n)->{
            selectedItemUpdateEventCount.increment();
        });
    }

    public void simulateChoiceSelect(int choiceIndex) {
        selectedItem.setValue(choiceBox.getValue().getElseFail(choiceIndex));
    }

    public ManagedObject getValue() {
        return selectedItem.getValue(); 
    }
    
    public Can<ManagedObject> getChoices() {
        return choiceBox.getValue(); 
    }
    
}
