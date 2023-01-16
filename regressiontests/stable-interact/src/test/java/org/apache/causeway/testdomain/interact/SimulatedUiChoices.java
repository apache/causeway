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
package org.apache.causeway.testdomain.interact;

import java.util.concurrent.atomic.LongAdder;

import org.apache.causeway.commons.binding.Bindable;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.binding._Bindables;
import org.apache.causeway.core.metamodel.interactions.managed.ManagedValue;
import org.apache.causeway.core.metamodel.interactions.managed.ParameterNegotiationModel;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.feature.ObjectFeature;

import lombok.Getter;
import lombok.val;

public class SimulatedUiChoices extends HasValueValidation {

    private final Bindable<Can<ManagedObject>> choiceBox = _Bindables.empty();
    private final Bindable<ManagedObject> selectedItem = _Bindables.empty();

    @Getter private final LongAdder choiceBoxUpdateEventCount = new LongAdder();
    @Getter private final LongAdder selectedItemUpdateEventCount = new LongAdder();

    private ObjectFeature objectFeature;

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

        objectFeature = managedValue.getObjectFeature();
    }

    public void bind(final ParameterNegotiationModel pendingArgs, final int paramNr) {
        bind(pendingArgs.getParamModels().getElseFail(paramNr));
    }

    /**
     * assuming the parameter is a singular type
     * @param choiceIndex
     */
    public void simulateChoiceSelect(final int choiceIndex) {
        selectedItem.setValue(choiceBox.getValue().getElseFail(choiceIndex));
    }

    /**
     * assuming the parameter is a plural type
     * @param choiceIndices
     */
    public void simulateMultiChoiceSelect(final int ... choiceIndices) {
        val newValues = choiceBox.getValue()
                .pickByIndex(choiceIndices);
        selectedItem.setValue(ManagedObject.packed(objectFeature, newValues));
    }

    public ManagedObject getValue() {
        return selectedItem.getValue();
    }

    public Can<ManagedObject> getChoices() {
        return choiceBox.getValue();
    }

}
