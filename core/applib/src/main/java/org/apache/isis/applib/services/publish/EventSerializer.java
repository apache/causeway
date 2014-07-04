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

import org.w3c.dom.Document;

import org.apache.isis.applib.annotation.Programmatic;

public interface EventSerializer {

    /**
     * Combines the {@link EventMetadata metadata} and the {@link EventPayload payload}
     * into some serialized form (such as JSON, XML or a string) that can then be published.
     * 
     * <p>
     * This method returns an object for maximum flexibility, which is then
     * handed off to the {@link PublishingService}.  It's important to make sure that the
     * publishing service is able to handle the serialized form.  Strings are a good
     * lowest common denominator, but in some cases are type-safe equivalent, such as a
     * {@link Document w3c DOM Document} or a JSON node might be passed instead.
     *  
     * @return a string, some JSON, some XML or some other standard serialized form. 
     */
    public Object serialize(EventMetadata metadata, EventPayload payload);

    /**
     * Used as a fallback if no other implementation is defined.
     */
    public static class Simple implements EventSerializer {

        @Programmatic
        @Override
        public Object serialize(EventMetadata metadata, EventPayload payload) {
            return "PUBLISHED:" +
            		"\n  metadata:" + 
                    "\n    id       :" + metadata.getId() + 
                    "\n    eventType:" + metadata.getEventType() + 
                    "\n    user     :" + metadata.getUser() + 
                    "\n    timestamp:" + metadata.getTimestamp() + 
                    "\n  payload:" +
                    "\n" + payload.toString();
        }
    }

}
