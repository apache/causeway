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

import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.interactions.managed.ParameterNegotiationModel;
import org.apache.isis.core.metamodel.spec.ManagedObject;

import lombok.val;

import javafx.scene.control.ComboBox;

@SuppressWarnings("restriction")
public class SimulatedUiChoices {

    //    static interface ChoicesBinding {
    //        void propagateValueToBackend(ManagedObject newPendingParameterValue);
    //    }

    private final ComboBox<ManagedObject> choiceBox = new ComboBox<>();

    public void bind(ParameterNegotiationModel pendingArgs, int paramNr) {

        val actionMeta = pendingArgs.getHead().getMetaModel();
        val paramMetaList = actionMeta.getParameters();
        val paramMeta = paramMetaList.getElseFail(paramNr);

        val choices = paramMeta.getChoices(pendingArgs, InteractionInitiatedBy.USER);

        choices.stream()
        .forEach(choiceBox.getItems()::add);

        choiceBox.getSelectionModel().selectedItemProperty().addListener((e, o, n)->{
            // propagate changes from UI to backend
            pendingArgs.setParamValue(paramNr, n); // does not trigger change listeners
        });

        pendingArgs.getBindableParamValue(paramNr).addListener((e, o, n)->{
            // propagate changes from backend to UI
            //TODO disable change-listeners
            choiceBox.getSelectionModel().select(n); // does trigger change listeners???
            //TODO enable change-listeners
        });

    }

    public void simulateChoiceSelect(int choiceIndex) {
        choiceBox.getSelectionModel().clearAndSelect(choiceIndex);
    }

    public ManagedObject getValue() {
        return choiceBox.getSelectionModel().selectedItemProperty().get(); 
    }


}
