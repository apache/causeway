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
package org.apache.causeway.viewer.wicket.ui.test.components.widgets.choices;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.wicketstuff.select2.Response;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.metamodel._testing._TestDummies;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.objectmanager.memento.ObjectMemento;
import org.apache.causeway.viewer.wicket.ui.components.widgets.select2.providers.ChoiceProviderDefault;

class ChoiceProviderForReferencesTest extends ChoiceProviderTestAbstract {

    @BeforeEach
    void setup() throws Exception {
        super.setUp();
    }

    @Test
    void roundtrip() {

        var a = new _TestDummies.CustomerAsViewmodel("a");
        var b = new _TestDummies.CustomerAsViewmodel("b");
        var c = new _TestDummies.CustomerAsViewmodel("c");

        var choiceValues = Can.of(a, b, c);

        var choices = choiceValues
                .map(mmc.getObjectManager()::adapt);

        var isRequired = true;
        var choiceProvider = new ChoiceProviderDefault(mockScalarModel(choices, isRequired));

        var response = new Response<ObjectMemento>();
        choiceProvider.query(null, 0, response);
        var mementos = Can.ofCollection(response.getResults()); // throws null if any away

        assertEquals(3, mementos.size());

        /* debug
        mementos
        .forEach(memento->{
            System.err.printf("id: %s%n", choiceProvider.getIdValue(memento));
            System.err.printf("title (un-translated):  %s%n", memento.getTitle());
            System.err.printf("displayValue: %s%n", choiceProvider.getDisplayValue(memento));
        });*/

        var asIds = mementos.map(choiceProvider::getIdValue);

        var recoveredMementos = Can.ofCollection(choiceProvider.toChoices(asIds.toList()));

        var recoveredChoices = recoveredMementos
                .map(mmc.getObjectManager()::demementify);

        var recoveredChoiceValues = recoveredChoices
                .map(ManagedObject::getPojo);

        assertEquals(choiceValues, recoveredChoiceValues);

    }

}
