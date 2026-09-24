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
package org.apache.causeway.viewer.wicket.ui.observation;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.LongSupplier;

import org.apache.wicket.MetaDataKey;
import org.apache.wicket.request.cycle.RequestCycle;

import io.micrometer.observation.Observation;

import org.apache.causeway.core.config.CausewayConfiguration.Viewer.Wicket.Observation.Detail;
import org.apache.causeway.core.config.observation.CausewayObservationIntegration;
import org.apache.causeway.core.config.observation.ObservationClosure;
import org.apache.causeway.core.metamodel.context.HasMetaModelContext;
import org.apache.causeway.viewer.wicket.ui.CausewayModuleViewerWicketUi;

/**
 * Coordinates Causeway's semantic Wicket observations for one request.
 *
 * @since 2.2
 */
final class WicketObservationCoordinator {

    static final String SUPPRESSED_DETAIL_TAG = "causeway.wicket.suppressed.detail";
    static final String SUPPRESSED_BUDGET_TAG = "causeway.wicket.suppressed.budget";
    static final String SUPPRESSED_CHILD_TAG = "causeway.wicket.collection.suppressed.children";
    static final String ROW_COUNT_TAG = "causeway.wicket.collection.row.count";
    static final String ROW_TOTAL_NANOS_TAG = "causeway.wicket.collection.row.duration.total.nanos";
    static final String ROW_MAX_NANOS_TAG = "causeway.wicket.collection.row.duration.max.nanos";
    static final String CELL_COUNT_TAG = "causeway.wicket.collection.logical-cell.count";

    private static final MetaDataKey<State> STATE_KEY = new MetaDataKey<>() {
        private static final long serialVersionUID = 1L;
    };

    private WicketObservationCoordinator() {}

    static Admission begin(
            final HasMetaModelContext context,
            final WicketRenderObservationDescriptor descriptor,
            final Class<?> beanType) {
        final CausewayObservationIntegration integration = context
                .lookupService(CausewayObservationIntegration.class)
                .orElse(null);
        if(integration == null || integration.isNoop()) {
            return Admission.inactive();
        }

        final RequestCycle requestCycle = RequestCycle.get();
        final State state = requestCycle != null
                ? stateFor(requestCycle, settings(context))
                : new State(settings(context), System::nanoTime);
        return state.begin(integration, descriptor, beanType);
    }

    static void cleanup(final RequestCycle requestCycle, final Throwable failure) {
        if(requestCycle == null) {
            return;
        }
        final State state = requestCycle.getMetaData(STATE_KEY);
        if(state != null) {
            state.cleanup(failure);
            requestCycle.setMetaData(STATE_KEY, null);
        }
    }

    private static State stateFor(
            final RequestCycle requestCycle,
            final Settings settings) {
        State state = requestCycle.getMetaData(STATE_KEY);
        if(state == null) {
            state = new State(settings, System::nanoTime);
            requestCycle.setMetaData(STATE_KEY, state);
        }
        return state;
    }

    private static Settings settings(final HasMetaModelContext context) {
        try {
            final org.apache.causeway.core.config.CausewayConfiguration.Viewer.Wicket.Observation config =
                    context.getConfiguration().getViewer().getWicket().getObservation();
            return new Settings(config.getDetail(), config.getMaxSpansPerRequest());
        } catch (RuntimeException | LinkageError ex) {
            // Lightweight Wicket tests and detached components have no metamodel singleton.
            return Settings.compatibilityDefaults();
        }
    }

    static final class Admission {
        private static final Admission INACTIVE = new Admission();

        private State owner;
        private WicketRenderObservationDescriptor descriptor;
        private Observation observation;
        private ObservationClosure closure;
        private Aggregate aggregate;
        private Aggregate timedAggregate;
        private long startedAtNanos;
        private long detailSuppressed;
        private long budgetSuppressed;
        private boolean finished;

        private Admission() {}

        private Admission(
                final State owner,
                final WicketRenderObservationDescriptor descriptor,
                final Observation observation,
                final ObservationClosure closure,
                final Aggregate aggregate,
                final Aggregate timedAggregate,
                final long startedAtNanos) {
            this.owner = owner;
            this.descriptor = descriptor;
            this.observation = observation;
            this.closure = closure;
            this.aggregate = aggregate;
            this.timedAggregate = timedAggregate;
            this.startedAtNanos = startedAtNanos;
        }

        static Admission inactive() {
            return INACTIVE;
        }

        boolean isEmitted() {
            return closure != null && !finished;
        }

        boolean isTracked() {
            return owner != null && !finished;
        }

        void onError(final Throwable failure) {
            if(closure != null) {
                closure.onError(failure);
            }
        }

        void finish() {
            if(owner != null) {
                owner.finish(this);
            }
        }

        private void attachFinalMetadata() {
            if(observation == null) {
                return;
            }
            addIfPositive(SUPPRESSED_DETAIL_TAG, detailSuppressed);
            addIfPositive(SUPPRESSED_BUDGET_TAG, budgetSuppressed);
            if(aggregate != null) {
                add(ROW_COUNT_TAG, aggregate.rowCount);
                add(ROW_TOTAL_NANOS_TAG, aggregate.rowTotalNanos);
                add(ROW_MAX_NANOS_TAG, aggregate.rowMaxNanos);
                if(descriptor.getRegion() == WicketRenderObservationDescriptor.Region.COLLECTION) {
                    add(CELL_COUNT_TAG, aggregate.logicalCellCount);
                }
                addIfPositive(SUPPRESSED_CHILD_TAG, aggregate.suppressedChildCount);
            }
        }

        private void addIfPositive(final String key, final long value) {
            if(value > 0) {
                add(key, value);
            }
        }

        private void add(final String key, final long value) {
            observation.highCardinalityKeyValue(key, Long.toString(value));
        }
    }

    static final class State {
        private final Settings settings;
        private final LongSupplier ticker;
        private final Deque<Admission> active = new ArrayDeque<>();
        private int admittedCount;

        State(final Settings settings, final LongSupplier ticker) {
            this.settings = settings;
            this.ticker = ticker;
        }

        Admission begin(
                final CausewayObservationIntegration integration,
                final WicketRenderObservationDescriptor descriptor) {
            return begin(integration, descriptor, WicketObservationCoordinator.class);
        }

        Admission begin(
                final CausewayObservationIntegration integration,
                final WicketRenderObservationDescriptor descriptor,
                final Class<?> beanType) {
            final Aggregate timedAggregate = applicableAggregate(descriptor);
            final long startedAtNanos = timedAggregate != null && descriptor.isRowCallback()
                    ? ticker.getAsLong()
                    : 0L;
            if(timedAggregate != null && descriptor.isLogicalCellCallback()) {
                timedAggregate.logicalCellCount = increment(
                        timedAggregate.logicalCellCount);
            }

            final Suppression suppression = suppressionFor(descriptor);
            if(suppression != null) {
                recordSuppression(suppression, descriptor, timedAggregate);
                return new Admission(this, descriptor, null, null, null,
                        timedAggregate, startedAtNanos);
            }

            final Observation observation = descriptor.customize(
                    integration.provider(
                            beanType,
                            CausewayObservationIntegration.withModuleName(
                                    CausewayModuleViewerWicketUi.NAMESPACE))
                            .get(descriptor.getRegion().getObservationName()));
            final ObservationClosure closure = new ObservationClosure()
                    .startAndOpenScope(observation);
            if(admittedCount < Integer.MAX_VALUE) {
                admittedCount++;
            }
            final Aggregate aggregate = descriptor.isCollectionAggregate()
                    ? new Aggregate()
                    : null;
            final Admission admission = new Admission(this, descriptor, observation,
                    closure, aggregate, timedAggregate, startedAtNanos);
            active.push(admission);
            return admission;
        }

        private Suppression suppressionFor(
                final WicketRenderObservationDescriptor descriptor) {
            if(settings.detail.ordinal() < descriptor.minimumDetail().ordinal()) {
                return Suppression.DETAIL;
            }
            if(settings.maximum == 0) {
                return null;
            }
            if(admittedCount >= settings.maximum) {
                return Suppression.BUDGET;
            }
            if(!descriptor.isStructural()
                    && admittedCount >= settings.maximum - settings.structuralReserve) {
                return Suppression.BUDGET;
            }
            return null;
        }

        private void recordSuppression(
                final Suppression suppression,
                final WicketRenderObservationDescriptor descriptor,
                final Aggregate aggregate) {
            final Admission carrier = nearestStructural();
            if(carrier != null) {
                if(suppression == Suppression.DETAIL) {
                    carrier.detailSuppressed = increment(carrier.detailSuppressed);
                } else {
                    carrier.budgetSuppressed = increment(carrier.budgetSuppressed);
                }
            }
            if(aggregate != null
                    && (descriptor.isRowCallback() || descriptor.isLogicalCellCallback())) {
                aggregate.suppressedChildCount = increment(
                        aggregate.suppressedChildCount);
            }
        }

        private Admission nearestStructural() {
            for (Admission admission : active) {
                if(admission.descriptor.isStructural()) {
                    return admission;
                }
            }
            return null;
        }

        private Aggregate applicableAggregate(
                final WicketRenderObservationDescriptor descriptor) {
            final WicketRenderObservationDescriptor.Region required =
                    descriptor.getRegion() == WicketRenderObservationDescriptor.Region.ROW_PREPARATION
                            ? WicketRenderObservationDescriptor.Region.COLLECTION_PREPARATION
                            : descriptor.getRegion() == WicketRenderObservationDescriptor.Region.ROW
                                    || descriptor.isLogicalCellCallback()
                                    ? WicketRenderObservationDescriptor.Region.COLLECTION
                                    : null;
            if(required == null) {
                return null;
            }
            for (Admission admission : active) {
                if(admission.descriptor.getRegion() == required) {
                    return admission.aggregate;
                }
            }
            return null;
        }

        synchronized void finish(final Admission admission) {
            if(admission.finished) {
                return;
            }
            admission.finished = true;
            if(admission.timedAggregate != null && admission.descriptor.isRowCallback()) {
                final long duration = Math.max(0L,
                        ticker.getAsLong() - admission.startedAtNanos);
                admission.timedAggregate.recordRow(duration);
            }
            active.remove(admission);
            admission.attachFinalMetadata();
            if(admission.closure != null) {
                admission.closure.close();
            }
        }

        synchronized void cleanup(final Throwable failure) {
            while(!active.isEmpty()) {
                final Admission admission = active.peek();
                admission.onError(failure);
                finish(admission);
            }
        }

        private static long increment(final long value) {
            return value < Long.MAX_VALUE ? value + 1L : Long.MAX_VALUE;
        }
    }

    private static final class Aggregate {
        private long rowCount;
        private long rowTotalNanos;
        private long rowMaxNanos;
        private long logicalCellCount;
        private long suppressedChildCount;

        private void recordRow(final long durationNanos) {
            rowCount = State.increment(rowCount);
            rowTotalNanos = durationNanos > Long.MAX_VALUE - rowTotalNanos
                    ? Long.MAX_VALUE
                    : rowTotalNanos + durationNanos;
            rowMaxNanos = Math.max(rowMaxNanos, durationNanos);
        }
    }

    static final class Settings {
        private final Detail detail;
        private final int maximum;
        private final int structuralReserve;

        Settings(final Detail detail, final int maximum) {
            if(maximum < 0) {
                throw new IllegalArgumentException("maximum must not be negative");
            }
            this.detail = detail != null ? detail : Detail.MEMBERS;
            this.maximum = maximum;
            this.structuralReserve = maximum > 0
                    ? Math.min(16, Math.max(1, (maximum + 9) / 10))
                    : 0;
        }

        static Settings compatibilityDefaults() {
            return new Settings(Detail.MEMBERS, 0);
        }
    }

    private enum Suppression {
        DETAIL,
        BUDGET
    }
}
