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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import javax.inject.Provider;

import org.junit.jupiter.api.Test;

import org.apache.causeway.applib.services.command.Command;
import org.apache.causeway.applib.services.command.CommandRecordingSuppressed;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.metamodel.interactions.InteractionHead;
import org.apache.causeway.core.metamodel.object.ManagedObject;
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
