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

import org.apache.isis.core.metamodel.interactions.managed.ActionInteraction;
import org.apache.isis.core.metamodel.interactions.managed.ParameterNegotiationModel;

public class SimulatedUiSubmit extends HasActionValidation {

    private Runnable doSubmit; // might require a weak reference when actually implementing
    
    public void bind(final ActionInteraction interaction, final ParameterNegotiationModel pendingArgs) {
        validationMessage.bind(pendingArgs.getObservableActionValidation());
        doSubmit = ()->interaction.submit(pendingArgs);
    }
    
    public void simulateSubmit() {
        doSubmit.run();
    }

    
}
