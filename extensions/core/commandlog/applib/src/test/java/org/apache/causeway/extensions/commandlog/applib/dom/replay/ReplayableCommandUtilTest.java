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
package org.apache.causeway.extensions.commandlog.applib.dom.replay;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntry;
import org.apache.causeway.schema.cmd.v2.ActionDto;
import org.apache.causeway.schema.cmd.v2.CommandDto;
import org.apache.causeway.schema.cmd.v2.PropertyDto;
import org.apache.causeway.schema.common.v2.InteractionType;

class ReplayableCommandUtilTest {

    private static final Bookmark RESULT = Bookmark.forLogicalTypeNameAndIdentifier("demo.Customer", "1");

    @Test
    void state_changing_property_command_remains_replayable_without_result() {
        final var entry = propertyEntry(null);

        final var replayable = ReplayableCommand.Util.isDoOp(entry, safeActionSpecificationLoader());

        assertThat(replayable).isTrue();
    }

    @Test
    void safe_action_with_single_result_is_replayable() {
        final var entry = actionEntry(RESULT);

        final var replayable = ReplayableCommand.Util.isDoOp(entry, safeActionSpecificationLoader());

        assertThat(replayable).isTrue();
    }

    @Test
    void safe_action_without_result_is_not_replayable_and_is_not_mutated() {
        final var entry = actionEntry(null);

        final var replayable = ReplayableCommand.Util.isDoOp(entry, safeActionSpecificationLoader());

        assertThat(replayable).isFalse();
        verify(entry, never()).setReplayState(any());
        verify(entry, never()).setResult(any());
    }

    @Test
    void non_safe_action_without_result_remains_replayable() {
        final var entry = actionEntry(null);

        final var replayable = ReplayableCommand.Util.isDoOp(entry, nonSafeActionSpecificationLoader());

        assertThat(replayable).isTrue();
    }

    @Test
    void unresolved_action_semantics_defaults_to_replayable() {
        final var entry = actionEntry(null);
        final var specificationLoader = mock(SpecificationLoader.class);

        final var replayable = ReplayableCommand.Util.isDoOp(entry, specificationLoader);

        assertThat(replayable).isTrue();
    }

    private static CommandLogEntry actionEntry(final Bookmark result) {
        final var actionDto = new ActionDto();
        actionDto.setInteractionType(InteractionType.ACTION_INVOCATION);
        actionDto.setLogicalMemberIdentifier("demo.Customer#find");
        return entry(actionDto, result);
    }

    private static CommandLogEntry propertyEntry(final Bookmark result) {
        final var propertyDto = new PropertyDto();
        propertyDto.setInteractionType(InteractionType.PROPERTY_EDIT);
        propertyDto.setLogicalMemberIdentifier("demo.Customer#name");
        return entry(propertyDto, result);
    }

    private static CommandLogEntry entry(
            final org.apache.causeway.schema.cmd.v2.MemberDto memberDto,
            final Bookmark result) {
        final var commandDto = new CommandDto();
        commandDto.setMember(memberDto);
        final var entry = mock(CommandLogEntry.class);
        when(entry.getCommandDto()).thenReturn(commandDto);
        when(entry.getLogicalMemberIdentifier()).thenReturn(memberDto.getLogicalMemberIdentifier());
        when(entry.getResult()).thenReturn(result);
        return entry;
    }

    private static SpecificationLoader safeActionSpecificationLoader() {
        return specificationLoaderWithActionSemantics(SemanticsOf.SAFE);
    }

    private static SpecificationLoader nonSafeActionSpecificationLoader() {
        return specificationLoaderWithActionSemantics(SemanticsOf.NON_IDEMPOTENT);
    }

    private static SpecificationLoader specificationLoaderWithActionSemantics(final SemanticsOf semantics) {
        final var specificationLoader = mock(SpecificationLoader.class);
        final var objectSpecification = mock(ObjectSpecification.class);
        final var objectAction = mock(ObjectAction.class);
        when(objectSpecification.getCorrespondingClass()).thenReturn((Class) Customer.class);
        when(objectAction.getSemantics()).thenReturn(semantics);
        when(specificationLoader.specForLogicalTypeNameElseFail("demo.Customer")).thenReturn(objectSpecification);
        when(specificationLoader.loadFeature(any(Identifier.class))).thenReturn(Optional.of(objectAction));
        return specificationLoader;
    }

    private static class Customer {
    }
}
