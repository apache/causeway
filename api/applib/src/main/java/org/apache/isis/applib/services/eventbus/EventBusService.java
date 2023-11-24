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
package org.apache.isis.applib.services.eventbus;

/**
 * A service implementing an Event Bus, allowing domain objects to emit
 * arbitrary events on an in-memory event bus.
 *
 * <p>
 * Events are delivered synchronously to event subscribers (domain services).
 * </p>
 *
 * @since 2.0 {@index}
 */
public interface EventBusService {

    /**
     * Post an event (of any class) to the in-memory event bus.
     *
     * <p>
     *     The event will be delivered synchronously (within the same
     *     transactional boundary) to all subscribers of that event type
     *     (with subscribers as domain services with public method annotated
     *     using Spring's
     *     {@link org.springframework.context.event.EventListener} annotation.
     * </p>
     */
    void post(Object event);

}

