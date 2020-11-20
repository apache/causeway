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

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.IsisInteractionScope;
import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.applib.services.iactn.Interaction;
import org.apache.isis.applib.services.publishing.spi.ExecutionSubscriber;
import org.apache.isis.core.metamodel.services.publishing.ExecutionDispatcher;

import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * Wrapper around {@link ExecutionSubscriber}.  Is a no-op if there is no injected service.
 */
@Service
@Named("isisRuntimeServices.ExecutionDispatcherDefault")
@Order(OrderPrecedence.MIDPOINT)
@Primary
@Qualifier("Default")
@IsisInteractionScope
@RequiredArgsConstructor(onConstructor_ = {@Inject})
//@Log4j2
public class ExecutionDispatcherDefault 
implements ExecutionDispatcher {

    private final List<ExecutionSubscriber> executionListeners;

    @Override
    public void dispatchActionInvoking(final Interaction.Execution<?,?> execution) {
        notifyListeners(execution);
    }

    @Override
    public void dispatchPropertyChanging(final Interaction.Execution<?,?> execution) {
        notifyListeners(execution);
    }
    
    @Override
    public <T> T withDispatchSuppressed(final Supplier<T> block) {
        try {
            suppressionRequestCounter.increment();
            return block.get();
        } finally {
            suppressionRequestCounter.decrement();
        }
    }

    // -- HELPERS

    private void notifyListeners(final Interaction.Execution<?,?> execution) {
        if(isSuppressed()) {
            return;
        }
        for (val executionListener : executionListeners) {
            executionListener.onExecution(execution);
        }
    }

    private final LongAdder suppressionRequestCounter = new LongAdder();
    
    private boolean isSuppressed() {
        return executionListeners == null 
                || executionListeners.isEmpty() 
                || suppressionRequestCounter.intValue() > 0;
    }
    

}
