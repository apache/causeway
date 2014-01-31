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

package org.apache.isis.objectstore.jdo.applib.service.publish;

import java.util.List;
import java.util.UUID;

import org.apache.isis.applib.AbstractFactoryAndRepository;
import org.apache.isis.applib.annotation.ActionSemantics;
import org.apache.isis.applib.annotation.NotContributed;
import org.apache.isis.applib.annotation.NotInServiceMenu;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.annotation.ActionSemantics.Of;
import org.apache.isis.applib.annotation.NotContributed.As;
import org.apache.isis.applib.query.QueryDefault;
import org.apache.isis.applib.services.HasTransactionId;

public class PublishingServiceJdoRepository extends AbstractFactoryAndRepository {

    @Programmatic
    public List<PublishedEventJdo> findQueued() {
        return allMatches(
                new QueryDefault<PublishedEventJdo>(PublishedEventJdo.class, 
                        "findByStateOrderByTimestamp", 
                        "state", PublishedEventJdo.State.QUEUED));
    }

    @Programmatic
    public List<PublishedEventJdo> findProcessed() {
        return allMatches(
                new QueryDefault<PublishedEventJdo>(PublishedEventJdo.class, 
                        "findByStateOrderByTimestamp", 
                        "state", PublishedEventJdo.State.PROCESSED));
    }

    @Programmatic
    public List<PublishedEventJdo> findByTransactionId(final UUID transactionId) {
        return allMatches(
                new QueryDefault<PublishedEventJdo>(PublishedEventJdo.class, 
                        "findByTransactionId", 
                        "transactionId", transactionId));
    }

    @Programmatic
    public void purgeProcessed() {
        // REVIEW: this is not particularly performant.
        // much better would be to go direct to the JDO API.
        List<PublishedEventJdo> processedEvents = findProcessed();
        for (PublishedEventJdo publishedEvent : processedEvents) {
            publishedEvent.delete();
        }
    }

}
