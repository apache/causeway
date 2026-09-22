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

import java.util.function.Consumer;

import org.quartz.JobExecutionContext;
import org.junit.jupiter.api.Test;

import org.apache.causeway.core.config.observation.CausewayTraceClassifier.ExecutionMode;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class RunBackgroundCommandsJobTraceClassificationTest {

    @Test
    void classifiesBeforeReturningWhenPaused() {
        @SuppressWarnings("unchecked")
        final Consumer<ExecutionMode> classifier = mock(Consumer.class);
        final RunBackgroundCommandsJob job = new RunBackgroundCommandsJob(classifier);
        final BackgroundCommandsJobControl jobControl = mock(BackgroundCommandsJobControl.class);
        final JobExecutionContext quartzContext = mock(JobExecutionContext.class);
        job.backgroundCommandsJobControl = jobControl;
        when(jobControl.isPaused()).thenReturn(true);

        job.execute(quartzContext);

        final var inOrder = inOrder(classifier, jobControl);
        inOrder.verify(classifier).accept(ExecutionMode.BACKGROUND);
        inOrder.verify(jobControl).isPaused();
        verifyNoInteractions(quartzContext);
    }

    @Test
    void defaultClassifierIsSafeWithoutAgentWhenPaused() {
        final RunBackgroundCommandsJob job = new RunBackgroundCommandsJob();
        final BackgroundCommandsJobControl jobControl = mock(BackgroundCommandsJobControl.class);
        job.backgroundCommandsJobControl = jobControl;
        when(jobControl.isPaused()).thenReturn(true);

        job.execute(mock(JobExecutionContext.class));
    }
}
