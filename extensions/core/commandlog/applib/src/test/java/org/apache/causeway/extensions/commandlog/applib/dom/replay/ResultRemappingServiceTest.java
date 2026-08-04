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

import org.junit.jupiter.api.Test;

import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.extensions.commandlog.applib.spi.CommandReplayMappingListener;
import org.apache.causeway.schema.cmd.v2.ActionDto;
import org.apache.causeway.schema.cmd.v2.CommandDto;
import org.apache.causeway.schema.cmd.v2.ParamDto;
import org.apache.causeway.schema.cmd.v2.ParamsDto;
import org.apache.causeway.schema.common.v2.OidDto;
import org.apache.causeway.schema.common.v2.OidsDto;
import org.apache.causeway.schema.common.v2.ValueType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ResultRemappingServiceTest {

    @Test
    void remapsTargetAndReferenceOnIndependentCopy() {
        var listener = listenerMapping("demo.Customer", "1", "demo.Customer", "2");
        var service = new ResultRemappingService(List.of(listener));
        var recorded = actionCommand();

        var execution = service.remapped(recorded);

        assertThat(execution).isNotSameAs(recorded);
        assertThat(target(execution)).isEqualTo(bookmark("demo.Customer", "2"));
        assertThat(referenceParameter(execution)).isEqualTo(bookmark("demo.Customer", "2"));
        assertThat(stringParameter(execution)).isEqualTo("unchanged");
        assertThat(target(recorded)).isEqualTo(bookmark("demo.Customer", "1"));
        assertThat(referenceParameter(recorded)).isEqualTo(bookmark("demo.Customer", "1"));
    }

    @Test
    void noReplacementPreservesRecordedValuesOnCopy() {
        var service = new ResultRemappingService(List.of());
        var recorded = actionCommand();

        var execution = service.remapped(recorded);

        assertThat(target(execution)).isEqualTo(bookmark("demo.Customer", "1"));
        assertThat(referenceParameter(execution)).isEqualTo(bookmark("demo.Customer", "1"));
        assertThat(stringParameter(execution)).isEqualTo("unchanged");
    }

    @Test
    void lookupUsesFirstNonEmptyReplacement() {
        var first = listenerMapping("demo.Customer", "1", "demo.Customer", "2");
        var second = listenerMapping("demo.Customer", "1", "demo.Customer", "3");
        var service = new ResultRemappingService(List.of(first, second));

        assertThat(service.lookup(bookmark("demo.Customer", "1")))
                .contains(bookmark("demo.Customer", "2"));
        verify(second, never()).lookup(bookmark("demo.Customer", "1"));
    }

    @Test
    void lookupContinuesAfterListenerFailure() {
        var failing = mock(CommandReplayMappingListener.class);
        var recorded = bookmark("demo.Customer", "1");
        when(failing.lookup(recorded)).thenThrow(new IllegalStateException("lookup failed"));
        var succeeding = listenerMapping("demo.Customer", "1", "demo.Customer", "2");
        var service = new ResultRemappingService(List.of(failing, succeeding));

        assertThat(service.lookup(recorded)).contains(bookmark("demo.Customer", "2"));
    }

    @Test
    void resultNotificationIsSentToEveryListener() {
        var first = mock(CommandReplayMappingListener.class);
        var second = mock(CommandReplayMappingListener.class);
        var service = new ResultRemappingService(List.of(first, second));
        var recorded = bookmark("demo.Invoice", "1");
        var actual = bookmark("demo.Invoice", "2");
        var interactionId = UUID.randomUUID();

        service.notifyReplayResult(recorded, actual, interactionId);

        verify(first).onReplayResult(recorded, actual, interactionId);
        verify(second).onReplayResult(recorded, actual, interactionId);
    }

    @Test
    void resultNotificationFailurePropagates() {
        var listener = mock(CommandReplayMappingListener.class);
        var recorded = bookmark("demo.Invoice", "1");
        var actual = bookmark("demo.Invoice", "2");
        var interactionId = UUID.randomUUID();
        doThrow(new IllegalStateException("rejected"))
                .when(listener).onReplayResult(recorded, actual, interactionId);
        var service = new ResultRemappingService(List.of(listener));

        assertThatThrownBy(() -> service.notifyReplayResult(recorded, actual, interactionId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("rejected");
    }

    @Test
    void missingResultDoesNotNotify() {
        var listener = mock(CommandReplayMappingListener.class);
        var service = new ResultRemappingService(List.of(listener));

        service.notifyReplayResult(null, bookmark("demo.Invoice", "2"), UUID.randomUUID());
        service.notifyReplayResult(bookmark("demo.Invoice", "1"), null, UUID.randomUUID());

        verify(listener, never()).onReplayResult(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any());
    }

    private static CommandReplayMappingListener listenerMapping(
            final String recordedType,
            final String recordedId,
            final String actualType,
            final String actualId) {
        var listener = mock(CommandReplayMappingListener.class);
        when(listener.lookup(bookmark(recordedType, recordedId)))
                .thenReturn(Optional.of(bookmark(actualType, actualId)));
        return listener;
    }

    private static CommandDto actionCommand() {
        var command = new CommandDto();
        var targets = new OidsDto();
        targets.getOid().add(oid("demo.Customer", "1"));
        command.setTargets(targets);

        var action = new ActionDto();
        var parameters = new ParamsDto();
        action.setParameters(parameters);
        command.setMember(action);

        var reference = new ParamDto();
        reference.setName("customer");
        reference.setType(ValueType.REFERENCE);
        reference.setReference(oid("demo.Customer", "1"));
        parameters.getParameter().add(reference);

        var scalar = new ParamDto();
        scalar.setName("filter");
        scalar.setType(ValueType.STRING);
        scalar.setString("unchanged");
        parameters.getParameter().add(scalar);
        return command;
    }

    private static Bookmark target(final CommandDto command) {
        return Bookmark.forOidDto(command.getTargets().getOid().get(0));
    }

    private static Bookmark referenceParameter(final CommandDto command) {
        return Bookmark.forOidDto(((ActionDto) command.getMember())
                .getParameters().getParameter().get(0).getReference());
    }

    private static String stringParameter(final CommandDto command) {
        return ((ActionDto) command.getMember()).getParameters().getParameter().get(1).getString();
    }

    private static OidDto oid(final String type, final String id) {
        var oid = new OidDto();
        oid.setType(type);
        oid.setId(id);
        return oid;
    }

    private static Bookmark bookmark(final String type, final String id) {
        return Bookmark.forLogicalTypeNameAndIdentifier(type, id);
    }
}
