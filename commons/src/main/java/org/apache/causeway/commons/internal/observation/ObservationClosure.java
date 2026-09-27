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
package org.apache.causeway.commons.internal.observation;

import java.util.function.Supplier;

import org.jspecify.annotations.Nullable;

import lombok.Getter;
import lombok.experimental.Accessors;

import io.micrometer.common.KeyValue;
import io.micrometer.observation.Observation;
import io.micrometer.observation.Observation.Scope;

/**
 * Helps if start and stop of an {@link Observation} happen in different code locations.
 */
@Getter @Accessors(fluent = true)
public final class ObservationClosure implements AutoCloseable {

	public static final KeyValue DISCARD_KEY = KeyValue.of("causeway.discard", "");
	
    private Observation observation;
    private Scope scope;
    @Getter(lombok.AccessLevel.NONE)
    private Throwable failure;

    public ObservationClosure startAndOpenScope(final Observation observation) {
        if(observation==null) return this;
        if (this.observation != null) {
            throw new IllegalStateException("Observation lifecycle is already active");
        }
        failure = null;
        this.observation = observation.start();
        try {
            this.scope = this.observation.openScope();
        } catch (RuntimeException | Error ex) {
            onError(ex);
            close();
            throw ex;
        }
        return this;
    }

    @Override
    public void close() {
        final var scopeToClose = scope;
        final var observationToStop = observation;
        final var originalFailure = failure;
        scope = null;
        observation = null;
        failure = null;
        Throwable cleanupFailure = null;
        try {
            if (scopeToClose != null) scopeToClose.close();
        } catch (RuntimeException | Error ex) {
            cleanupFailure = ex;
        }
        try {
            if (observationToStop != null) observationToStop.stop();
        } catch (RuntimeException | Error ex) {
            if (cleanupFailure == null) cleanupFailure = ex;
            else suppress(cleanupFailure, ex);
        }
        if (cleanupFailure != null) {
            if (originalFailure != null) suppress(originalFailure, cleanupFailure);
            else if (cleanupFailure instanceof RuntimeException ex) throw ex;
            else throw (Error) cleanupFailure;
        }
    }

    public void onError(final Exception ex) {
        onError((Throwable) ex);
    }

    public void onError(final Throwable ex) {
        if (observation == null || ex == null) return;
        if (failure == null) failure = ex;
        try {
            observation.error(ex);
        } catch (RuntimeException | Error reportingFailure) {
            suppress(ex, reportingFailure);
        }
    }

    private static void suppress(final Throwable primary, final Throwable secondary) {
        if (primary != secondary) primary.addSuppressed(secondary);
    }

    public ObservationClosure tag(final String key, @Nullable final Supplier<? extends Object> valueSupplier) {
        if(observation==null || valueSupplier == null) return this;
        try {
            observation.highCardinalityKeyValue(key, "" + valueSupplier.get());
        } catch (Exception e) {
            observation.highCardinalityKeyValue(key, "EXCEPTION: " + e.getMessage());
        }
        return this;
    }

    public void discard() {
        try {
            discard(this.observation);
        } catch (RuntimeException | Error ex) {
            onError(ex);
            throw ex;
        } finally {
            close();
        }
    }
    
    // -- UTILITY
    
    /**
     * Denies span export, in collaboration with a Spring registered {@link DiscardedSpanExportingPredicate}.
     */
    public static void discard(@Nullable final Observation obs) {
        if(obs == null)
            return;
        obs.lowCardinalityKeyValue(DISCARD_KEY);
    }

}
