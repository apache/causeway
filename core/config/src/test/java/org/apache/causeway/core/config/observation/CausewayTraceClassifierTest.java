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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;

class CausewayTraceClassifierTest {
    @Test void usesFixedKeyForBothModes() {
        try (var context = context(true)) {
            var tracer = mock(Tracer.class);
            var span = mock(Span.class);
            when(tracer.currentSpan()).thenReturn(span);
            context.registerBean(Tracer.class, () -> tracer);
            context.refresh();
            var classifier = context.getBean(CausewayTraceClassifier.class);
            classifier.classifyCurrentSpan(CausewayTraceClassifier.ExecutionMode.FOREGROUND);
            classifier.classifyCurrentSpan(CausewayTraceClassifier.ExecutionMode.BACKGROUND);
            verify(span).tag("causeway.execution.mode", "foreground");
            verify(span).tag("causeway.execution.mode", "background");
            verifyNoMoreInteractions(span);
        }
    }
    @Test void missingTracerOrSpanIsSafe() {
        try (var context = context(true)) {
            context.refresh();
            context.getBean(CausewayTraceClassifier.class).classifyCurrentSpan(CausewayTraceClassifier.ExecutionMode.FOREGROUND);
        }
        var tracer = mock(Tracer.class);
        new CausewayTraceClassifier(tracer).classifyCurrentSpan(CausewayTraceClassifier.ExecutionMode.BACKGROUND);
        verify(tracer).currentSpan();
        verifyNoMoreInteractions(tracer);
    }
    @Test void inactiveDoesNotResolveOrMutateApplicationTracers() {
        try (var context = context(false)) {
            var first = mock(Tracer.class);
            var second = mock(Tracer.class);
            context.registerBean("first", Tracer.class, () -> first);
            context.registerBean("second", Tracer.class, () -> second);
            context.refresh();
            context.getBean(CausewayTraceClassifier.class).classifyCurrentSpan(CausewayTraceClassifier.ExecutionMode.FOREGROUND);
            verifyNoInteractions(first, second);
        }
    }
    @Test void primaryTracerIsUsedAndAmbiguityFails() {
        for (boolean primary : new boolean[]{false, true}) {
            try (var context = context(true)) {
                var first = mock(Tracer.class);
                var second = mock(Tracer.class);
                context.registerBean("first", Tracer.class, () -> first, bd -> bd.setPrimary(primary));
                context.registerBean("second", Tracer.class, () -> second);
                if (!primary) {
                    assertThrows(org.springframework.beans.factory.BeanCreationException.class, context::refresh);
                } else {
                    context.refresh();
                    context.getBean(CausewayTraceClassifier.class).classifyCurrentSpan(CausewayTraceClassifier.ExecutionMode.FOREGROUND);
                    verify(first).currentSpan();
                    verifyNoInteractions(second);
                }
            }
        }
    }
    private AnnotationConfigApplicationContext context(boolean active) {
        var context = new AnnotationConfigApplicationContext();
        if (active) context.getEnvironment().setActiveProfiles("observation");
        context.register(CausewayObservationAutoConfiguration.class);
        return context;
    }
}
