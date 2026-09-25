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

import java.util.Objects;

import io.opentelemetry.api.trace.Span;

import org.apache.causeway.core.config.CausewayConfiguration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Adds bounded Causeway classification to the current Java-agent-owned span.
 *
 * <p>The OpenTelemetry API is safe when no Java agent is attached: the current
 * span is non-recording and attribute mutation is a no-op.</p>
 *
 * @since 2.2
 */
public final class CausewayTraceClassifier {

    public static final String EXECUTION_MODE_ATTRIBUTE =
            CausewayConfiguration.Execution.Mode.DEFAULT_KEY;

    @Getter
    @RequiredArgsConstructor
    public enum ExecutionMode {
        FOREGROUND("foreground"),
        BACKGROUND("background");

        private final String attributeValue;
    }

    private CausewayTraceClassifier() {
    }

    public static void classifyCurrentSpan(final ExecutionMode executionMode) {
        classifyCurrentSpan(EXECUTION_MODE_ATTRIBUTE, executionMode);
    }

    public static void classifyCurrentSpan(
            final String executionModeAttribute,
            final ExecutionMode executionMode) {
        classify(Span.current(), executionModeAttribute, executionMode);
    }

    static void classify(final Span span, final ExecutionMode executionMode) {
        classify(span, EXECUTION_MODE_ATTRIBUTE, executionMode);
    }

    static void classify(
            final Span span,
            final String executionModeAttribute,
            final ExecutionMode executionMode) {
        Objects.requireNonNull(span, "span");
        Objects.requireNonNull(executionModeAttribute, "executionModeAttribute");
        Objects.requireNonNull(executionMode, "executionMode");
        span.setAttribute(executionModeAttribute, executionMode.getAttributeValue());
    }
}
