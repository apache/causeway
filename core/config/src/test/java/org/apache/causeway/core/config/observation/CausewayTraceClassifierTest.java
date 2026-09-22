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

import io.opentelemetry.api.trace.Span;

import static org.apache.causeway.core.config.observation.CausewayTraceClassifier.EXECUTION_MODE_ATTRIBUTE;
import static org.apache.causeway.core.config.observation.CausewayTraceClassifier.ExecutionMode.BACKGROUND;
import static org.apache.causeway.core.config.observation.CausewayTraceClassifier.ExecutionMode.FOREGROUND;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class CausewayTraceClassifierTest {

    @Test
    void classifiesForegroundAndBackgroundWithBoundedValues() {
        final Span span = mock(Span.class);

        CausewayTraceClassifier.classify(span, FOREGROUND);
        CausewayTraceClassifier.classify(span, BACKGROUND);

        verify(span).setAttribute(EXECUTION_MODE_ATTRIBUTE, "foreground");
        verify(span).setAttribute(EXECUTION_MODE_ATTRIBUTE, "background");
    }

    @Test
    void repeatedClassificationIsIdempotentAttributeMutation() {
        final Span span = mock(Span.class);

        CausewayTraceClassifier.classify(span, FOREGROUND);
        CausewayTraceClassifier.classify(span, FOREGROUND);

        verify(span, times(2)).setAttribute(EXECUTION_MODE_ATTRIBUTE, "foreground");
    }

    @Test
    void noAgentCurrentSpanIsSafe() {
        CausewayTraceClassifier.classifyCurrentSpan(BACKGROUND);
    }
}
