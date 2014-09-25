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

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;
import org.joda.time.LocalDate;
import org.apache.isis.applib.AbstractFactoryAndRepository;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.query.Query;
import org.apache.isis.applib.query.QueryDefault;
import org.apache.isis.applib.services.bookmark.Bookmark;

/**
 * Provides supporting functionality for querying and persisting
 * {@link org.apache.isis.objectstore.jdo.applib.service.publish.PublishedEventJdo published event} entities.
 *
 * <p>
 * This supporting service with no UI and no side-effects, and is there are no other implementations of the service,
 * thus has been annotated with {@link org.apache.isis.applib.annotation.DomainService}.  This means that there is no
 * need to explicitly register it as a service (eg in <tt>isis.properties</tt>).
 */
@DomainService
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


    @Programmatic
    public List<PublishedEventJdo> findByTargetAndFromAndTo(
            final Bookmark target, 
            final LocalDate from, 
            final LocalDate to) {
        final String targetStr = target.toString();
        final Timestamp fromTs = toTimestampStartOfDayWithOffset(from, 0);
        final Timestamp toTs = toTimestampStartOfDayWithOffset(to, 1);
        
        final Query<PublishedEventJdo> query;
        if(from != null) {
            if(to != null) {
                query = new QueryDefault<PublishedEventJdo>(PublishedEventJdo.class, 
                        "findByTargetAndTimestampBetween", 
                        "targetStr", targetStr,
                        "from", fromTs,
                        "to", toTs);
            } else {
                query = new QueryDefault<PublishedEventJdo>(PublishedEventJdo.class, 
                        "findByTargetAndTimestampAfter", 
                        "targetStr", targetStr,
                        "from", fromTs);
            }
        } else {
            if(to != null) {
                query = new QueryDefault<PublishedEventJdo>(PublishedEventJdo.class, 
                        "findByTargetAndTimestampBefore", 
                        "targetStr", targetStr,
                        "to", toTs);
            } else {
                query = new QueryDefault<PublishedEventJdo>(PublishedEventJdo.class, 
                        "findByTarget", 
                        "targetStr", targetStr);
            }
        }
        return allMatches(query);
    }

    @Programmatic
    public List<PublishedEventJdo> findByFromAndTo(
            final LocalDate from, 
            final LocalDate to) {
        final Timestamp fromTs = toTimestampStartOfDayWithOffset(from, 0);
        final Timestamp toTs = toTimestampStartOfDayWithOffset(to, 1);
        
        final Query<PublishedEventJdo> query;
        if(from != null) {
            if(to != null) {
                query = new QueryDefault<PublishedEventJdo>(PublishedEventJdo.class, 
                        "findByTimestampBetween", 
                        "from", fromTs,
                        "to", toTs);
            } else {
                query = new QueryDefault<PublishedEventJdo>(PublishedEventJdo.class, 
                        "findByTimestampAfter", 
                        "from", fromTs);
            }
        } else {
            if(to != null) {
                query = new QueryDefault<PublishedEventJdo>(PublishedEventJdo.class, 
                        "findByTimestampBefore", 
                        "to", toTs);
            } else {
                query = new QueryDefault<PublishedEventJdo>(PublishedEventJdo.class, 
                        "find");
            }
        }
        return allMatches(query);
    }
    
    private static Timestamp toTimestampStartOfDayWithOffset(final LocalDate dt, int daysOffset) {
        return dt!=null
                ?new java.sql.Timestamp(dt.toDateTimeAtStartOfDay().plusDays(daysOffset).getMillis())
                :null;
    }

}
