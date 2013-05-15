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

package org.apache.isis.applib.services.publish;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.UUID;

import org.junit.Test;

public class EventMetadataTest_getId {

    @Test
    public void test() {
        UUID transactionId = UUID.fromString("1bd8e5d4-2d67-4395-b5e8-d74acd766766");
        int sequence = 2;
        String user = "fred";
        long timestamp = 1364120978631L;
        String title = "some title";
        EventMetadata eventMetadata = new EventMetadata(transactionId, sequence, EventType.ACTION_INVOCATION, user, timestamp, title);
        
        assertThat(eventMetadata.getTransactionId(), is(UUID.fromString("1bd8e5d4-2d67-4395-b5e8-d74acd766766")));
        assertThat(eventMetadata.getSequence(), is(2));
        assertThat(eventMetadata.getUser(), is("fred"));
        assertThat(eventMetadata.getTimestamp(), is(1364120978631L));
        assertThat(eventMetadata.getId(), is("1bd8e5d4-2d67-4395-b5e8-d74acd766766.2"));
        assertThat(eventMetadata.getTitle(), is("some title"));
        assertThat(eventMetadata.getEventType(), is(EventType.ACTION_INVOCATION));
    }

}
