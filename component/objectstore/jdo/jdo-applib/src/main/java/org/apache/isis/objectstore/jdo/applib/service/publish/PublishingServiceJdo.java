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

import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.isis.applib.AbstractService;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.command.CommandContext;
import org.apache.isis.applib.services.publish.EventMetadata;
import org.apache.isis.applib.services.publish.EventPayload;
import org.apache.isis.applib.services.publish.EventSerializer;
import org.apache.isis.applib.services.publish.PublishingService;

/**
 * An implementation of {@link PublishingService} that persists events as
 * entities into a JDO-backed database.
 */
public class PublishingServiceJdo extends AbstractService implements PublishingService {

    
    @Programmatic
    @PostConstruct
    public void init(Map<String,String> props) {
        ensureDependenciesInjected();
    }
    
    // //////////////////////////////////////
    
    private void ensureDependenciesInjected() {
        if(this.commandContext == null) {
            throw new IllegalStateException(this.getClassName() + " requires CommandContext service to be configured");
        }
        if(this.eventSerializer == null) {
            throw new IllegalStateException(this.getClassName() + " requires EventSerializer service to be configured");
        }
    }

    
    @Override
    @Programmatic
    public void publish(final EventMetadata metadata, final EventPayload payload) {
        final String serializedEvent = eventSerializer.serialize(metadata, payload).toString();
        final PublishedEventJdo publishedEvent = newTransientInstance(PublishedEventJdo.class);
        publishedEvent.setSerializedForm(serializedEvent);
        publishedEvent.setTransactionId(metadata.getTransactionId());
        publishedEvent.setSequence(metadata.getSequence());
        publishedEvent.setEventType(metadata.getEventType());
        publishedEvent.setTimestamp(metadata.getJavaSqlTimestamp());
        publishedEvent.setUser(metadata.getUser());
        publishedEvent.setTitle(metadata.getTitle());
        
        publishedEvent.setTargetClass(metadata.getTargetClass());
        publishedEvent.setTarget(metadata.getTarget());
        publishedEvent.setTargetAction(metadata.getTargetAction());
        publishedEvent.setMemberIdentifier(metadata.getActionIdentifier());
        
        persist(publishedEvent);
    }

    // //////////////////////////////////////

    private EventSerializer eventSerializer;
    
    @Override
    public void setEventSerializer(EventSerializer eventSerializer) {
        this.eventSerializer = eventSerializer;
    }

    
    @javax.inject.Inject
    private CommandContext commandContext;
}
