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
package org.apache.causeway.core.runtimeservices.executor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import javax.inject.Provider;

import org.junit.jupiter.api.Test;

import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.command.Command;
import org.apache.causeway.applib.services.command.CommandRecordingSuppressed;
import org.apache.causeway.applib.services.repository.EntityState;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.metamodel.interactions.InteractionHead;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.PackedManagedObject;
import org.apache.causeway.core.metamodel.services.publishing.CommandPublisher;
import org.apache.causeway.core.metamodel.spec.feature.ObjectMember;

class MemberExecutorServiceDefaultTest {

    @Test
    void prepare_command_for_publishing_does_nothing_for_suppressed_target() {
        @SuppressWarnings("unchecked")
        Provider<CommandPublisher> commandPublisherProvider = mock(Provider.class);
        var service = newService(commandPublisherProvider);
        var target = mock(ManagedObject.class);
        when(target.getPojo()).thenReturn(new SuppressedTarget());
        var interactionHead = mock(InteractionHead.class);
        when(interactionHead.getTarget()).thenReturn(target);

        service.prepareCommandForPublishing(
                mock(Command.class),
                interactionHead,
                mock(ObjectMember.class),
                mock(ObjectMember.class));

        verifyNoInteractions(commandPublisherProvider);
    }

    @Test
    void prepare_command_for_publishing_does_nothing_for_suppressed_owner() {
        @SuppressWarnings("unchecked")
        Provider<CommandPublisher> commandPublisherProvider = mock(Provider.class);
        var service = newService(commandPublisherProvider);
        var owner = mock(ManagedObject.class);
        var target = mock(ManagedObject.class);
        when(owner.getPojo()).thenReturn(new SuppressedTarget());
        when(target.getPojo()).thenReturn(new Object());
        var interactionHead = mock(InteractionHead.class);
        when(interactionHead.getOwner()).thenReturn(owner);
        when(interactionHead.getTarget()).thenReturn(target);

        service.prepareCommandForPublishing(
                mock(Command.class),
                interactionHead,
                mock(ObjectMember.class),
                mock(ObjectMember.class));

        verifyNoInteractions(commandPublisherProvider);
    }

    @Test
    void set_command_result_records_scalar_bookmarkable_entity() {
        var service = newService();
        var command = newCommand();
        var bookmark = bookmark("1");

        service.setCommandResultIfEntity(command, entityAdapter(bookmark));

        assertThat(command.getResult()).isEqualTo(bookmark);
    }

    @Test
    void set_command_result_records_singleton_packed_bookmarkable_entity() {
        var service = newService();
        var command = newCommand();
        var bookmark = bookmark("1");

        service.setCommandResultIfEntity(command, packed(entityAdapter(bookmark)));

        assertThat(command.getResult()).isEqualTo(bookmark);
    }

    @Test
    void set_command_result_ignores_empty_packed_result() {
        var service = newService();
        var command = newCommand();

        service.setCommandResultIfEntity(command, packed());

        assertThat(command.getResult()).isNull();
    }

    @Test
    void set_command_result_ignores_multi_object_packed_result() {
        var service = newService();
        var command = newCommand();

        service.setCommandResultIfEntity(command, packed(entityAdapter(bookmark("1")), entityAdapter(bookmark("2"))));

        assertThat(command.getResult()).isNull();
    }

    @Test
    void set_command_result_ignores_singleton_packed_non_bookmarkable_result() {
        var service = newService();
        var command = newCommand();

        service.setCommandResultIfEntity(command, packed(valueAdapter()));

        assertThat(command.getResult()).isNull();
    }

    @Test
    void set_command_result_does_not_overwrite_existing_result() {
        var service = newService();
        var command = newCommand();
        var existing = bookmark("existing");
        command.updater().setResult(org.apache.causeway.commons.functional.Try.success(existing));

        service.setCommandResultIfEntity(command, packed(entityAdapter(bookmark("new"))));

        assertThat(command.getResult()).isEqualTo(existing);
    }

    private MemberExecutorServiceDefault newService() {
        @SuppressWarnings("unchecked")
        Provider<CommandPublisher> commandPublisherProvider = mock(Provider.class);
        return newService(commandPublisherProvider);
    }

    private Command newCommand() {
        return new Command(UUID.randomUUID());
    }

    private Bookmark bookmark(final String identifier) {
        return Bookmark.forLogicalTypeNameAndIdentifier("demo.Customer", identifier);
    }

    private ManagedObject entityAdapter(final Bookmark bookmark) {
        var adapter = mock(ManagedObject.class);
        when(adapter.getSpecialization()).thenReturn(ManagedObject.Specialization.ENTITY);
        when(adapter.getPojo()).thenReturn(new Object());
        when(adapter.getEntityState()).thenReturn(EntityState.ATTACHED);
        when(adapter.getBookmark()).thenReturn(Optional.of(bookmark));
        return adapter;
    }

    private ManagedObject valueAdapter() {
        var adapter = mock(ManagedObject.class);
        when(adapter.getSpecialization()).thenReturn(ManagedObject.Specialization.VALUE);
        when(adapter.getPojo()).thenReturn("not-bookmarkable");
        when(adapter.getEntityState()).thenReturn(EntityState.NOT_PERSISTABLE);
        return adapter;
    }

    private PackedManagedObject packed(final ManagedObject... elements) {
        var packed = mock(PackedManagedObject.class);
        when(packed.getSpecialization()).thenReturn(ManagedObject.Specialization.PACKED);
        when(packed.unpack()).thenReturn(Can.ofArray(elements));
        return packed;
    }

    private MemberExecutorServiceDefault newService(final Provider<CommandPublisher> commandPublisherProvider) {
        return new MemberExecutorServiceDefault(
                null,
                mock(CausewayConfiguration.class),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                commandPublisherProvider);
    }

    static class SuppressedTarget implements CommandRecordingSuppressed {
    }
}
