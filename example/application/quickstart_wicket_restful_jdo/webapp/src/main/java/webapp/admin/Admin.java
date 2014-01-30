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
import org.apache.isis.applib.annotation.Bookmarkable;
import org.apache.isis.applib.annotation.Named;
import org.apache.isis.applib.annotation.Prototype;
import org.apache.isis.applib.annotation.ActionSemantics.Of;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.objectstore.jdo.applib.service.audit.AuditEntryJdo;
import org.apache.isis.objectstore.jdo.applib.service.audit.AuditEntryRepository;
import org.apache.isis.objectstore.jdo.applib.service.background.BackgroundTaskJdo;
import org.apache.isis.objectstore.jdo.applib.service.background.BackgroundTaskRepository;
import org.apache.isis.objectstore.jdo.applib.service.interaction.InteractionJdo;
import org.apache.isis.objectstore.jdo.applib.service.interaction.InteractionRepository;
import org.apache.isis.objectstore.jdo.applib.service.publish.PublishedEvent;
import org.apache.isis.objectstore.jdo.applib.service.publish.PublishedEventRepository;

public class Admin extends AbstractService {


    @MemberOrder(sequence="10.1")
    @ActionSemantics(Of.SAFE)
    @Prototype
    public InteractionJdo lookup(final @Named("Transaction Id") UUID transactionId) {
        return interactionRepository.findByTransactionId(transactionId);
    }
    public boolean hideLookup() {
        return interactionRepository == null;
    }
    
    @ActionSemantics(Of.SAFE)
    @Bookmarkable
    @MemberOrder(sequence="10.2")
    public List<InteractionJdo> currentlyRunning() {
        return interactionRepository.findCurrent();
    }
    public boolean hideCurrentlyRunning() {
        return interactionRepository == null;
    }
    
    @ActionSemantics(Of.SAFE)
    @MemberOrder(sequence="10.3")
    public List<InteractionJdo> previouslyRan() {
        return interactionRepository.findCompleted();
    }
    public boolean hidePreviouslyRan() {
        return interactionRepository == null;
    }

    
    // //////////////////////////////////////

    @ActionSemantics(Of.SAFE)
    @Prototype
    @MemberOrder(sequence="20")
    public List<BackgroundTaskJdo> allTasks() {
        return backgroundTaskRepository.listAll();
    }
    public boolean hideAllTasks() {
        return backgroundTaskRepository == null;
    }

    // //////////////////////////////////////

    @ActionSemantics(Of.SAFE)
    @Prototype
    @MemberOrder(sequence="30")
    public List<AuditEntryJdo> allAuditEntries() {
        return auditEntryRepository.listAll();
    }
    public boolean hideAllAuditEntries() {
        return auditEntryRepository == null;
    }
    
    // //////////////////////////////////////

    @ActionSemantics(Of.SAFE)
    @MemberOrder(sequence="40.1")
    public List<PublishedEvent> listQueuedEvents() {
        return publishedEventRepository.findQueued();
    }
    public boolean hideListQueuedEvents() {
        return publishedEventRepository == null;
    }

    @ActionSemantics(Of.SAFE)
    @Prototype
    @MemberOrder(sequence="40.2")
    public List<PublishedEvent> listProcessedEvents() {
        return publishedEventRepository.findProcessed();
    }
    public boolean hideListProcessedEvents() {
        return publishedEventRepository == null;
    }

    @ActionSemantics(Of.IDEMPOTENT)
    @MemberOrder(sequence="40.3")
    public void purgeProcessedEvents() {
        publishedEventRepository.purgeProcessed();
    }
    public boolean hidePurgeProcessedEvents() {
        return publishedEventRepository == null;
    }

    // //////////////////////////////////////

    @javax.inject.Inject
    private InteractionRepository interactionRepository;
    
    @javax.inject.Inject
    private BackgroundTaskRepository backgroundTaskRepository;
    
    @javax.inject.Inject
    private AuditEntryRepository auditEntryRepository;
    
    @javax.inject.Inject
    private PublishedEventRepository publishedEventRepository;
    
}

