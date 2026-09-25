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

import java.util.Objects;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.causeway.core.config.observation.ObservationClosure;
import org.apache.causeway.core.metamodel.context.HasMetaModelContext;

/**
 * Opens a semantic observation for the actual rendering of one Wicket component subtree.
 *
 * @since 2.2
 */
public final class WicketRenderObservationBehavior extends Behavior {

    private static final long serialVersionUID = 1L;

    public static <T extends Component> T addTo(
            final T component,
            final WicketRenderObservationDescriptor descriptor) {
        component.add(new WicketRenderObservationBehavior(descriptor));
        return component;
    }

    private final WicketRenderObservationDescriptor descriptor;
    private transient ObservationClosure activeClosure;
    private transient WicketObservationCoordinator.Admission activeAdmission;

    public WicketRenderObservationBehavior(final WicketRenderObservationDescriptor descriptor) {
        this.descriptor = Objects.requireNonNull(descriptor, "descriptor");
    }

    @Override
    public void beforeRender(final Component component) {
        if(activeClosure != null || activeAdmission != null) {
            throw new IllegalStateException("Wicket render observation lifecycle is already active");
        }
        final HasMetaModelContext context = metamodelContextOf(component);
        if(context == null) {
            return;
        }

        descriptor.nominateSemanticTraceName();

        final WicketObservationCoordinator.Admission admission =
                WicketObservationCoordinator.begin(context, descriptor, getClass());
        if(!admission.isTracked()) {
            return;
        }
        activeAdmission = admission;
        try {
            WicketRenderObservationTracker.register(
                    component.getRequestCycle(), this, admission);
        } catch (RuntimeException | Error ex) {
            clearActiveAdmission();
            admission.onError(ex);
            admission.finish();
            throw ex;
        }
    }

    @Override
    public void afterRender(final Component component) {
        if(activeClosure != null || activeAdmission != null) {
            WicketRenderObservationTracker.complete(component.getRequestCycle(), this);
        }
    }

    private static HasMetaModelContext metamodelContextOf(final Component component) {
        Component candidate = component;
        while(candidate != null) {
            if(candidate instanceof HasMetaModelContext) {
                return (HasMetaModelContext) candidate;
            }
            candidate = candidate.getParent();
        }
        return null;
    }

    void activate(final ObservationClosure closure) {
        activeClosure = closure;
    }

    void clearActiveClosure() {
        activeClosure = null;
    }

    void clearActiveAdmission() {
        activeAdmission = null;
    }

    WicketRenderObservationDescriptor descriptor() {
        return descriptor;
    }

    boolean isActive() {
        return activeClosure != null || activeAdmission != null;
    }
}
