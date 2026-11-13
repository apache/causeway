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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.applib.id.LogicalType;
import org.apache.causeway.applib.services.metamodel.BeanSort;
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

/**
 * Focused coverage for the synthetic parented-collection-navigation replay argument reconstruction
 * (MA-5): recorded DTO parameters are matched to the current action parameters by stable id, then by
 * friendly name, and any current parameter absent from the DTO is padded with an empty value — so a
 * command recorded against an earlier column/filter set still replays.
 */
class CommandExecutorSyntheticNavigationReplayTest {

    @Test
    void args_matchByIdThenFriendlyName_andPadMissing_inCurrentParameterOrder() {

        // given an action whose current parameters (in current metamodel order) are name, checkbox, sequence
        var stringSpec = mock(ObjectSpecification.class);
        var boolSpec = mock(ObjectSpecification.class);
        var intSpec = mock(ObjectSpecification.class);

        var nameParam = actionParameter("name", "Name", stringSpec);
        var checkboxParam = actionParameter("checkbox", "Checkbox", boolSpec);
        var sequenceParam = actionParameter("sequence", "Sequence", intSpec);

        var action = mock(ObjectAction.class);
        when(action.getParameters()).thenReturn(Can.of(nameParam, checkboxParam, sequenceParam));

        // and a command DTO recorded against an earlier metamodel:
        //  - in a different order,
        //  - with "name" recorded under its friendly name "Name" (so id lookup misses, friendly-name hits),
        //  - and with no "sequence" parameter at all (a filter column that did not exist yet).
        var dtoCheckbox = paramDto("checkbox");
        var dtoName = paramDto("Name");
        var actionDto = actionDto(dtoCheckbox, dtoName);

        var marshaller = mock(SchemaValueMarshaller.class);
        when(marshaller.actionIdentifier(actionDto)).thenReturn(
                Identifier.actionIdentifier(LogicalType.eager(Object.class, "test.Owner"),
                        "__causeway_navigate_to_one_of_items"));
        var nameValue = value("some name");
        var checkboxValue = value(Boolean.TRUE);
        when(marshaller.recoverParameterFrom(any(Identifier.class), eq(dtoName))).thenReturn(nameValue);
        when(marshaller.recoverParameterFrom(any(Identifier.class), eq(dtoCheckbox))).thenReturn(checkboxValue);

        // when
        var args = CommandExecutorServiceDefault
                .argAdaptersForParentedCollectionNavigation(actionDto, action, marshaller);

        // then - one argument per current parameter, in current parameter order
        assertThat(args.size()).isEqualTo(3);
        assertThat(args.getElseFail(0)).isSameAs(nameValue);        // matched by friendly name "Name"
        assertThat(args.getElseFail(1)).isSameAs(checkboxValue);    // matched by id "checkbox"
        assertThat(ManagedObjects.isNullOrUnspecifiedOrEmpty(args.getElseFail(2))).isTrue(); // padded (missing)

        // and the marshaller was only asked to recover the two matched parameters (the missing one was padded)
        verify(marshaller, times(2)).recoverParameterFrom(any(Identifier.class), any(ParamDto.class));
    }

    // -- HELPER

    private static ObjectActionParameter actionParameter(
            final String id, final String friendlyName, final ObjectSpecification elementType) {
        var parameter = mock(ObjectActionParameter.class);
        when(parameter.getId()).thenReturn(id);
        when(parameter.getCanonicalFriendlyName()).thenReturn(friendlyName);
        when(parameter.getElementType()).thenReturn(elementType);
        return parameter;
    }

    private static ParamDto paramDto(final String name) {
        var dto = new ParamDto();
        dto.setName(name);
        return dto;
    }

    private static ActionDto actionDto(final ParamDto... params) {
        var actionDto = new ActionDto();
        var paramsDto = new ParamsDto();
        for (var param : params) {
            paramsDto.getParameter().add(param);
        }
        actionDto.setParameters(paramsDto);
        return actionDto;
    }

    private static ManagedObject value(final Object pojo) {
        var spec = mock(ObjectSpecification.class);
        var specLoader = mock(SpecificationLoader.class);
        when(spec.isValue()).thenReturn(true);
        when(spec.beanSort()).thenReturn(BeanSort.VALUE);
        when(spec.getSpecificationLoader()).thenReturn(specLoader);
        when(specLoader.specForType(pojo.getClass())).thenReturn(Optional.of(spec));
        return ManagedObject.value(spec, pojo);
    }
}
