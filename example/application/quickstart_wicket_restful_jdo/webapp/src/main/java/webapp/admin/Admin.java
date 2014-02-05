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
package webapp.admin;

import java.util.List;
import java.util.UUID;

import org.apache.isis.applib.AbstractService;
import org.apache.isis.applib.annotation.ActionSemantics;
import org.apache.isis.applib.annotation.ActionSemantics.Of;
import org.apache.isis.applib.annotation.Bookmarkable;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Named;
import org.apache.isis.applib.annotation.Prototype;
import org.apache.isis.objectstore.jdo.applib.service.audit.AuditEntryJdo;
import org.apache.isis.objectstore.jdo.applib.service.audit.AuditingServiceJdoRepository;
import org.apache.isis.objectstore.jdo.applib.service.background.BackgroundCommandServiceJdoRepository;
import org.apache.isis.objectstore.jdo.applib.service.command.CommandJdo;
import org.apache.isis.objectstore.jdo.applib.service.command.CommandServiceJdoRepository;
import org.apache.isis.objectstore.jdo.applib.service.publish.PublishedEventJdo;
import org.apache.isis.objectstore.jdo.applib.service.publish.PublishingServiceJdoRepository;

public class Admin extends AbstractService {

    @MemberOrder(sequence="10.1")
    @ActionSemantics(Of.SAFE)
    @Prototype
    public CommandJdo lookupCommand(
            final @Named("Transaction Id") UUID transactionId) {
        return commandServiceRepository.findByTransactionId(transactionId);
    }
    public boolean hideLookupCommand() {
        return commandServiceRepository == null;
    }
    
    // //////////////////////////////////////
    
    @ActionSemantics(Of.SAFE)
    @Bookmarkable
    @MemberOrder(sequence="10.2")
    public List<CommandJdo> commandsCurrentlyRunning() {
        return commandServiceRepository.findCurrent();
    }
    public boolean hideCommandsCurrentlyRunning() {
        return commandServiceRepository == null;
    }
    
    // //////////////////////////////////////
    
    @ActionSemantics(Of.SAFE)
    @MemberOrder(sequence="10.3")
    public List<CommandJdo> commandsPreviouslyRan() {
        return commandServiceRepository.findCompleted();
    }
    public boolean hideCommandsPreviouslyRan() {
        return commandServiceRepository == null;
    }

    // //////////////////////////////////////

    @ActionSemantics(Of.SAFE)
    @Prototype
    @MemberOrder(sequence="20")
    public List<CommandJdo> allCommands() {
        return backgroundCommandServiceRepository.listAll();
    }
    public boolean hideAllCommands() {
        return backgroundCommandServiceRepository == null;
    }

    // //////////////////////////////////////

    @ActionSemantics(Of.SAFE)
    @Prototype
    @MemberOrder(sequence="30")
    public List<AuditEntryJdo> allAuditEntries() {
        return auditingServiceRepository.listAll();
    }
    public boolean hideAllAuditEntries() {
        return auditingServiceRepository == null;
    }
    
    // //////////////////////////////////////

    @ActionSemantics(Of.SAFE)
    @MemberOrder(sequence="40.1")
    public List<PublishedEventJdo> allQueuedEvents() {
        return publishingServiceRepository.findQueued();
    }
    public boolean hideAllQueuedEvents() {
        return publishingServiceRepository == null;
    }

    @ActionSemantics(Of.SAFE)
    @Prototype
    @MemberOrder(sequence="40.2")
    public List<PublishedEventJdo> allProcessedEvents() {
        return publishingServiceRepository.findProcessed();
    }
    public boolean hideAllProcessedEvents() {
        return publishingServiceRepository == null;
    }

    @ActionSemantics(Of.IDEMPOTENT)
    @MemberOrder(sequence="40.3")
    public void purgeProcessedEvents() {
        publishingServiceRepository.purgeProcessed();
    }
    public boolean hidePurgeProcessedEvents() {
        return publishingServiceRepository == null;
    }

    // //////////////////////////////////////

    @javax.inject.Inject
    private CommandServiceJdoRepository commandServiceRepository;
    
    @javax.inject.Inject
    private BackgroundCommandServiceJdoRepository backgroundCommandServiceRepository;
    
    @javax.inject.Inject
    private AuditingServiceJdoRepository auditingServiceRepository;
    
    @javax.inject.Inject
    private PublishingServiceJdoRepository publishingServiceRepository;
    
}

