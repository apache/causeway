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
package org.apache.isis.objectstore.jdo.applib.service.background;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.applib.AbstractFactoryAndRepository;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.query.QueryDefault;
import org.apache.isis.objectstore.jdo.applib.service.command.CommandJdo;

/**
 * Provides supporting functionality for querying
 * {@link org.apache.isis.objectstore.jdo.applib.service.command.CommandJdo command} entities that have been persisted
 * to execute in the background.
 *
 * <p>
 * This supporting service with no UI and no side-effects, and is there are no other implementations of the service,
 * thus has been annotated with {@link org.apache.isis.applib.annotation.DomainService}.  This means that there is no
 * need to explicitly register it as a service (eg in <tt>isis.properties</tt>).
 */
@DomainService
public class BackgroundCommandServiceJdoRepository extends AbstractFactoryAndRepository {

    @SuppressWarnings("unused")
    private static final Logger LOG = LoggerFactory.getLogger(BackgroundCommandServiceJdoRepository.class);

    @Programmatic
    public List<CommandJdo> findByTransactionId(final UUID transactionId) {
        return allMatches(
                new QueryDefault<CommandJdo>(CommandJdo.class, 
                        "findBackgroundCommandByTransactionId", 
                        "transactionId", transactionId));
    }

    @Programmatic
    public List<CommandJdo> findByParent(CommandJdo parent) {
        return allMatches(
                new QueryDefault<CommandJdo>(CommandJdo.class, 
                        "findBackgroundCommandsByParent", 
                        "parent", parent));
    }

    @Programmatic
    public List<CommandJdo> findBackgroundCommandsNotYetStarted() {
        return allMatches(
                new QueryDefault<CommandJdo>(CommandJdo.class, 
                        "findBackgroundCommandsNotYetStarted"));
    }
    
}
