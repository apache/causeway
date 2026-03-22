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
package org.apache.causeway.viewer.wicket.viewer.integration;

import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.cycle.IRequestCycleListener;
import org.apache.wicket.request.cycle.RequestCycle;

import org.apache.causeway.commons.internal.observation.CausewayObservationInternal;
import org.apache.causeway.commons.internal.observation.CausewayObservationInternal.ObservationProvider;

/**
 * @since 4.0
 */
public record TelemetryStartHandler(
        ObservationProvider observationProvider)
implements IRequestCycleListener {

    public TelemetryStartHandler(final CausewayObservationInternal observationInternal) {
        this(observationInternal.provider(TelemetryStartHandler.class));
    }

    @Override
    public synchronized void onBeginRequest(final RequestCycle requestCycle) {
        if (requestCycle instanceof RequestCycle2 requestCycle2) {
            requestCycle2.observationClosure.startAndOpenScope(
                    observationProvider.get("Apache Wicket Request Cycle")
                .lowCardinalityKeyValue("ck2", "test2"));
        }
    }

    @Override
    public IRequestHandler onException(final RequestCycle requestCycle, final Exception ex) {
        if (requestCycle instanceof RequestCycle2 requestCycle2) {
            requestCycle2.observationClosure.onError(ex);
        }
        return null;
    }

}
