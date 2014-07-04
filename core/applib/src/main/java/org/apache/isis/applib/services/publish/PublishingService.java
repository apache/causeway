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


import org.apache.isis.applib.annotation.Hidden;


/**
 * Will be called whenever an publishable entity has changed its state, or an published action has been invoked.
 *
 * <p>
 * Typically an entity is marked to be published using the {@link org.apache.isis.applib.annotation.PublishedObject}
 * annotation, and an action is marked to be published using the
 * {@link org.apache.isis.applib.annotation.PublishedAction} annotation.
 *
 * <p>
 * There are currently two implementations, <tt>PublishingServiceJdo</tt> (part of the
 * <tt>o.a.i.module:isis-module-publishing-jdo</tt>) and the demo
 * {@link org.apache.isis.applib.services.publish.PublishingService.Stderr}.
 *
 * <p>
 * To use either service, must include on the classpath and also register the service (eg in <tt>isis.properties</tt>).
 */
public interface PublishingService {
    
    @Hidden
    public void publish(EventMetadata metadata, EventPayload payload);
    
    public static class Stderr implements PublishingService {

        private EventSerializer eventSerializer = new EventSerializer.Simple();

        @Hidden
        @Override
        public void publish(EventMetadata metadata, EventPayload payload) {
            Object serializedEvent = eventSerializer.serialize(metadata, payload);
            System.err.println(serializedEvent);
        }

        @Override
        public void setEventSerializer(EventSerializer eventSerializer) {
            this.eventSerializer = eventSerializer;
        }
    }
    
    void setEventSerializer(EventSerializer eventSerializer);
}


