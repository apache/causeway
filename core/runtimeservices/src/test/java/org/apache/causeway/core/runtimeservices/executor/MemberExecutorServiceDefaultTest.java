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

import java.util.Optional;

import jakarta.inject.Provider;

import org.junit.jupiter.api.Test;

import org.apache.causeway.applib.services.command.Command;
import org.apache.causeway.applib.services.command.CommandRecordingSuppressed;
import org.apache.causeway.applib.services.metamodel.BeanSort;
import org.apache.causeway.core.config.observation.CausewayObservationIntegration;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.interactions.InteractionHead;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.services.publishing.CommandPublisher;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectMember;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class MemberExecutorServiceDefaultTest {

    @Test
    void suppressedTargetBypassesCommandPreparation() {
        var commandPublisherProvider = commandPublisherProvider();
        var service = newService(commandPublisherProvider);
        var interactionHead = interactionHead(new Object(), new SuppressedTarget());
        var command = mock(Command.class);
        var objectMember = mock(ObjectMember.class);
        var facetHolder = mock(FacetHolder.class);

        service.prepareCommandForPublishing(command, interactionHead, objectMember, facetHolder);

        verifyNoInteractions(commandPublisherProvider, command, objectMember, facetHolder);
    }

    @Test
    void suppressedOwnerBypassesCommandPreparation() {
        var commandPublisherProvider = commandPublisherProvider();
        var service = newService(commandPublisherProvider);
        var interactionHead = interactionHead(new SuppressedTarget(), new Object());
        var command = mock(Command.class);
        var objectMember = mock(ObjectMember.class);
        var facetHolder = mock(FacetHolder.class);

        service.prepareCommandForPublishing(command, interactionHead, objectMember, facetHolder);

        verifyNoInteractions(commandPublisherProvider, command, objectMember, facetHolder);
    }

    @Test
    void unmarkedTargetRetainsNormalPublisherNotification() {
        var commandPublisher = mock(CommandPublisher.class);
        var commandPublisherProvider = commandPublisherProvider();
        when(commandPublisherProvider.get()).thenReturn(commandPublisher);
        var service = newService(commandPublisherProvider);
        var interactionHead = interactionHead(new Object(), new Object());
        var command = mock(Command.class);

        service.prepareCommandForPublishing(
            command,
            interactionHead,
            mock(ObjectMember.class),
            mock(FacetHolder.class));

        verify(commandPublisher).ready(command);
    }

    private InteractionHead interactionHead(final Object ownerPojo, final Object targetPojo) {
        var owner = managedObject(ownerPojo);
        var target = managedObject(targetPojo);
        var interactionHead = mock(InteractionHead.class);
        when(interactionHead.owner()).thenReturn(owner);
        when(interactionHead.target()).thenReturn(target);
        return interactionHead;
    }

    private ManagedObject managedObject(final Object pojo) {
        var objectSpecification = mock(ObjectSpecification.class);
        var specificationLoader = mock(SpecificationLoader.class);
        when(objectSpecification.isValue()).thenReturn(true);
        when(objectSpecification.getBeanSort()).thenReturn(BeanSort.VALUE);
        when(objectSpecification.getSpecificationLoader()).thenReturn(specificationLoader);
        when(specificationLoader.specForType(pojo.getClass())).thenReturn(Optional.of(objectSpecification));
        return ManagedObject.value(objectSpecification, pojo);
    }

    @SuppressWarnings("unchecked")
    private Provider<CommandPublisher> commandPublisherProvider() {
        return mock(Provider.class);
    }

    private MemberExecutorServiceDefault newService(final Provider<CommandPublisher> commandPublisherProvider) {
        return new MemberExecutorServiceDefault(
            null,
            null,
            null,
            null,
            null,
            null,
            commandPublisherProvider,
            mock(CausewayObservationIntegration.class));
    }

    private static final class SuppressedTarget implements CommandRecordingSuppressed {
    }
}
