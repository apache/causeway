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
package org.apache.causeway.core.metamodel.execution;

import java.util.Optional;
import java.util.function.Predicate;

import org.jspecify.annotations.Nullable;

import org.apache.causeway.applib.services.iactnlayer.InteractionContext;
import org.apache.causeway.commons.internal.exceptions._Exceptions.FirstExceptionCollector;
import org.apache.causeway.commons.internal.observation.ObservationClosure;

import io.micrometer.observation.Observation;

public final class InteractionLayerStack {

    // TODO: reading the javadoc for TransactionSynchronizationManager and looking at the implementations
    //  of TransactionSynchronization (in particular SpringSessionSynchronization), I suspect that this
    //  ThreadLocal would be considered bad practice and instead should be managed using the TransactionSynchronization mechanism.
    private final ThreadLocal<InteractionLayer> threadLocalLayer = new ThreadLocal<>();

    public Optional<InteractionLayer> currentLayer() {
        return Optional.ofNullable(threadLocalLayer.get());
    }
    
    public InteractionLayer push(
            final ExecutionContext executionContext,
            final InteractionContext interactionContext,
            final Observation observation) {
        var parent = currentLayer().orElse(null);
        var interactionCarrier = currentLayer()
                .map(InteractionLayer::interactionCarrier)
                .orElseGet(()->new InteractionCarrierDefault(executionContext));
        
        @SuppressWarnings("resource")
        var newLayer = new InteractionLayer(parent, interactionCarrier, interactionContext)
        	.addOnCloseListener(new ObservationClosure().startAndOpenScope(observation)::close);
        threadLocalLayer.set(newLayer);
        return newLayer;
    }
    
	public InteractionLayer pushForTesting(InteractionCarrier interactionCarrier, InteractionContext interactionContext) {
        var parent = currentLayer().orElse(null);
        var newLayer = new InteractionLayer(parent, interactionCarrier, interactionContext);
        threadLocalLayer.set(newLayer);
        return newLayer;
	}

    public void clear() {
    	try {
    		var layer = peek();
	    	if(layer!=null) {
	    		var exColl = new FirstExceptionCollector();
	            layer.closeAll(exColl);
	            exColl.rethrow();
	    	}
    	} finally {
    		threadLocalLayer.remove();
    	}
    }

    public boolean isEmpty() {
        return threadLocalLayer.get()==null;
    }

    public int size() {
        return currentLayer()
            .map(InteractionLayer::totalLayerCount)
            .orElse(0);
    }

    @Nullable
    public InteractionLayer peek() {
        return threadLocalLayer.get();
    }

    @Nullable
    public InteractionLayer pop() {
        var current = threadLocalLayer.get();
        if(current==null) {
			return null;
		}

        var newTop = current.parent();
        
        var exColl = new FirstExceptionCollector();
        current.close(exColl);
        if(exColl.hasException()) {
        	// close the entire stack, only then re-throw
        	current.closeAll(exColl);
        	threadLocalLayer.remove();
        	exColl.rethrow();
        }
        
        return set(newTop);
    }

    public void popWhile(final Predicate<InteractionLayer> condition) {
        while(!isEmpty()) {
            if(!condition.test(peek())) {
				return;
			}
            pop();
        }
    }

    // -- HELPER

    private InteractionLayer set(@Nullable final InteractionLayer layer) {
        if(layer != null) {
            threadLocalLayer.set(layer);
        } else {
            threadLocalLayer.remove();
        }
        return layer;
    }

}
