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

import javax.inject.Inject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.applib.services.command.Command;
import org.apache.causeway.applib.value.semantics.ValueSemanticsProvider;
import org.apache.causeway.core.metamodel.services.schema.SchemaValueMarshaller;
import org.apache.causeway.schema.cmd.v2.ActionDto;
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
    
    @Inject private SchemaValueMarshaller valueMarshaller;
    
    @Override
    public void testCommandWithNonEmptyArg(
            final ValueSemanticsProvider.Context context,
            final Command command) {

        assertNotNull(command.getCommandDto());
        
        //debug
        //System.err.printf("CommandDto %s %s%n", name,
        //      CommandDtoUtils.dtoMapper().toString(command.getCommandDto()));
        
        var actionDto = (ActionDto)command.getCommandDto().getMember();
        
        assertNotNull(actionDto.getParameters());
        assertNotNull(actionDto.getParameters().getParameter());
        assertEquals(1, actionDto.getParameters().getParameter().size()); // we are testing a single arg action 
        
        var parameterRecordedDto = actionDto.getParameters().getParameter().get(0);
        assertNotNull(parameterRecordedDto);
        
        final Identifier paramId = context.getFeatureIdentifier();
        var parameterRecorded = valueMarshaller.recoverParameterFrom(paramId, parameterRecordedDto);
        assertNotNull(parameterRecorded);

        assertEquals(valueType, parameterRecorded.getSpecification().getCorrespondingClass(), ()->
            String.format("command value parsing type mismatch '%s'",
                    _Utils.valueDtoToXml(parameterRecordedDto)));

        tester.assertValueEquals(example.getUpdateValue(), parameterRecorded.getPojo(), "command failed");

    }
    
    @Override
    public void testCommandWithEmptyArg(
            final ValueSemanticsProvider.Context context,
            final Command command) {

        assertNotNull(command.getCommandDto());
        
        //debug
//        System.err.printf("CommandDto %s %s%n", name,
//              CommandDtoUtils.dtoMapper().toString(command.getCommandDto()));
        
        var actionDto = (ActionDto)command.getCommandDto().getMember();
        
        assertNotNull(actionDto.getParameters());
        assertNotNull(actionDto.getParameters().getParameter());
        assertEquals(1, actionDto.getParameters().getParameter().size()); // we are testing a single arg action 
        
        var parameterRecordedDto = actionDto.getParameters().getParameter().get(0);
        assertNotNull(parameterRecordedDto);
        
        final Identifier paramId = context.getFeatureIdentifier();
        var parameterRecorded = valueMarshaller.recoverParameterFrom(paramId, parameterRecordedDto);
        assertNotNull(parameterRecorded);

        assertEquals(valueType, parameterRecorded.getSpecification().getCorrespondingClass(), ()->
            String.format("command value parsing type mismatch '%s'",
                    _Utils.valueDtoToXml(parameterRecordedDto)));

        assertNull(parameterRecorded.getPojo());
    }
    
}
