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
package org.apache.causeway.core.runtimeservices.publish;

import java.util.List;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Supplier;

import javax.annotation.PostConstruct;
import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.annotation.InteractionScope;
import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.services.iactn.Execution;
import org.apache.causeway.applib.services.iactnlayer.InteractionLayerTracker;
import org.apache.causeway.applib.services.publishing.spi.ExecutionSubscriber;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.having.HasEnabling;
import org.apache.causeway.core.metamodel.services.publishing.ExecutionPublisher;
import org.apache.causeway.core.runtimeservices.CausewayModuleCoreRuntimeServices;

import lombok.RequiredArgsConstructor;
import lombok.val;

@Service
@Named(CausewayModuleCoreRuntimeServices.NAMESPACE + ".ExecutionPublisherDefault")
@Priority(PriorityPrecedence.MIDPOINT)
@Qualifier("Default")
@InteractionScope
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class ExecutionPublisherDefault
implements ExecutionPublisher {

    private final List<ExecutionSubscriber> subscribers;
    private final InteractionLayerTracker iaTracker;

    private Can<ExecutionSubscriber> enabledSubscribers = Can.empty();
    /**
     * this is the reason that this service is @InteractionScope'd
     */
    private final LongAdder suppressionRequestCounter = new LongAdder();

    @PostConstruct
    public void init() {
        enabledSubscribers = Can.ofCollection(subscribers)
                .filter(HasEnabling::isEnabled);
    }

    @Override
    public void destroy() throws Exception {
        suppressionRequestCounter.reset();
    }

    @Override
    public void publishActionInvocation(final Execution<?,?> execution) {
        notifySubscribers(execution);
    }

    @Override
    public void publishPropertyEdit(final Execution<?,?> execution) {
        notifySubscribers(execution);
    }

    @Override
    public <T> T withPublishingSuppressed(final Supplier<T> block) {
        try {
            suppressionRequestCounter.increment();
            return block.get();
        } finally {
            suppressionRequestCounter.decrement();
        }
    }

    // -- HELPER

    private void notifySubscribers(final Execution<?,?> execution) {

        val handle = _Xray.enterExecutionPublishing(
                iaTracker,
                execution,
                enabledSubscribers,
                this::getCannotPublishReason);

        if(canPublish()) {
            for (val subscriber : enabledSubscribers) {
                subscriber.onExecution(execution);
            }
        }

        _Xray.exitPublishing(handle);

    }


    private boolean canPublish() {
        return enabledSubscribers.isNotEmpty()
                && suppressionRequestCounter.longValue() < 1L;
    }

    // x-ray support
    private @Nullable String getCannotPublishReason() {
        return enabledSubscribers.isEmpty()
                ? "no subscribers"
                : suppressionRequestCounter.longValue() > 0L
                        ? String.format(
                                "suppressed for block of executable code\nsuppression request depth %d",
                                suppressionRequestCounter.longValue())
                        : null;
    }

}
