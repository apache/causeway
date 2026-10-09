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

import java.io.Serializable;
import java.util.Objects;

import org.apache.wicket.Component;
import org.apache.wicket.request.cycle.RequestCycle;

import org.apache.causeway.core.config.observation.CausewayObservationIntegration;
import org.apache.causeway.commons.internal.observation.ObservationClosure;
import org.apache.causeway.core.metamodel.context.HasMetaModelContext;
import org.apache.causeway.viewer.wicket.ui.CausewayModuleViewerWicketUi;

/**
 * Keeps object-page construction/configuration in scope through the
 * descendant component tree's {@code onBeforeRender} callbacks.
 *
 * @since 4.0.0
 */
public final class WicketPagePreparationObservation implements Serializable {

    private static final long serialVersionUID = 1L;

    private final WicketRenderObservationDescriptor descriptor;
    private transient ObservationClosure activeClosure;
    private transient RequestCycle requestCycle;

    public WicketPagePreparationObservation(
            final WicketRenderObservationDescriptor descriptor) {
        this.descriptor = Objects.requireNonNull(descriptor, "descriptor");
        if(descriptor.getRegion()
                != WicketRenderObservationDescriptor.Region.PAGE_PREPARATION) {
            throw new IllegalArgumentException(
                    "Page preparation descriptor required");
        }
    }

    public void configure(
            final Component component,
            final Runnable configure) {
        start(component);
        try {
            configure.run();
        } catch (RuntimeException | Error ex) {
            complete(ex);
            throw ex;
        }
    }

    public void beforeRender(final Runnable beforeRender) {
        try {
            beforeRender.run();
        } catch (RuntimeException | Error ex) {
            complete(ex);
            throw ex;
        } finally {
            complete(null);
        }
    }

    public void detach(final Runnable detach) {
        complete(null);
        detach.run();
    }

    public void fail(final Throwable failure) {
        complete(failure);
    }

    boolean isActive() {
        return activeClosure != null;
    }

    private void start(final Component component) {
        if(activeClosure != null || !(component instanceof HasMetaModelContext)) {
            return;
        }
        final CausewayObservationIntegration integration =
                ((HasMetaModelContext) component)
                        .lookupService(CausewayObservationIntegration.class)
                        .orElse(null);
        if(integration == null || integration.isNoop()) {
            return;
        }
        requestCycle = component.getRequestCycle();
        activeClosure = new ObservationClosure().startAndOpenScope(
                descriptor.customize(integration.provider(
                        getClass(),
                        CausewayObservationIntegration.withModuleName(
                                CausewayModuleViewerWicketUi.NAMESPACE))
                        .get(descriptor.getRegion().getObservationName())));
        WicketRenderObservationTracker.register(requestCycle, this, activeClosure);
    }

    private void complete(final Throwable failure) {
        if (activeClosure == null) return;
        if (failure != null) {
            // Preparation includes descendant callbacks, whose normal completion
            // can be skipped on error. The request owns their reverse unwind.
            WicketRenderObservationTracker.cleanup(requestCycle, failure);
        } else {
            WicketRenderObservationTracker.complete(requestCycle, this);
        }
    }

    void clearActiveClosure() {
        activeClosure = null;
        requestCycle = null;
    }
}
