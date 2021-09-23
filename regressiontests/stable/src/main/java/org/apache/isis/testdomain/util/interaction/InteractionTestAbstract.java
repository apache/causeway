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
package org.apache.isis.testdomain.util.interaction;

import java.util.Collections;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.springframework.lang.Nullable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.services.iactnlayer.InteractionService;
import org.apache.isis.applib.services.wrapper.WrapperFactory;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.collections._Sets;
import org.apache.isis.core.metamodel.interactions.managed.ActionInteraction;
import org.apache.isis.core.metamodel.interactions.managed.CollectionInteraction;
import org.apache.isis.core.metamodel.interactions.managed.PropertyInteraction;
import org.apache.isis.core.metamodel.objectmanager.ObjectManager;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.testdomain.publishing.subscriber.EntityPropertyChangeSubscriberForTesting;
import org.apache.isis.testdomain.util.CollectionAssertions;
import org.apache.isis.testdomain.util.kv.KVStoreForTesting;
import org.apache.isis.testing.integtestsupport.applib.IsisIntegrationTestAbstract;

import lombok.val;

public abstract class InteractionTestAbstract extends IsisIntegrationTestAbstract {

    @Inject protected ObjectManager objectManager;
    @Inject protected InteractionService interactionService;
    @Inject protected WrapperFactory wrapper;
    @Inject protected KVStoreForTesting kvStoreForTesting;
    @Inject protected DomainObjectTesterFactory testerFactory;

    protected ManagedObject newValue(final Object value) {
        return objectManager.adapt(value);
    }

    protected ManagedObject newViewmodel(final Class<?> type) {
        val viewModel = factoryService.viewModel(type);
        return objectManager.adapt(viewModel);
    }

    // -- INTERACTION STARTERS

    protected ActionInteraction startActionInteractionOn(final Class<?> type, final String actionId, final Where where) {
        val viewModel = factoryService.viewModel(type);
        val managedObject = objectManager.adapt(viewModel);
        return ActionInteraction.start(managedObject, actionId, where);
    }

    protected PropertyInteraction startPropertyInteractionOn(final Class<?> type, final String propertyId, final Where where) {
        val viewModel = factoryService.viewModel(type);
        val managedObject = objectManager.adapt(viewModel);
        return PropertyInteraction.start(managedObject, propertyId, where);
    }

    protected CollectionInteraction startCollectionInteractionOn(final Class<?> type, final String collectionId, final Where where) {
        val viewModel = factoryService.viewModel(type);
        val managedObject = objectManager.adapt(viewModel);
        return CollectionInteraction.start(managedObject, collectionId, where);
    }

    // -- SHORTCUTS

    protected Object invokeAction(final Class<?> type, final String actionId, final @Nullable List<Object> pojoArgList) {
        val managedAction = startActionInteractionOn(type, actionId, Where.OBJECT_FORMS)
                .getManagedAction().get(); // should not throw

        assertFalse(managedAction.checkVisibility().isPresent()); // is visible
        assertFalse(managedAction.checkUsability().isPresent()); // can invoke

        val args = managedAction.getInteractionHead()
                .getPopulatedParameterValues(pojoArgList);

        // spawns its own transactional boundary, or reuses an existing one if available
        val either = managedAction.invoke(args);

        assertTrue(either.isLeft()); // assert action did not throw

        val actionResultAsPojo = either.leftIfAny().getPojo();

        return actionResultAsPojo;
    }

    // -- ASSERTIONS

    protected void assertMetamodelValid() {
        val specLoader = objectManager.getMetaModelContext().getSpecificationLoader();
        assertEquals(Collections.<String>emptyList(), specLoader.getOrAssessValidationResult().getMessages());
    }

    protected void assertComponentWiseEquals(final Object a, final Object b) {
        CollectionAssertions.assertComponentWiseEquals(a, b);
    }

    protected void assertComponentWiseUnwrappedEquals(final Object a, final Object b) {
        CollectionAssertions.assertComponentWiseUnwrappedEquals(a, b);
    }

    protected void assertEmpty(final Object x) {
        if(x instanceof CharSequence) {
            assertTrue(_Strings.isEmpty((CharSequence)x));
            return;
        }
        assertEquals(0L, _NullSafe.streamAutodetect(x).count());
    }

    protected void assertDoesIncrement(final Supplier<LongAdder> adder, final Runnable runnable) {
        final int eventCount0 = adder.get().intValue();
        runnable.run();
        final int eventCount1 = adder.get().intValue();
        assertEquals(eventCount0 + 1, eventCount1);
    }

    protected void assertDoesNotIncrement(final Supplier<LongAdder> adder, final Runnable runnable) {
        final int eventCount0 = adder.get().intValue();
        runnable.run();
        final int eventCount1 = adder.get().intValue();
        assertEquals(eventCount0, eventCount1);
    }

    // -- ASSERTIONS (INTERACTIONAL)

    protected void assertInteractional(final Runnable runnable) {
        InteractionBoundaryProbe.assertInteractional(kvStoreForTesting, runnable);
    }

    protected <T> T assertInteractional(final Supplier<T> supplier) {
        return InteractionBoundaryProbe.assertInteractional(kvStoreForTesting, supplier);
    }

    // -- ASSERTIONS (TRANSACTIONAL)

    protected void assertTransactional(final Runnable runnable) {
        InteractionBoundaryProbe.assertTransactional(kvStoreForTesting, runnable);
    }

    protected <T> T assertTransactional(final Supplier<T> supplier) {
        return InteractionBoundaryProbe.assertTransactional(kvStoreForTesting, supplier);
    }

    // -- ASSERTIONS (AUDITING)

    protected void assertNoPropertyChanges() {
        val changes = EntityPropertyChangeSubscriberForTesting.getPropertyChangeEntries(kvStoreForTesting);
        assertTrue(changes.isEmpty());
    }

    protected void assertNoChangedObjectsPending() {
        // previous transaction has committed, so ChangedObjectsService should have cleared its data
        // however, this call has side-effects, it locks current transaction's capacity of further enlisting changes
        // missing a good solution on how to test this yet
        // assertTrue(getChangedObjectsService().getChangedObjectProperties().isEmpty());
    }

    protected void assertJdoBookCreatePropertyChanges() {

        val expectedAudits = _Sets.ofSorted(
                "Jdo Book/author: '[NEW]' -> 'Sample Author'",
                "Jdo Book/price: '[NEW]' -> '99.0'",
                "Jdo Book/publisher: '[NEW]' -> 'Sample Publisher'",
                "Jdo Book/isbn: '[NEW]' -> 'Sample ISBN'",
                "Jdo Book/description: '[NEW]' -> 'A sample book for testing.'",
                "Jdo Book/name: '[NEW]' -> 'Sample Book'",
                "Jdo Inventory/name: '[NEW]' -> 'Sample Inventory'");

        val actualAudits = EntityPropertyChangeSubscriberForTesting.getPropertyChangeEntries(kvStoreForTesting)
                .stream()
                .collect(Collectors.toCollection(TreeSet::new));

        assertEquals(expectedAudits, actualAudits);

        EntityPropertyChangeSubscriberForTesting.clearPropertyChangeEntries(kvStoreForTesting);

    }

    protected void assertJdoBookDeletePropertyChanges() {

        val expectedAudits = _Sets.ofSorted(
                "Jdo Book/author: 'Sample Author' -> '[DELETED]'",
                "Jdo Book/description: 'A sample book for testing.' -> '[DELETED]'",
                "Jdo Book/isbn: 'Sample ISBN' -> '[DELETED]'",
                "Jdo Book/name: 'Sample Book' -> '[DELETED]'",
                "Jdo Book/price: '12.0' -> '[DELETED]'",
                "Jdo Book/publisher: 'Sample Publisher' -> '[DELETED]'",
                "Jdo Inventory/name: 'Sample Inventory' -> '[DELETED]'");

        val actualAudits = EntityPropertyChangeSubscriberForTesting.getPropertyChangeEntries(kvStoreForTesting)
                .stream()
                .collect(Collectors.toCollection(TreeSet::new));

        assertEquals(expectedAudits, actualAudits);

        EntityPropertyChangeSubscriberForTesting.clearPropertyChangeEntries(kvStoreForTesting);

    }

    protected void assertJdoBookPriceChangePropertyChanges() {

        val expectedAudits = _Sets.ofSorted(
                "Jdo Book/price: '99.0' -> '12.0'");

        val actualAudits = EntityPropertyChangeSubscriberForTesting.getPropertyChangeEntries(kvStoreForTesting)
                .stream()
                .collect(Collectors.toCollection(TreeSet::new));

        assertEquals(expectedAudits, actualAudits);

        EntityPropertyChangeSubscriberForTesting.clearPropertyChangeEntries(kvStoreForTesting);
    }

    // -- UTILTITIES

    protected void dumpPropertyChanges() {
        val audits = EntityPropertyChangeSubscriberForTesting.getPropertyChangeEntries(kvStoreForTesting);
        System.err.println("==PROPERTY CHANGES==");
        audits.forEach(System.err::println);
        System.err.println("==========");
    }


}
