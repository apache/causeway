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

import org.apache.causeway.core.metamodel.context.HasMetaModelContext;

/**
 * Observes synchronous initialization work or keeps semantic Wicket preparation
 * in scope across component configuration and descendant {@code onBeforeRender} callbacks.
 *
 * @since 2.2
 */
public class WicketPreparationObservation implements Serializable {

    private static final long serialVersionUID = 1L;

    private final WicketRenderObservationDescriptor descriptor;
    private transient WicketObservationCoordinator.Admission activeAdmission;

    public WicketPreparationObservation(
            final WicketRenderObservationDescriptor descriptor) {
        this.descriptor = Objects.requireNonNull(descriptor, "descriptor");
        if(!descriptor.getRegion().isPreparation()) {
            throw new IllegalArgumentException(
                    "Initialization or preparation descriptor required");
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
            throw new IllegalArgumentException(
                    "Initialization or preparation descriptor required");
        }

        final WicketObservationCoordinator.Admission admission =
                WicketObservationCoordinator.begin(
                        context, descriptor, WicketPreparationObservation.class);
        try {
            return work.get();
        } catch (RuntimeException | Error ex) {
            admission.onError(ex);
            throw ex;
        } finally {
            admission.finish();
        }
    }

    boolean isActive() {
        return activeAdmission != null;
    }

    private void start(final Component component) {
        if(activeAdmission != null || !(component instanceof HasMetaModelContext)) {
            return;
        }
        final WicketObservationCoordinator.Admission admission =
                WicketObservationCoordinator.begin(
                        (HasMetaModelContext) component,
                        descriptor,
                        WicketPreparationObservation.class);
        if(admission.isTracked()) {
            activeAdmission = admission;
        }
    }

    private void complete(final Throwable failure) {
        if(activeAdmission == null) {
            return;
        }
        final WicketObservationCoordinator.Admission admission = activeAdmission;
        activeAdmission = null;
        if(failure != null) {
            admission.onError(failure);
        }
        admission.finish();
    }
}
