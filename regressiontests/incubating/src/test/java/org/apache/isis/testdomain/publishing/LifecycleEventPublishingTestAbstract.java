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
package org.apache.isis.testdomain.publishing;

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
import org.apache.isis.testdomain.util.event.LifecycleEventSubscriberForTesting;
import org.apache.isis.testdomain.util.kv.KVStoreForTesting;

import lombok.val;

public abstract class LifecycleEventPublishingTestAbstract
extends PublishingTestAbstract {

    @Inject private KVStoreForTesting kvStore;

    @Override
    protected final boolean supportsProgrammaticTesting(final ChangeScenario changeScenario) {
        return false;
    }

    @Override
    protected void given() {
        LifecycleEventSubscriberForTesting.clearPublishedEvents(kvStore);
    }

    @Override
    protected void verify(
            final ChangeScenario changeScenario,
            final VerificationStage verificationStage) {
        switch(verificationStage) {

        case FAILURE_CASE:

            assertHasLifecycleEvents(
                    JdoBook.PersistingLifecycleEvent.class,
                    JpaBook.PersistingLifecycleEvent.class,
                    Can.empty());

            break;
        case PRE_COMMIT:
        case POST_INTERACTION:
            break;
        case POST_COMMIT:

            switch(changeScenario) {
            case PROPERTY_UPDATE:
                //TODO add assertions
                break;
            case ACTION_INVOCATION:
                //TODO add assertions
                break;
            default:
                //TODO add assertions ...
                throw _Exceptions.unmatchedCase(changeScenario);
            }

            break;
        default:
            // if hitting this, the caller is requesting a verification stage, we are providing no case for
            fail(String.format("internal error, stage not verified: %s", verificationStage));
        }
    }

    // -- HELPER

    private void assertHasLifecycleEvents(
            final Class<? extends AbstractLifecycleEvent<JdoBook>> eventClassWhenJdo,
            final Class<? extends AbstractLifecycleEvent<JpaBook>> eventClassWhenJpa,
            final Can<BookDto> expectedBooks) {

        val jdoBooks = LifecycleEventSubscriberForTesting
                .getPublishedEventsJdo(kvStore, eventClassWhenJdo);
        val jpaBooks = LifecycleEventSubscriberForTesting
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
