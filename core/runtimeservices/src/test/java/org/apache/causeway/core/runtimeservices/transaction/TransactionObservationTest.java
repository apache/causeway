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
package org.apache.causeway.core.runtimeservices.transaction;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;

import org.apache.causeway.core.config.observation.CausewayObservationIntegration;
import org.apache.causeway.core.metamodel.execution.ExecutionContext;
import org.apache.causeway.core.metamodel.execution.InteractionLayerStack;
import org.apache.causeway.core.metamodel.execution.InteractionLayerTracker;
import org.apache.causeway.core.security.authentication.InteractionContextFactory;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationHandler;
import io.micrometer.observation.ObservationRegistry;

class TransactionObservationTest {
    @Test void eachManagerOwnsAClosureAndCleanupRestoresContext() {
        var registry = ObservationRegistry.create();
        var stopped = new ArrayList<Observation.Context>();
        registry.observationConfig().observationHandler(new ObservationHandler<Observation.Context>() {
            public boolean supportsContext(Observation.Context context) { return true; }
            public void onStop(Observation.Context context) { stopped.add(context); }
        });
        var managers = List.of(mock(PlatformTransactionManager.class), mock(PlatformTransactionManager.class));
        for (var manager : managers) {
            var status = mock(TransactionStatus.class);
            when(status.isNewTransaction()).thenReturn(true);
            when(manager.getTransaction(any())).thenReturn(status);
        }
        var service = new TransactionServiceSpring(managers, List.of(), () -> mock(InteractionLayerTracker.class),
                mock(ConfigurableListableBeanFactory.class), new CausewayObservationIntegration(registry));
        var executionContext = mock(ExecutionContext.class, RETURNS_DEEP_STUBS);
        when(executionContext.idGenerator().interactionId()).thenReturn(UUID.randomUUID());
        var stack = new InteractionLayerStack();
        var layer = stack.push(executionContext, InteractionContextFactory.testing(), Observation.NOOP);
        try {
            service.onOpen(layer.interaction());
            service.onClose(layer.interaction());
            assertEquals(2, stopped.stream().filter(c -> c.getName().equals("Transaction")).count());
            assertNull(registry.getCurrentObservation());
        } finally { stack.clear(); }
    }
}
