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
package org.apache.causeway.core.config.observation;

import org.junit.jupiter.api.Test;

import io.micrometer.observation.Observation;
import io.micrometer.tracing.Tracer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class CausewayTracingObservationHandlerTest {

    private final CausewayTracingObservationHandler handler =
            new CausewayTracingObservationHandler(mock(Tracer.class));

    @Test
    void preservesContextualNameCasingAndPunctuation() {
        final Observation.Context context = new Observation.Context();
        context.setName("causeway.action.invocation");
        context.setContextualName("act ApplicationUser#updateEmailAddress");

        assertEquals("act ApplicationUser#updateEmailAddress",
                handler.getSpanName(context));
    }

    @Test
    void fallsBackToStableObservationName() {
        final Observation.Context context = new Observation.Context();
        context.setName("causeway.root.interaction");

        assertEquals("causeway.root.interaction", handler.getSpanName(context));
    }

    @Test
    void defensivelyBoundsExternalContextualNames() {
        final Observation.Context context = new Observation.Context();
        context.setName("causeway.test");
        context.setContextualName("X".repeat(60));

        assertEquals("X".repeat(50), handler.getSpanName(context));
    }
}
