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

package org.apache.isis.core.runtime.persistence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ResolveState;

public class PersistorUtil {

    private static final Logger LOG = LoggerFactory.getLogger(PersistorUtil.class);

    private PersistorUtil() {
    }

    // //////////////////////////////////////////////////////////////////
    // update resolve state
    // //////////////////////////////////////////////////////////////////

    public static void startResolvingOrUpdating(ObjectAdapter objectAdapter) {
        if(objectAdapter.canTransitionToResolving()) {
            startResolving(objectAdapter);
        } else {
            startUpdating(objectAdapter);
        }
    }

    public static void startResolving(final ObjectAdapter adapter) {
        changeTo(adapter, ResolveState.RESOLVING);
    }

    public static void startUpdating(final ObjectAdapter adapter) {
        changeTo(adapter, ResolveState.UPDATING);
    }

    private static void changeTo(final ObjectAdapter adapter, final ResolveState state) {
        changeTo("start ", adapter, state);
    }

    /**
     * Marks the specified object as loaded: resolved, partly resolve or updated
     * as specified by the second parameter. Attempting to specify any other
     * state throws a run time exception.
     */
    public static void toEndState(final ObjectAdapter adapter) {
        changeTo("end ", adapter, adapter.getResolveState().getEndState());
    }

    private static void changeTo(final String direction, final ObjectAdapter adapter, final ResolveState state) {
        if (LOG.isTraceEnabled()) {
            LOG.trace(direction + adapter + " as " + state.name());
        }
        adapter.changeState(state);
    }


}
