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

import java.util.ArrayList;
import java.util.List;

import org.jspecify.annotations.Nullable;

import org.apache.causeway.applib.services.iactn.Interaction;
import org.apache.causeway.applib.services.iactnlayer.InteractionContext;
import org.apache.causeway.applib.services.iactnlayer.InteractionService;
import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.commons.internal.exceptions._Exceptions.FirstExceptionCollector;

/**
 * Binds an {@link Interaction} (&quot;what&quot; is being executed) with
 * an {@link InteractionContext} (&quot;who&quot; is executing, &quot;when&quot; and &quot;where&quot;).
 *
 * <p> {@link InteractionLayer}s are so called because they may be nested (held in a stack).  For example the
 * {@link org.apache.causeway.applib.services.sudo.SudoService} creates a new temporary layer with a different
 * {@link InteractionContext#getUser() user}, while fixtures that mock the clock switch out the
 * {@link InteractionContext#getClock() clock}.
 *
 * <p> The stack of layers is per-thread, managed by {@link InteractionService} as a thread-local).
 *
 * @since 2.0 {@index}
 */
public record InteractionLayer(
        @Nullable InteractionLayer parent,
        /**
         * Carries an {@link Interaction} through its life-cycle.
         */
        InteractionCarrier interactionCarrier,
        /**
         * WHO is performing this {@link #getInteraction()}, also
         * WHEN and WHERE.
         */
        InteractionContext interactionContext,
        /**
         * @since 4.0
         */
        List<Runnable> onCloseListeners) {
	
	public InteractionLayer(
	        @Nullable InteractionLayer parent,
	        InteractionCarrier interactionCarrier,
	        InteractionContext interactionContext) {
		this(parent, interactionCarrier, interactionContext, new ArrayList<>());
	}
	
	public InteractionLayer addOnCloseListener(Runnable listener) {
		onCloseListeners.add(listener);
		return this;
	}

	public Interaction interaction() {
		return interactionCarrier.interaction();
	}
	
    public boolean isRoot() {
        return parent==null;
    }

    public int parentCount() {
        return parent!=null
            ? 1 + parent.parentCount()
            : 0;
    }

    public int totalLayerCount() {
        return 1 + parentCount();
    }

    public InteractionLayer rootLayer() {
        return parent!=null
            ? parent.rootLayer()
            : this;
    }
    
    public void close(FirstExceptionCollector exCollector) {
    	if(_NullSafe.isEmpty(onCloseListeners)) {
			return;
		}
    	for(var listener : onCloseListeners) {
    		try {
    			listener.run();
    		} catch (Exception e) {
    			exCollector.collect(e);
			}
    	}
    }

    public void closeAll(FirstExceptionCollector exCollector) {
        close(exCollector);
        if(parent!=null) {
            parent.closeAll(exCollector);
        }
    }

}
