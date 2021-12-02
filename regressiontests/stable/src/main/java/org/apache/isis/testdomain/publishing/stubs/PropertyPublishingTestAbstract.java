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

import javax.inject.Inject;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.testdomain.publishing.PublishingTestFactoryAbstract.ChangeScenario;
import org.apache.isis.testdomain.publishing.PublishingTestFactoryAbstract.VerificationStage;
import org.apache.isis.testdomain.publishing.subscriber.EntityPropertyChangeSubscriberForTesting;
import org.apache.isis.testdomain.util.CollectionAssertions;
import org.apache.isis.testdomain.util.dto.BookDto;
import org.apache.isis.testdomain.util.kv.KVStoreForTesting;

import lombok.val;

public abstract class PropertyPublishingTestAbstract
extends PublishingTestAbstract {

    @Inject private KVStoreForTesting kvStore;

    @Override
    protected final boolean supportsProgrammaticTesting(final ChangeScenario changeScenario) {
        return changeScenario.isSupportsProgrammatic();
    }

    @Override
    protected void given() {
        EntityPropertyChangeSubscriberForTesting.clearPropertyChangeEntries(kvStore);
    }

    @Override
    protected void verify(
            final ChangeScenario changeScenario,
            final VerificationStage verificationStage) {
        switch(verificationStage) {
        case FAILURE_CASE:
            assertHasPropertyChangeEntries(Can.empty());
            break;
        case PRE_COMMIT:
        case POST_INTERACTION:
            break;
        case POST_COMMIT:

            val defaultBook = BookDto.sample();

            switch(changeScenario) {
            case ENTITY_CREATION:
                return; // factory-service does not trigger property publishing
            case ENTITY_LOADING:
                return; // not subject of change tests
            case ENTITY_PERSISTING:
                assertContainsPropertyChangeEntries(Can.of(
                        formatPersistenceStandardSpecificCapitalize("%s Book/name: '[NEW]' -> '" + defaultBook.getName() + "'")));
                return;
            case PROPERTY_UPDATE:
                assertHasPropertyChangeEntries(Can.of(
                        formatPersistenceStandardSpecificCapitalize("%s Book/name: '" + defaultBook.getName() + "' -> 'Book #2'")));
                return;
            case ACTION_INVOCATION:
                assertHasPropertyChangeEntries(Can.of(
                        formatPersistenceStandardSpecificCapitalize("%s Book/price: '" + defaultBook.getPrice() + "' -> '" + (2.*defaultBook.getPrice()) + "'")));
                return;
            case ENTITY_REMOVAL:
                assertContainsPropertyChangeEntries(Can.of(
                        formatPersistenceStandardSpecificCapitalize("%s Book/name: '" + defaultBook.getName() + "' -> '[DELETED]'")));
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

    private void assertContainsPropertyChangeEntries(final Can<String> expectedAuditEntries) {
        val actualAuditEntries = EntityPropertyChangeSubscriberForTesting.getPropertyChangeEntries(kvStore);
        expectedAuditEntries.forEach(expectedAuditEntry->{
            assertTrue(actualAuditEntries.contains(expectedAuditEntry),
                    ()->String.format("expectedAuditEntry (%s) not found in %s", expectedAuditEntry, actualAuditEntries));
        });

    }

    private void assertHasPropertyChangeEntries(final Can<String> expectedAuditEntries) {
        val actualAuditEntries = EntityPropertyChangeSubscriberForTesting.getPropertyChangeEntries(kvStore);
        CollectionAssertions.assertComponentWiseEquals(expectedAuditEntries, actualAuditEntries);
    }


}
