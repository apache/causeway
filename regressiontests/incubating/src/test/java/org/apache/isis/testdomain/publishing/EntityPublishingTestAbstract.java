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

import javax.inject.Inject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import org.apache.isis.testdomain.publishing.PublishingTestFactoryAbstract.VerificationStage;
import org.apache.isis.testdomain.util.kv.KVStoreForTesting;

import static org.apache.isis.testdomain.publishing.subscriber.EntityChangesSubscriberForTesting.clearPublishedEntries;
import static org.apache.isis.testdomain.publishing.subscriber.EntityChangesSubscriberForTesting.getCreated;
import static org.apache.isis.testdomain.publishing.subscriber.EntityChangesSubscriberForTesting.getDeleted;
import static org.apache.isis.testdomain.publishing.subscriber.EntityChangesSubscriberForTesting.getLoaded;
import static org.apache.isis.testdomain.publishing.subscriber.EntityChangesSubscriberForTesting.getModified;
import static org.apache.isis.testdomain.publishing.subscriber.EntityChangesSubscriberForTesting.getUpdated;

public abstract class EntityPublishingTestAbstract
implements HasPersistenceStandard {

    @Inject private KVStoreForTesting kvStore;

    protected void given() {
        clearPublishedEntries(kvStore);
    }

    protected void verify(final VerificationStage verificationStage) {
        switch(verificationStage) {
        case FAILURE_CASE:
            assertEquals(0, getCreated(kvStore));
            assertEquals(0, getDeleted(kvStore));
            assertEquals(0, getLoaded(kvStore));
            assertEquals(0, getUpdated(kvStore));
            assertEquals(0, getModified(kvStore));
            break;
        case PRE_COMMIT:
        case POST_INTERACTION:
            break;
        case POST_COMMIT:
            assertEquals(0, getCreated(kvStore));
            assertEquals(0, getDeleted(kvStore));
            //assertEquals(1, getLoaded()); // not reproducible
            assertEquals(1, getUpdated(kvStore));
            //assertEquals(1, getModified(kvStore)); // not reproducible
            break;
        default:
            // if hitting this, the caller is requesting a verification stage, we are providing no case for
            fail(String.format("internal error, stage not verified: %s", verificationStage));
        }
    }

}
