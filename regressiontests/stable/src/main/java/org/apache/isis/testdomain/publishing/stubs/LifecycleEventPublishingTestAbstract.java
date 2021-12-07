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
package org.apache.isis.testdomain.publishing.stubs;

import java.util.Objects;

import javax.inject.Inject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import org.apache.isis.applib.events.lifecycle.AbstractLifecycleEvent;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.testdomain.jdo.entities.JdoBook;
import org.apache.isis.testdomain.jpa.entities.JpaBook;
import org.apache.isis.testdomain.publishing.PublishingTestFactoryAbstract.ChangeScenario;
import org.apache.isis.testdomain.publishing.PublishingTestFactoryAbstract.VerificationStage;
import org.apache.isis.testdomain.util.CollectionAssertions;
import org.apache.isis.testdomain.util.dto.BookDto;
import org.apache.isis.testdomain.util.event.LifecycleEventSubscriberJdoForTesting;
import org.apache.isis.testdomain.util.event.LifecycleEventSubscriberJpaForTesting;
import org.apache.isis.testdomain.util.kv.KVStoreForTesting;

import lombok.val;

public abstract class LifecycleEventPublishingTestAbstract
extends PublishingTestAbstract {

    @Inject private KVStoreForTesting kvStore;

    @Override
    protected final boolean supportsProgrammaticTesting(final ChangeScenario changeScenario) {
        return changeScenario.isSupportsProgrammatic();
    }

    @Override
    protected void given() {
        LifecycleEventSubscriberJdoForTesting.clearPublishedEvents(kvStore);
        LifecycleEventSubscriberJpaForTesting.clearPublishedEvents(kvStore);
    }

    @Override
    protected void verify(
            final ChangeScenario changeScenario,
            final VerificationStage verificationStage) {

        val defaultSample = BookDto.sample();

        val bookSamplesForCreate = Can.of(
                defaultSample,
                BookDto.builder().build()); // empty-defaults
        val bookSample1 = Can.of( // initial
                defaultSample);
        val bookSample2 = Can.of( // after property update
                defaultSample.asBuilder()
                .name("Book #2")
                .build());
        val bookSample3 = Can.of( // after action invocation
                defaultSample.asBuilder()
                .price(defaultSample.getPrice()*2.)
                .build());

        switch(verificationStage) {

        case FAILURE_CASE:

            assertHasCreatedLifecycleEvents(Can.empty());
            assertHasLoadedLifecycleEvents(Can.empty());
            assertHasPersistingLifecycleEvents(Can.empty());
            assertHasPersistedLifecycleEvents(Can.empty());
            assertHasUpdatingLifecycleEvents(Can.empty());
            assertHasUpdatedLifecycleEvents(Can.empty());
            assertHasRemovingLifecycleEvents(Can.empty());
            return;

        case PRE_COMMIT:

            switch(changeScenario) {
            case ENTITY_CREATION:
                //TODO what is there to verify?
                return;
            case ENTITY_PERSISTING:

                assertHasPersistingLifecycleEvents(bookSample1);
                //assertHasPersistedLifecycleEvents(Can.empty()); //TODO what is expected empty or not?
                return;

            case ENTITY_LOADING:
                //TODO what is there to verify?
                return;

            case PROPERTY_UPDATE: // update the book's name -> "Book #2"

                //XXX if we want to trigger callback events before PRE_COMMIT then changes need to be flushed .eg
                //val em = jpaSupport.getEntityManagerElseFail(JpaBook.class);
                //em.flush(); // in effect makes changes visible during PRE_COMMIT
                //assertHasUpdatingLifecycleEvents(bookSample2);

                //assertHasUpdatedLifecycleEvents(Can.empty()); //XXX only empty if not flushed
                return;

            case ACTION_INVOCATION: // double the book's price action -> 198.0

                //assertHasUpdatingLifecycleEvents(bookSample3); //XXX only populated if flushed
                //assertHasUpdatedLifecycleEvents(Can.empty()); //XXX only empty if not flushed
                return;

            case ENTITY_REMOVAL:

                assertHasRemovingLifecycleEvents(bookSample1);
                return;

            default:
                throw _Exceptions.unmatchedCase(changeScenario);
            }

        case POST_INTERACTION:
        case POST_COMMIT:

            switch(changeScenario) {
            case ENTITY_CREATION:

                assertHasCreatedLifecycleEvents(bookSamplesForCreate);
                assertHasLoadedLifecycleEvents(Can.empty());
                assertHasPersistingLifecycleEvents(Can.empty());
                assertHasPersistedLifecycleEvents(Can.empty());
                assertHasUpdatingLifecycleEvents(Can.empty());
                assertHasUpdatedLifecycleEvents(Can.empty());
                assertHasRemovingLifecycleEvents(Can.empty());
                return;

            case ENTITY_PERSISTING:

                assertHasCreatedLifecycleEvents(Can.empty()); // creation events are deliberately not triggered for this test
                assertHasLoadedLifecycleEvents(Can.empty());
                assertHasPersistingLifecycleEvents(bookSample1);
                assertHasPersistedLifecycleEvents(bookSample1);
                assertHasUpdatingLifecycleEvents(Can.empty());
                assertHasUpdatedLifecycleEvents(Can.empty());
                assertHasRemovingLifecycleEvents(Can.empty());
                return;

            case ENTITY_LOADING:

                assertHasCreatedLifecycleEvents(Can.empty());
                assertHasLoadedLifecycleEvents(bookSample1);
                assertHasPersistingLifecycleEvents(Can.empty());
                assertHasPersistedLifecycleEvents(Can.empty());
                assertHasUpdatingLifecycleEvents(Can.empty());
                assertHasUpdatedLifecycleEvents(Can.empty());
                assertHasRemovingLifecycleEvents(Can.empty());
                return;


            case PROPERTY_UPDATE: // update the book's name -> "Book #2"

                assertHasCreatedLifecycleEvents(Can.empty());
                assertHasLoadedLifecycleEvents(Can.empty());
                assertHasPersistingLifecycleEvents(Can.empty());
                assertHasPersistedLifecycleEvents(Can.empty());
                assertHasUpdatingLifecycleEvents(bookSample2);
                assertHasUpdatedLifecycleEvents(bookSample2);
                assertHasRemovingLifecycleEvents(Can.empty());
                return;

            case ACTION_INVOCATION: // double the book's price action -> 198.0

                assertHasCreatedLifecycleEvents(Can.empty());
                assertHasLoadedLifecycleEvents(Can.empty());
                assertHasPersistingLifecycleEvents(Can.empty());
                assertHasPersistedLifecycleEvents(Can.empty());
                assertHasUpdatingLifecycleEvents(bookSample3);
                assertHasUpdatedLifecycleEvents(bookSample3);
                assertHasRemovingLifecycleEvents(Can.empty());
                return;

            case ENTITY_REMOVAL:

                assertHasCreatedLifecycleEvents(Can.empty());
                assertHasLoadedLifecycleEvents(Can.empty());
                assertHasPersistingLifecycleEvents(Can.empty());
                assertHasPersistedLifecycleEvents(Can.empty());
                assertHasUpdatingLifecycleEvents(Can.empty());
                assertHasUpdatedLifecycleEvents(Can.empty());
                assertHasRemovingLifecycleEvents(bookSample1);
                return;

            default:
                throw _Exceptions.unmatchedCase(changeScenario);
            }


        default:
            // if hitting this, the caller is requesting a verification stage, we are providing no case for
            fail(String.format("internal error, stage not verified: %s", verificationStage));
        }
    }

    // -- HELPER

    // these events are emitted by the FactoryService only!
    private void assertHasCreatedLifecycleEvents(final Can<BookDto> expectedBooks) {
        assertHasLifecycleEvents(
                JdoBook.CreatedLifecycleEvent.class,
                JpaBook.CreatedLifecycleEvent.class,
                expectedBooks);
    }

    private void assertHasLoadedLifecycleEvents(final Can<BookDto> expectedBooks) {
        assertHasLifecycleEvents(
                JdoBook.LoadedLifecycleEvent.class,
                JpaBook.LoadedLifecycleEvent.class,
                expectedBooks);
    }

    private void assertHasPersistingLifecycleEvents(final Can<BookDto> expectedBooks) {
        assertHasLifecycleEvents(
                JdoBook.PersistingLifecycleEvent.class,
                JpaBook.PersistingLifecycleEvent.class,
                expectedBooks);
    }

    private void assertHasPersistedLifecycleEvents(final Can<BookDto> expectedBooks) {
        assertHasLifecycleEvents(
                JdoBook.PersistedLifecycleEvent.class,
                JpaBook.PersistedLifecycleEvent.class,
                expectedBooks);
    }

    private void assertHasUpdatingLifecycleEvents(final Can<BookDto> expectedBooks) {
        assertHasLifecycleEvents(
                JdoBook.UpdatingLifecycleEvent.class,
                JpaBook.UpdatingLifecycleEvent.class,
                expectedBooks);
    }

    private void assertHasUpdatedLifecycleEvents(final Can<BookDto> expectedBooks) {
        assertHasLifecycleEvents(
                JdoBook.UpdatedLifecycleEvent.class,
                JpaBook.UpdatedLifecycleEvent.class,
                expectedBooks);
    }

    private void assertHasRemovingLifecycleEvents(final Can<BookDto> expectedBooks) {
        assertHasLifecycleEvents(
                JdoBook.RemovingLifecycleEvent.class,
                JpaBook.RemovingLifecycleEvent.class,
                expectedBooks);
    }

    private void assertHasLifecycleEvents(
            final Class<? extends AbstractLifecycleEvent<JdoBook>> eventClassWhenJdo,
            final Class<? extends AbstractLifecycleEvent<JpaBook>> eventClassWhenJpa,
            final Can<BookDto> expectedBooks) {

        val jdoBooks = LifecycleEventSubscriberJdoForTesting
                .getPublishedEventsJdo(kvStore, eventClassWhenJdo);
        val jpaBooks = LifecycleEventSubscriberJpaForTesting
                .getPublishedEventsJpa(kvStore, eventClassWhenJpa);

        assertEquals(0, jdoBooks.size() * jpaBooks.size()); // its either JDO or JPA, cannot be both

        val actualBooks = jdoBooks.isEmpty()
                ? jpaBooks
                : jdoBooks;

        CollectionAssertions.assertComponentWiseEquals(
                expectedBooks, actualBooks, this::bookDifference);
    }

    private String bookDifference(final BookDto a, final BookDto b) {
        if(!Objects.equals(a.toString(), b.toString())) {
            return String.format("differing string representation %s != %s",
                    a.toString(), b.toString());
        }
        return null; // no difference
    }


}
