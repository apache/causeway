/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.objectstore.jdo.applib.service.command;

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
import org.apache.isis.applib.services.command.Command;
import org.apache.isis.applib.services.command.CommandContext;


/**
 * Provides supporting functionality for querying and persisting
 * {@link org.apache.isis.objectstore.jdo.applib.service.command.CommandJdo command} entities.
 *
 * <p>
 * This supporting service with no UI and no side-effects, and is there are no other implementations of the service,
 * thus has been annotated with {@link org.apache.isis.applib.annotation.DomainService}.  This means that there is no
 * need to explicitly register it as a service (eg in <tt>isis.properties</tt>).
 */
@DomainService
public class CommandServiceJdoRepository extends AbstractFactoryAndRepository {

    @Programmatic
    public List<CommandJdo> findByFromAndTo(
            final LocalDate from, final LocalDate to) {
        final Timestamp fromTs = toTimestampStartOfDayWithOffset(from, 0);
        final Timestamp toTs = toTimestampStartOfDayWithOffset(to, 1);
        
        final Query<CommandJdo> query;
        if(from != null) {
            if(to != null) {
                query = new QueryDefault<CommandJdo>(CommandJdo.class, 
                        "findByTimestampBetween", 
                        "from", fromTs,
                        "to", toTs);
            } else {
                query = new QueryDefault<CommandJdo>(CommandJdo.class, 
                        "findByTimestampAfter", 
                        "from", fromTs);
            }
        } else {
            if(to != null) {
                query = new QueryDefault<CommandJdo>(CommandJdo.class, 
                        "findByTimestampBefore", 
                        "to", toTs);
            } else {
                query = new QueryDefault<CommandJdo>(CommandJdo.class, 
                        "find");
            }
        }
        return allMatches(query);
    }


    @Programmatic
    public CommandJdo findByTransactionId(final UUID transactionId) {
        persistCurrentCommandIfRequired();
        return firstMatch(
                new QueryDefault<CommandJdo>(CommandJdo.class, 
                        "findByTransactionId", 
                        "transactionId", transactionId));
    }

    @Programmatic
    public List<CommandJdo> findCurrent() {
        persistCurrentCommandIfRequired();
        return allMatches(
                new QueryDefault<CommandJdo>(CommandJdo.class, "findCurrent"));
    }
    
    @Programmatic
    public List<CommandJdo> findCompleted() {
        persistCurrentCommandIfRequired();
        return allMatches(
                new QueryDefault<CommandJdo>(CommandJdo.class, "findCompleted"));
    }

    private void persistCurrentCommandIfRequired() {
        if(commandContext == null || commandService == null) {
            return;
        } 
        final Command command = commandContext.getCommand();
        final CommandJdo commandJdo = commandService.asUserInitiatedCommandJdo(command);
        if(commandJdo == null) {
            return;
        } 
        persistIfNotAlready(commandJdo);
    }

    // //////////////////////////////////////

    
    @Programmatic
    public List<CommandJdo> findByTargetAndFromAndTo(
            final Bookmark target, final LocalDate from, final LocalDate to) {
        final String targetStr = target.toString();
        final Timestamp fromTs = toTimestampStartOfDayWithOffset(from, 0);
        final Timestamp toTs = toTimestampStartOfDayWithOffset(to, 1);
        
        final Query<CommandJdo> query;
        if(from != null) {
            if(to != null) {
                query = new QueryDefault<CommandJdo>(CommandJdo.class, 
                        "findByTargetAndTimestampBetween", 
                        "targetStr", targetStr,
                        "from", fromTs,
                        "to", toTs);
            } else {
                query = new QueryDefault<CommandJdo>(CommandJdo.class, 
                        "findByTargetAndTimestampAfter", 
                        "targetStr", targetStr,
                        "from", fromTs);
            }
        } else {
            if(to != null) {
                query = new QueryDefault<CommandJdo>(CommandJdo.class, 
                        "findByTargetAndTimestampBefore", 
                        "targetStr", targetStr,
                        "to", toTs);
            } else {
                query = new QueryDefault<CommandJdo>(CommandJdo.class, 
                        "findByTarget", 
                        "targetStr", targetStr);
            }
        }
        return allMatches(query);
    }

    private static Timestamp toTimestampStartOfDayWithOffset(final LocalDate dt, int daysOffset) {
        return dt!=null
                ?new java.sql.Timestamp(dt.toDateTimeAtStartOfDay().plusDays(daysOffset).getMillis())
                :null;
    }

    // //////////////////////////////////////

    
    @javax.inject.Inject
    private CommandServiceJdo commandService;
    
    @javax.inject.Inject
    private CommandContext commandContext;


}
