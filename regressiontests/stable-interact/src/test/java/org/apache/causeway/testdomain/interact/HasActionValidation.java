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
import org.apache.causeway.commons.internal.binding._Bindables;
import org.apache.causeway.core.metamodel.interactions.managed.ParameterNegotiationModel;

import lombok.Getter;

abstract class HasActionValidation {

    protected final Bindable<String> validationMessage = _Bindables.empty();
    @Getter private final LongAdder validationUpdateEventCount = new LongAdder();
    
    public void bind(ParameterNegotiationModel pendingArgs) {
        validationMessage.bind(pendingArgs.getObservableActionValidation());
        validationMessage.addListener((e,o,n)->{
            validationUpdateEventCount.increment();
        });
    }

    public String getValidationMessage() {
        return validationMessage.getValue(); 
    }
    
}
