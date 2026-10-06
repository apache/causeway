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
package org.apache.causeway.extensions.commandlog.applib.job;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.apache.causeway.core.config.observation.CausewayTraceClassifier;
import org.apache.causeway.core.config.observation.CausewayTraceClassifier.ExecutionMode;

class RunBackgroundCommandsJobTraceClassificationTest {
    @Test void classifiesBeforePauseAndPreservesFailures() {
        var job = new RunBackgroundCommandsJob();
        job.traceClassifier = mock(CausewayTraceClassifier.class);
        job.backgroundCommandsJobControl = mock(BackgroundCommandsJobControl.class);
        when(job.backgroundCommandsJobControl.isPaused()).thenAnswer(invocation -> {
            verify(job.traceClassifier).classifyCurrentSpan(ExecutionMode.BACKGROUND);
            return true;
        });
        job.execute(null);
        reset(job.backgroundCommandsJobControl, job.traceClassifier);
        var failure = new IllegalStateException("job failure");
        when(job.backgroundCommandsJobControl.isPaused()).thenThrow(failure);
        assertSame(failure, assertThrows(IllegalStateException.class, () -> job.execute(null)));
        verify(job.traceClassifier).classifyCurrentSpan(ExecutionMode.BACKGROUND);
    }
    @Test void noCurrentSpanAllowsSuccessfulRunAndPreservesPendingLookupFailure() {
        var job = new RunBackgroundCommandsJob();
        job.traceClassifier = new CausewayTraceClassifier(io.micrometer.tracing.Tracer.NOOP, "custom.mode");
        job.backgroundCommandsJobControl = mock(BackgroundCommandsJobControl.class);
        job.interactionService = mock(org.apache.causeway.applib.services.iactn.InteractionService.class);
        job.listeners = java.util.List.of();
        when(job.interactionService.callAndCatch(any(), any())).thenReturn(
                org.apache.causeway.commons.functional.Try.success(java.util.List.of()));
        assertDoesNotThrow(() -> job.execute(null));
        var failure = new IllegalStateException("pending lookup failed");
        when(job.interactionService.callAndCatch(any(), any())).thenReturn(
                org.apache.causeway.commons.functional.Try.failure(failure));
        assertSame(failure, assertThrows(IllegalStateException.class, () -> job.execute(null)));
    }
    @Test void noCurrentSpanDoesNotPreventPausedJobExecution() {
        var job = new RunBackgroundCommandsJob();
        job.traceClassifier = new CausewayTraceClassifier(io.micrometer.tracing.Tracer.NOOP, "custom.mode");
        job.backgroundCommandsJobControl = mock(BackgroundCommandsJobControl.class);
        when(job.backgroundCommandsJobControl.isPaused()).thenReturn(true);
        assertDoesNotThrow(() -> job.execute(null));
    }
}
