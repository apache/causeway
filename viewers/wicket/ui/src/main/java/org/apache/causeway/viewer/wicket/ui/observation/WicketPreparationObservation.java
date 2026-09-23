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
import java.util.function.Supplier;

import org.apache.wicket.Component;

import org.apache.causeway.core.config.observation.CausewayObservationIntegration;
import org.apache.causeway.core.config.observation.ObservationClosure;
import org.apache.causeway.core.metamodel.context.HasMetaModelContext;
import org.apache.causeway.viewer.wicket.ui.CausewayModuleViewerWicketUi;

/**
 * Keeps semantic Wicket preparation in scope across component configuration
 * and descendant {@code onBeforeRender} callbacks.
 *
 * @since 2.2
 */
public class WicketPreparationObservation implements Serializable {

    private static final long serialVersionUID = 1L;

    private final WicketRenderObservationDescriptor descriptor;
    private transient ObservationClosure activeClosure;

    public WicketPreparationObservation(
            final WicketRenderObservationDescriptor descriptor) {
        this.descriptor = Objects.requireNonNull(descriptor, "descriptor");
        if(!descriptor.getRegion().isPreparation()) {
            throw new IllegalArgumentException("Preparation descriptor required");
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

    public void prepare(
            final Component component,
            final Runnable preparation) {
        start(component);
        beforeRender(preparation);
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

    public static void observe(
            final HasMetaModelContext context,
            final WicketRenderObservationDescriptor descriptor,
            final Runnable work) {
        Objects.requireNonNull(work, "work");
        observe(context, descriptor, () -> {
            work.run();
            return null;
        });
    }

    public static <T> T observe(
            final HasMetaModelContext context,
            final WicketRenderObservationDescriptor descriptor,
            final Supplier<T> work) {
        Objects.requireNonNull(context, "context");
        Objects.requireNonNull(descriptor, "descriptor");
        Objects.requireNonNull(work, "work");
        if(!descriptor.getRegion().isPreparation()) {
            throw new IllegalArgumentException("Preparation descriptor required");
        }

        final CausewayObservationIntegration integration = context
                .lookupService(CausewayObservationIntegration.class)
                .orElse(null);
        if(integration == null || integration.isNoop()) {
            return work.get();
        }

        final ObservationClosure closure = start(integration, descriptor);
        try {
            return work.get();
        } catch (RuntimeException | Error ex) {
            closure.onError(ex);
            throw ex;
        } finally {
            closure.close();
        }
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
        activeClosure = start(integration, descriptor);
    }

    private static ObservationClosure start(
            final CausewayObservationIntegration integration,
            final WicketRenderObservationDescriptor descriptor) {
        return new ObservationClosure().startAndOpenScope(
                descriptor.customize(integration.provider(
                        WicketPreparationObservation.class,
                        CausewayObservationIntegration.withModuleName(
                                CausewayModuleViewerWicketUi.NAMESPACE))
                        .get(descriptor.getRegion().getObservationName())));
    }

    private void complete(final Throwable failure) {
        if(activeClosure == null) {
            return;
        }
        final ObservationClosure closure = activeClosure;
        activeClosure = null;
        if(failure != null) {
            closure.onError(failure);
        }
        closure.close();
    }
}
