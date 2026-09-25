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

import io.micrometer.observation.Observation;
import io.micrometer.observation.Observation.Scope;

/**
 * Owns an observation whose start and stop occur in different framework callbacks.
 *
 * @since 2.2
 */
public final class ObservationClosure implements AutoCloseable {

    private Observation observation;
    private Scope scope;

    public synchronized ObservationClosure startAndOpenScope(final Observation observation) {
        if (observation == null) {
            return this;
        }
        if (this.observation != null || this.scope != null) {
            throw new IllegalStateException("Observation lifecycle is already active");
        }

        this.observation = observation.start();
        try {
            this.scope = this.observation.openScope();
        } catch (RuntimeException | Error ex) {
            this.observation.error(ex);
            close();
            throw ex;
        }
        return this;
    }

    public synchronized void onError(final Throwable failure) {
        if (observation != null && failure != null) {
            observation.error(failure);
        }
    }

    @Override
    public synchronized void close() {
        final Scope scopeToClose = scope;
        final Observation observationToStop = observation;
        scope = null;
        observation = null;

        try {
            if (scopeToClose != null) {
                scopeToClose.close();
            }
        } finally {
            if (observationToStop != null) {
                observationToStop.stop();
            }
        }
    }
}
