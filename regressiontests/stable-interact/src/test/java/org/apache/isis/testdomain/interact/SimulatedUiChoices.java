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

import org.apache.isis.commons.binding.Bindable;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.binding._Bindables;
import org.apache.isis.core.metamodel.interactions.managed.ManagedValue;
import org.apache.isis.core.metamodel.interactions.managed.ParameterNegotiationModel;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

import lombok.Getter;
import lombok.val;

public class SimulatedUiChoices extends HasValueValidation {

    private final Bindable<Can<ManagedObject>> choiceBox = _Bindables.empty();
    private final Bindable<ManagedObject> selectedItem = _Bindables.empty();

    @Getter private final LongAdder choiceBoxUpdateEventCount = new LongAdder();
    @Getter private final LongAdder selectedItemUpdateEventCount = new LongAdder();

    private ObjectSpecification valueSpecification;

    @Override
    public void bind(final ManagedValue managedValue) {
        choiceBox.bind(managedValue.getChoices());
        choiceBox.addListener((e,o,n)->{
            choiceBoxUpdateEventCount.increment();
        });
        selectedItem.bindBidirectional(managedValue.getValue());
        super.bind(managedValue);

        selectedItem.addListener((e,o,n)->{
            selectedItemUpdateEventCount.increment();
        });

        valueSpecification = managedValue.getElementType();
    }

    public void bind(final ParameterNegotiationModel pendingArgs, final int paramNr) {
        bind(pendingArgs.getParamModels().getElseFail(paramNr));
    }

    /**
     * assuming the parameter is a scalar type
     * @param choiceIndex
     */
    public void simulateChoiceSelect(final int choiceIndex) {
        selectedItem.setValue(choiceBox.getValue().getElseFail(choiceIndex));
    }

    /**
     * assuming the parameter is a non-scalar type
     * @param choiceIndices
     */
    public void simulateMultiChoiceSelect(final int ... choiceIndices) {
        val newValuePojos = choiceBox.getValue()
                .pickByIndex(choiceIndices)
                .map(ManagedObject::getPojo);
        val newValue = ManagedObject.of(
                valueSpecification,
                newValuePojos.toList());
        selectedItem.setValue(newValue);
    }

    public ManagedObject getValue() {
        return selectedItem.getValue();
    }

    public Can<ManagedObject> getChoices() {
        return choiceBox.getValue();
    }

}
