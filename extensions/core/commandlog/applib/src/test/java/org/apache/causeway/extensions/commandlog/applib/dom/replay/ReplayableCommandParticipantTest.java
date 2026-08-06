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

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.bookmark.BookmarkService;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntry;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntryRepository;
import org.apache.causeway.extensions.commandlog.applib.dom.ReplayState;
import org.apache.causeway.extensions.commandlog.applib.dom.replay.ReplayableCommandParticipant.Role;
import org.apache.causeway.extensions.commandlog.applib.spi.CommandReplayMappingListener;
import org.apache.causeway.schema.cmd.v2.ActionDto;
import org.apache.causeway.schema.cmd.v2.CommandDto;
import org.apache.causeway.schema.cmd.v2.ParamDto;
import org.apache.causeway.schema.cmd.v2.ParamsDto;
import org.apache.causeway.schema.common.v2.OidDto;
import org.apache.causeway.schema.common.v2.OidsDto;
import org.apache.causeway.schema.common.v2.ValueType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReplayableCommandParticipantTest {

    @Test
    void derivesTargetReferenceParameterAndResultButNotScalarParameter() {
        var fixture = fixture(ReplayState.PENDING, null);

        var participants = fixture.command().getParticipants();

        assertThat(fixture.command().getHasResult()).isTrue();
        assertThat(participants).extracting(ReplayableCommandParticipant::getRole)
                .containsExactly(Role.TARGET, Role.PARAMETER, Role.RESULT);
        assertThat(participants.get(1).getParameterName()).isEqualTo("customer");
        assertThat(participants).extracting(ReplayableCommandParticipant::getRecordedBookmark)
                .containsExactly(fixture.target(), fixture.parameter(), fixture.result());
        verify(fixture.bookmarkService(), never()).lookup(fixture.result());
    }

    @Test
    void mappedTargetAndParameterAppearWhilePendingButUnmappedParticipantsWaitForSuccess() {
        var mappedTarget = bookmark("Customer", "2");
        var fixture = fixture(ReplayState.PENDING, mapping(recorded ->
                recorded.equals(bookmark("Order", "1")) ? Optional.of(mappedTarget) : Optional.empty()));

        var participants = fixture.command().getParticipants();

        assertThat(participant(participants, Role.TARGET).getActualBookmark()).isEqualTo(mappedTarget);
        assertThat(participant(participants, Role.PARAMETER).getActualBookmark()).isNull();
        assertThat(participant(participants, Role.RESULT).getActualBookmark()).isNull();
    }

    @Test
    void successfulReplayUsesMappingAndRecordedBookmarkFallback() {
        var mappedResult = bookmark("Invoice", "2");
        var fixture = fixture(ReplayState.OK, mapping(recorded ->
                recorded.equals(bookmark("Invoice", "1")) ? Optional.of(mappedResult) : Optional.empty()));

        var participants = fixture.command().getParticipants();

        assertThat(participant(participants, Role.TARGET).getActualBookmark()).isEqualTo(fixture.target());
        assertThat(participant(participants, Role.PARAMETER).getActualBookmark()).isEqualTo(fixture.parameter());
        assertThat(participant(participants, Role.RESULT).getActualBookmark()).isEqualTo(mappedResult);
    }

    @Test
    void roleSpecificObjectLinksResolveActualBookmarkBestEffort() {
        var mappedTarget = bookmark("Order", "2");
        var targetObject = new Object();
        var fixture = fixture(ReplayState.PENDING, mapping(recorded ->
                recorded.equals(bookmark("Order", "1")) ? Optional.of(mappedTarget) : Optional.empty()));
        when(fixture.bookmarkService().lookup(mappedTarget)).thenReturn(Optional.of(targetObject));

        var target = participant(fixture.command().getParticipants(), Role.TARGET);
        var parameter = participant(fixture.command().getParticipants(), Role.PARAMETER);

        assertThat(target.getTarget()).isSameAs(targetObject);
        assertThat(target.getArgument()).isNull();
        assertThat(target.getResult()).isNull();
        assertThat(target.hideTarget()).isFalse();
        assertThat(target.hideArgument()).isTrue();
        assertThat(parameter.getArgument()).isNull();
        assertThat(parameter.getRecordedBookmark()).isEqualTo(fixture.parameter());
    }

    @Test
    void readableMementosRehydrateFromCurrentCommandAndMappingState() {
        var actualParameter = bookmark("Customer", "2");
        var mapping = new MutableMapping();
        var fixture = fixture(ReplayState.OK, mapping);
        var parameter = participant(fixture.command().getParticipants(), Role.PARAMETER);
        var expectedMemento = fixture.interactionId() + "--parameter--customer";

        assertThat(parameter.viewModelMemento()).isEqualTo(expectedMemento);
        assertThat(parameter.viewModelMemento()).doesNotContain(fixture.parameter().stringify());

        mapping.actual = actualParameter;
        var rehydrated = new ReplayableCommandParticipant(expectedMemento, fixture.context());

        assertThat(rehydrated.getReplayableCommand().interactionId()).isEqualTo(fixture.interactionId());
        assertThat(rehydrated.getRecordedBookmark()).isEqualTo(fixture.parameter());
        assertThat(rehydrated.getActualBookmark()).isEqualTo(actualParameter);
        assertThat(rehydrated.title()).contains("PARAMETER", "customer", "→");
    }

    @Test
    void reportsNoResultWhenEntryHasNoRecordedResult() {
        var fixture = fixture(ReplayState.PENDING, null);
        when(fixture.entry().getResult()).thenReturn(null);

        assertThat(fixture.command().getHasResult()).isFalse();
        assertThat(fixture.command().getParticipants())
                .extracting(ReplayableCommandParticipant::getRole)
                .doesNotContain(Role.RESULT);
    }

    private static ReplayableCommandParticipant participant(
            final List<ReplayableCommandParticipant> participants,
            final Role role) {
        return participants.stream()
                .filter(candidate -> candidate.getRole() == role)
                .findFirst()
                .orElseThrow();
    }

    private static Fixture fixture(
            final ReplayState replayState,
            final CommandReplayMappingListener listener) {
        var interactionId = UUID.randomUUID();
        var target = bookmark("Order", "1");
        var parameter = bookmark("Customer", "1");
        var result = bookmark("Invoice", "1");
        var dto = commandDto(target, parameter);
        var entry = mock(CommandLogEntry.class);
        when(entry.getInteractionId()).thenReturn(interactionId);
        when(entry.getCommandDto()).thenReturn(dto);
        when(entry.getReplayState()).thenReturn(replayState);
        when(entry.getResult()).thenReturn(result);
        var repository = mock(CommandLogEntryRepository.class);
        when(repository.findByInteractionId(interactionId)).thenReturn(Optional.of(entry));
        var bookmarkService = mock(BookmarkService.class);
        var remappingService = new ResultRemappingService(
                listener != null ? List.of(listener) : List.of());
        var context = new ReplayContext(
                null, null, null, repository, null, null, remappingService, bookmarkService, null);
        return new Fixture(
                interactionId,
                new ReplayableCommand(interactionId, context),
                context,
                entry,
                bookmarkService,
                target,
                parameter,
                result);
    }

    private static CommandDto commandDto(final Bookmark target, final Bookmark reference) {
        var dto = new CommandDto();
        var targets = new OidsDto();
        targets.getOid().add(oid(target));
        dto.setTargets(targets);
        var action = new ActionDto();
        action.setLogicalMemberIdentifier("demo.Order#place");
        var params = new ParamsDto();
        var referenceParam = new ParamDto();
        referenceParam.setName("customer");
        referenceParam.setType(ValueType.REFERENCE);
        referenceParam.setReference(oid(reference));
        params.getParameter().add(referenceParam);
        var scalarParam = new ParamDto();
        scalarParam.setName("quantity");
        scalarParam.setType(ValueType.INT);
        params.getParameter().add(scalarParam);
        action.setParameters(params);
        dto.setMember(action);
        return dto;
    }

    private static OidDto oid(final Bookmark bookmark) {
        var oid = new OidDto();
        oid.setType(bookmark.logicalTypeName());
        oid.setId(bookmark.identifier());
        return oid;
    }

    private static Bookmark bookmark(final String type, final String id) {
        return Bookmark.forLogicalTypeNameAndIdentifier("demo." + type, id);
    }

    private static CommandReplayMappingListener mapping(
            final Function<Bookmark, Optional<Bookmark>> lookup) {
        return new CommandReplayMappingListener() {
            @Override
            public Optional<Bookmark> lookup(final Bookmark recordedBookmark) {
                return lookup.apply(recordedBookmark);
            }
        };
    }

    private record Fixture(
            UUID interactionId,
            ReplayableCommand command,
            ReplayContext context,
            CommandLogEntry entry,
            BookmarkService bookmarkService,
            Bookmark target,
            Bookmark parameter,
            Bookmark result) {
    }

    private static final class MutableMapping implements CommandReplayMappingListener {
        private Bookmark actual;

        @Override
        public Optional<Bookmark> lookup(final Bookmark recordedBookmark) {
            return actual != null && recordedBookmark.equals(bookmark("Customer", "1"))
                    ? Optional.of(actual)
                    : Optional.empty();
        }
    }
}
