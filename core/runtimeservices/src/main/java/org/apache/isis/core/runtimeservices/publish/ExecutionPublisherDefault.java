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

package org.apache.isis.core.runtimeservices.publish;

import java.util.List;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Supplier;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.InteractionScope;
import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.applib.services.iactn.Execution;
import org.apache.isis.applib.services.publishing.spi.ExecutionSubscriber;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.having.HasEnabling;
import org.apache.isis.core.interaction.session.InteractionTracker;
import org.apache.isis.core.metamodel.services.publishing.ExecutionPublisher;

import lombok.RequiredArgsConstructor;
import lombok.val;

@Service
@Named("isis.runtimeservices.ExecutionPublisherDefault")
@Order(OrderPrecedence.MIDPOINT)
@Primary
@Qualifier("Default")
@InteractionScope
@RequiredArgsConstructor(onConstructor_ = {@Inject})
//@Log4j2
public class ExecutionPublisherDefault
implements ExecutionPublisher {

    private final List<ExecutionSubscriber> subscribers;
    private final InteractionTracker iaTracker;

    private Can<ExecutionSubscriber> enabledSubscribers = Can.empty();

    @PostConstruct
    public void init() {
        enabledSubscribers = Can.ofCollection(subscribers)
                .filter(HasEnabling::isEnabled);
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

    private final LongAdder suppressionRequestCounter = new LongAdder();

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
