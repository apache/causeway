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
package org.apache.causeway.viewer.wicket.model.hints;

import org.apache.wicket.ajax.AjaxRequestTarget;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * All Causeway-related events subclass from this, and fall into two categories.
 *
 * <p>
 * Components that are raising events should create a subclass of {@link org.apache.causeway.viewer.wicket.model.hints.CausewayEventLetterAbstract} (the letter)
 * and then {@link org.apache.wicket.Component#send(org.apache.wicket.event.IEventSink, org.apache.wicket.event.Broadcast, Object) send} as a
 * {@link org.apache.wicket.event.Broadcast#EXACT exact} message to <tt>PageAbstract</tt>.
 * </p>
 *
 * <p>
 * Then, <tt>PageAbstract</tt> will wrap the letter into the {@link org.apache.causeway.viewer.wicket.model.hints.CausewayEnvelopeEvent} (envelope) and
 * send as a {@link org.apache.wicket.event.Broadcast#BREADTH down} to all its components.
 * </p>
 */
@RequiredArgsConstructor
public abstract class CausewayEventAbstract {

    /**
     * The {@link AjaxRequestTarget target}, if any, that caused this event to be generated.
     *
     * <p>
     * Typically populated, but not always...
     */
    @Getter private final AjaxRequestTarget target;

}

