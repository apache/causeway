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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.command.Command;
import org.apache.causeway.applib.services.command.CommandRecordingSuppressed;
import org.apache.causeway.applib.services.metamodel.BeanSort;
import org.apache.causeway.applib.services.repository.EntityState;
import org.apache.causeway.applib.services.xactn.TransactionService;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.functional.Try;
import org.apache.causeway.core.config.observation.CausewayObservationIntegration;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.interactions.InteractionHead;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.PackedManagedObject;
import org.apache.causeway.core.metamodel.services.publishing.CommandPublisher;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.ObjectMember;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;
import org.junit.jupiter.api.Test;

import jakarta.inject.Provider;

class MemberExecutorServiceDefaultTest {

    @Test
    void recordingAwareSafeActionOnSuppressedTargetBypassesCommandPreparation() {
        var commandPublisherProvider = commandPublisherProvider();
        var service = newService(commandPublisherProvider);
        var interactionHead = interactionHead(new Object(), new SuppressedTarget());
        var command = mock(Command.class);
        var objectAction = mock(ObjectAction.class);
        var facetHolder = mock(FacetHolder.class);

        service.prepareCommandForPublishing(command, interactionHead, objectAction, facetHolder);

        verifyNoInteractions(commandPublisherProvider, command, objectAction, facetHolder);
    }

    @Test
    void recordingAwarePropertyOnSuppressedOwnerBypassesCommandPreparation() {
        var commandPublisherProvider = commandPublisherProvider();
        var service = newService(commandPublisherProvider);
        var interactionHead = interactionHead(new SuppressedTarget(), new Object());
        var command = mock(Command.class);
        var property = mock(OneToOneAssociation.class);
        var facetHolder = mock(FacetHolder.class);

        service.prepareCommandForPublishing(command, interactionHead, property, facetHolder);

        verifyNoInteractions(commandPublisherProvider, command, property, facetHolder);
    }

    @Test
    void recordingAwareEligibleInteractionUsesOneNormalReadyNotification() {
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

    @Test
    void recordsDirectBookmarkableEntityResult() {
        var service = newService();
        var command = newCommand();
        var bookmark = bookmark("1");

        service.setCommandResultIfEntity(command, resultAdapter(
                ManagedObject.Specialization.ENTITY,
                EntityState.ATTACHED,
                Optional.of(bookmark)));

        assertThat(command.getResult()).isEqualTo(bookmark);
    }

    @Test
    void recordsDirectBookmarkableViewModelResult() {
        var service = newService();
        var command = newCommand();
        var bookmark = bookmark("view-model");

        service.setCommandResultIfEntity(command, resultAdapter(
                ManagedObject.Specialization.VIEWMODEL,
                EntityState.NOT_PERSISTABLE,
                Optional.of(bookmark)));

        assertThat(command.getResult()).isEqualTo(bookmark);
    }

    @Test
    void synchronizesPersistableResultWithoutIdentifierBeforeCapturingBookmark() {
        var transactionService = mock(TransactionService.class);
        var service = newService(transactionService);
        var command = newCommand();
        var bookmark = bookmark("new");

        service.setCommandResultIfEntity(command, resultAdapter(
                ManagedObject.Specialization.ENTITY,
                EntityState.ATTACHED_NO_OID,
                Optional.of(bookmark)));

        verify(transactionService).flushTransaction();
        assertThat(command.getResult()).isEqualTo(bookmark);
    }

    @Test
    void recordsSingletonPackedBookmarkableResult() {
        var service = newService();
        var command = newCommand();
        var bookmark = bookmark("1");

        service.setCommandResultIfEntity(command, packed(resultAdapter(
                ManagedObject.Specialization.ENTITY,
                EntityState.ATTACHED,
                Optional.of(bookmark))));

        assertThat(command.getResult()).isEqualTo(bookmark);
    }

    @Test
    void ignoresEmptyAndMultiplePackedResults() {
        var service = newService();
        var emptyCommand = newCommand();
        var multipleCommand = newCommand();

        service.setCommandResultIfEntity(emptyCommand, packed());
        service.setCommandResultIfEntity(multipleCommand, packed(
                resultAdapter(ManagedObject.Specialization.ENTITY, EntityState.ATTACHED, Optional.of(bookmark("1"))),
                resultAdapter(ManagedObject.Specialization.ENTITY, EntityState.ATTACHED, Optional.of(bookmark("2")))));

        assertThat(emptyCommand.getResult()).isNull();
        assertThat(multipleCommand.getResult()).isNull();
    }

    @Test
    void ignoresNullUnspecifiedAndNonBookmarkableResults() {
        var service = newService();
        var nullCommand = newCommand();
        var unspecifiedCommand = newCommand();
        var nonBookmarkableCommand = newCommand();

        service.setCommandResultIfEntity(nullCommand, null);
        service.setCommandResultIfEntity(unspecifiedCommand, ManagedObject.unspecified());
        service.setCommandResultIfEntity(nonBookmarkableCommand, resultAdapter(
                ManagedObject.Specialization.VALUE,
                EntityState.NOT_PERSISTABLE,
                Optional.empty()));

        assertThat(nullCommand.getResult()).isNull();
        assertThat(unspecifiedCommand.getResult()).isNull();
        assertThat(nonBookmarkableCommand.getResult()).isNull();
    }

    @Test
    void preservesExistingCommandResult() {
        var service = newService();
        var command = newCommand();
        var existing = bookmark("existing");
        command.updater().setResult(Try.success(existing));

        service.setCommandResultIfEntity(command, packed(resultAdapter(
                ManagedObject.Specialization.ENTITY,
                EntityState.ATTACHED,
                Optional.of(bookmark("new")))));

        assertThat(command.getResult()).isEqualTo(existing);
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
        when(objectSpecification.beanSort()).thenReturn(BeanSort.VALUE);
        when(objectSpecification.getSpecificationLoader()).thenReturn(specificationLoader);
        when(specificationLoader.specForType(pojo.getClass())).thenReturn(Optional.of(objectSpecification));
        return ManagedObject.value(objectSpecification, pojo);
    }

    private Command newCommand() {
        return new Command(UUID.randomUUID());
    }

    private Bookmark bookmark(final String identifier) {
        return Bookmark.forLogicalTypeNameAndIdentifier("demo.Customer", identifier);
    }

    private ManagedObject resultAdapter(
            final ManagedObject.Specialization specialization,
            final EntityState entityState,
            final Optional<Bookmark> bookmark) {
        var adapter = mock(concreteManagedObjectType(specialization));
        when(adapter.specialization()).thenReturn(specialization);
        when(adapter.getPojo()).thenReturn(new Object());
        when(adapter.getEntityState()).thenReturn(entityState);
        when(adapter.getBookmark()).thenReturn(bookmark);
        return adapter;
    }

    @SuppressWarnings("unchecked")
    private Class<? extends ManagedObject> concreteManagedObjectType(
            final ManagedObject.Specialization specialization) {
        var simpleName = switch (specialization) {
            case ENTITY -> "ManagedObjectEntity";
            case VIEWMODEL -> "ManagedObjectViewmodel";
            case VALUE -> "ManagedObjectValue";
            default -> throw new IllegalArgumentException("Unsupported test specialization: " + specialization);
        };
        try {
            return (Class<? extends ManagedObject>) Class.forName(
                    "org.apache.causeway.core.metamodel.object." + simpleName);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }

    private PackedManagedObject packed(final ManagedObject... elements) {
        var packed = mock(PackedManagedObject.class);
        when(packed.specialization()).thenReturn(ManagedObject.Specialization.PACKED);
        when(packed.unpack()).thenReturn(Can.ofArray(elements));
        return packed;
    }

    @SuppressWarnings("unchecked")
    private Provider<CommandPublisher> commandPublisherProvider() {
        return mock(Provider.class);
    }

    private MemberExecutorServiceDefault newService() {
        return newService((TransactionService) null);
    }

    private MemberExecutorServiceDefault newService(final TransactionService transactionService) {
        return new MemberExecutorServiceDefault(
            null,
            null,
            null,
            null,
            null,
            transactionService,
            commandPublisherProvider(),
            mock(CausewayObservationIntegration.class));
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
