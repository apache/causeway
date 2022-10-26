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

import java.util.function.Supplier;

import org.apache.causeway.commons.functional.Railway;
import org.apache.causeway.core.metamodel.interactions.managed.ActionInteraction;
import org.apache.causeway.core.metamodel.interactions.managed.InteractionVeto;
import org.apache.causeway.core.metamodel.interactions.managed.ParameterNegotiationModel;
import org.apache.causeway.core.metamodel.object.ManagedObject;

import lombok.Getter;

public class SimulatedUiSubmit extends HasActionValidation {

    // might require a weak reference when actually implementing
    private Supplier<Railway<InteractionVeto, ManagedObject>> doSubmit;
    @Getter private Railway<InteractionVeto, ManagedObject> result;

    public void bind(final ActionInteraction interaction, final ParameterNegotiationModel pendingArgs) {
        super.bind(pendingArgs);
        doSubmit = ()->interaction.invokeWith(pendingArgs);
    }

    public void simulateSubmit() {
        result = doSubmit.get();
    }

}
