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
package org.apache.isis.core.runtimeservices.clock;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.applib.clock.VirtualClock;
import org.apache.isis.applib.services.clock.ClockService;
import org.apache.isis.applib.services.iactnlayer.InteractionContext;
import org.apache.isis.applib.services.iactnlayer.InteractionLayerTracker;

import lombok.RequiredArgsConstructor;

@Service
@Named("isis.runtimeservices.ClockServiceDefault")
@Order(OrderPrecedence.MIDPOINT)
@Primary
@Qualifier("Default")
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class ClockServiceDefault implements ClockService {

    private final InteractionLayerTracker interactionLayerTracker;

    @Override
    public VirtualClock getClock() {
        return interactionLayerTracker.currentInteractionContext()
        .map(InteractionContext::getClock)
        .orElseGet(VirtualClock::system);
    }

}
