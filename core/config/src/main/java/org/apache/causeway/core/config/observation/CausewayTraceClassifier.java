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

import io.micrometer.tracing.Tracer;

/** Tags an existing entry span using the application's tracing bridge; never creates a span or scope. */
public final class CausewayTraceClassifier {
    public static final String EXECUTION_MODE_KEY = "causeway.execution.mode";

    public enum ExecutionMode {
        FOREGROUND("foreground"), BACKGROUND("background");
        private final String value;
        ExecutionMode(String value) { this.value = value; }
    }

    private final Tracer tracer;

    public CausewayTraceClassifier(Tracer tracer) {
        this.tracer = Objects.requireNonNull(tracer);
    }

    public void classifyCurrentSpan(ExecutionMode mode) {
        var span = tracer.currentSpan();
        if (span != null) span.tag(EXECUTION_MODE_KEY, mode.value);
    }
}
