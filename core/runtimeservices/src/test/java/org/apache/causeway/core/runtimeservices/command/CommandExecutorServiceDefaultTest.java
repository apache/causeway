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
package org.apache.causeway.core.runtimeservices.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.applib.id.LogicalType;
import org.apache.causeway.applib.services.bookmark.BookmarkService;
import org.apache.causeway.applib.services.clock.ClockService;
import org.apache.causeway.applib.services.iactnlayer.InteractionLayerTracker;
import org.apache.causeway.applib.services.metamodel.MetaModelService;
import org.apache.causeway.applib.services.sudo.SudoService;
import org.apache.causeway.applib.services.xactn.TransactionService;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.ManagedObjects;
import org.apache.causeway.core.metamodel.services.schema.SchemaValueMarshaller;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;
import org.apache.causeway.schema.cmd.v2.ActionDto;
import org.apache.causeway.schema.cmd.v2.ParamDto;
import org.apache.causeway.schema.cmd.v2.ParamsDto;

import lombok.val;

class CommandExecutorServiceDefaultTest {

    @Test
    void pads_and_reorders_legacy_parented_collection_navigation_arguments_by_parameter_id() {
        val fixtures = new Fixtures();
        val service = newService(fixtures.valueMarshaller);
        val action = action(
                parameter("name", String.class, 0),
                parameter("sequence", Integer.class, 1),
                parameter("checkbox", Boolean.class, 2),
                parameter("boundedReference", Object.class, 3),
                parameter("choicesReference", Object.class, 4),
                parameter("autocompleteReference", Object.class, 5));
        val nameDto = paramDto("name");
        val checkboxDto = paramDto("checkbox");
        val sequenceDto = paramDto("sequence");
        val actionDto = actionDto(checkboxDto, nameDto, sequenceDto);
        val nameAdapter = mock(ManagedObject.class);
        val checkboxAdapter = mock(ManagedObject.class);
        val sequenceAdapter = mock(ManagedObject.class);
        when(fixtures.valueMarshaller.recoverParameterFrom(eq(action.getParameters().getElseFail(0).getFeatureIdentifier()), eq(nameDto)))
                .thenReturn(nameAdapter);
        when(fixtures.valueMarshaller.recoverParameterFrom(eq(action.getParameters().getElseFail(1).getFeatureIdentifier()), eq(sequenceDto)))
                .thenReturn(sequenceAdapter);
        when(fixtures.valueMarshaller.recoverParameterFrom(eq(action.getParameters().getElseFail(2).getFeatureIdentifier()), eq(checkboxDto)))
                .thenReturn(checkboxAdapter);

        val argAdapters = service.argAdaptersFor(actionDto, action);

        assertThat(argAdapters.size()).isEqualTo(6);
        assertThat(argAdapters.getElseFail(0)).isSameAs(nameAdapter);
        assertThat(argAdapters.getElseFail(1)).isSameAs(sequenceAdapter);
        assertThat(argAdapters.getElseFail(2)).isSameAs(checkboxAdapter);
        assertThat(ManagedObjects.isNullOrUnspecifiedOrEmpty(argAdapters.getElseFail(3))).isTrue();
        assertThat(ManagedObjects.isNullOrUnspecifiedOrEmpty(argAdapters.getElseFail(4))).isTrue();
        assertThat(ManagedObjects.isNullOrUnspecifiedOrEmpty(argAdapters.getElseFail(5))).isTrue();
    }

    private CommandExecutorServiceDefault newService(final SchemaValueMarshaller valueMarshaller) {
        return new CommandExecutorServiceDefault(
                mock(BookmarkService.class),
                mock(SudoService.class),
                mock(ClockService.class),
                mock(TransactionService.class),
                mock(InteractionLayerTracker.class),
                valueMarshaller,
                mock(MetaModelService.class),
                mock(SpecificationLoader.class));
    }

    private ObjectAction action(final ObjectActionParameter... parameters) {
        val action = mock(ObjectAction.class);
        when(action.getId()).thenReturn("__causeway_navigate_to_one_of_items");
        when(action.getParameters()).thenReturn(Can.ofArray(parameters));
        return action;
    }

    private ObjectActionParameter parameter(
            final String id,
            final Class<?> type,
            final int index) {
        val parameter = mock(ObjectActionParameter.class);
        val elementType = mock(ObjectSpecification.class);
        val featureIdentifier = Identifier.actionIdentifier(
                LogicalType.fqcn(CommandExecutorServiceDefaultTest.class),
                "navigate",
                new Class<?>[] { String.class, Integer.class, Boolean.class, Object.class, Object.class, Object.class })
                .withParameterIndex(index);
        when(elementType.getCorrespondingClass()).thenAnswer(__ -> type);
        when(parameter.getId()).thenReturn(id);
        when(parameter.getCanonicalFriendlyName()).thenReturn(id);
        when(parameter.getFeatureIdentifier()).thenReturn(featureIdentifier);
        when(parameter.getElementType()).thenReturn(elementType);
        return parameter;
    }

    private ActionDto actionDto(final ParamDto... paramDtos) {
        val actionDto = new ActionDto();
        val paramsDto = new ParamsDto();
        paramsDto.getParameter().addAll(java.util.List.of(paramDtos));
        actionDto.setParameters(paramsDto);
        return actionDto;
    }

    private ParamDto paramDto(final String name) {
        val paramDto = new ParamDto();
        paramDto.setName(name);
        return paramDto;
    }

    private static class Fixtures {
        private final SchemaValueMarshaller valueMarshaller = mock(SchemaValueMarshaller.class);
    }

}
