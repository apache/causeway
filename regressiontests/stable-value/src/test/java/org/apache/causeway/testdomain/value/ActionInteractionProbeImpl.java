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
package org.apache.causeway.testdomain.value;

import org.apache.causeway.applib.services.command.Command;
import org.apache.causeway.applib.value.semantics.ValueSemanticsProvider.Context;
import org.apache.causeway.testdomain.model.valuetypes.ValueTypeExample;
import org.apache.causeway.testdomain.value.ValueSemanticsTester.ActionInteractionProbe;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class ActionInteractionProbeImpl<T> implements ActionInteractionProbe<T> {
    
    final @NonNull String name;
    final @NonNull Class<T> valueType;
    final @NonNull ValueTypeExample<T> example;
    final @NonNull ValueSemanticsTester<T> tester;
    
    
    @Override
    public void testCommand(Context context, Command command) {
        // TODO Auto-generated method stub
        System.err.printf("ACT testCommand %s%n", command);
    }

}

