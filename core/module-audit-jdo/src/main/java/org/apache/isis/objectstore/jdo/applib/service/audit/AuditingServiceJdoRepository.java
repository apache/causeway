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
package org.apache.isis.objectstore.jdo.applib.service.audit;

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
 * Provides supporting functionality for querying
 * {@link org.apache.isis.objectstore.jdo.applib.service.audit.AuditEntryJdo audit entry} entities.
 *
 * <p>
 * This supporting service with no UI and no side-effects, and is there are no other implementations of the service,
 * thus has been annotated with {@link org.apache.isis.applib.annotation.DomainService}.  This means that there is no
 * need to explicitly register it as a service (eg in <tt>isis.properties</tt>).
 */
@DomainService
public class AuditingServiceJdoRepository extends AbstractFactoryAndRepository {
    
    @Programmatic
    public List<AuditEntryJdo> findByTransactionId(final UUID transactionId) {
        return allMatches(
                new QueryDefault<AuditEntryJdo>(AuditEntryJdo.class, 
                        "findByTransactionId", 
                        "transactionId", transactionId));
    }

    @Programmatic
    public List<AuditEntryJdo> findByTargetAndFromAndTo(
            final Bookmark target, 
            final LocalDate from, 
            final LocalDate to) {
        final String targetStr = target.toString();
        final Timestamp fromTs = toTimestampStartOfDayWithOffset(from, 0);
        final Timestamp toTs = toTimestampStartOfDayWithOffset(to, 1);
        
        final Query<AuditEntryJdo> query;
        if(from != null) {
            if(to != null) {
                query = new QueryDefault<AuditEntryJdo>(AuditEntryJdo.class, 
                        "findByTargetAndTimestampBetween", 
                        "targetStr", targetStr,
                        "from", fromTs,
                        "to", toTs);
            } else {
                query = new QueryDefault<AuditEntryJdo>(AuditEntryJdo.class, 
                        "findByTargetAndTimestampAfter", 
                        "targetStr", targetStr,
                        "from", fromTs);
            }
        } else {
            if(to != null) {
                query = new QueryDefault<AuditEntryJdo>(AuditEntryJdo.class, 
                        "findByTargetAndTimestampBefore", 
                        "targetStr", targetStr,
                        "to", toTs);
            } else {
                query = new QueryDefault<AuditEntryJdo>(AuditEntryJdo.class, 
                        "findByTarget", 
                        "targetStr", targetStr);
            }
        }
        return allMatches(query);
    }

    @Programmatic
    public List<AuditEntryJdo> findByFromAndTo(
            final LocalDate from, 
            final LocalDate to) {
        final Timestamp fromTs = toTimestampStartOfDayWithOffset(from, 0);
        final Timestamp toTs = toTimestampStartOfDayWithOffset(to, 1);
        
        final Query<AuditEntryJdo> query;
        if(from != null) {
            if(to != null) {
                query = new QueryDefault<AuditEntryJdo>(AuditEntryJdo.class, 
                        "findByTimestampBetween", 
                        "from", fromTs,
                        "to", toTs);
            } else {
                query = new QueryDefault<AuditEntryJdo>(AuditEntryJdo.class, 
                        "findByTimestampAfter", 
                        "from", fromTs);
            }
        } else {
            if(to != null) {
                query = new QueryDefault<AuditEntryJdo>(AuditEntryJdo.class, 
                        "findByTimestampBefore", 
                        "to", toTs);
            } else {
                query = new QueryDefault<AuditEntryJdo>(AuditEntryJdo.class, 
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
