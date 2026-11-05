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
import org.apache.causeway.core.metamodel.context.HasMetaModelContext;

/**
 * Opens a semantic observation for the actual rendering of one Wicket component subtree.
 *
 * @since 4.0.0
 */
public final class WicketRenderObservationBehavior extends Behavior {

    private static final long serialVersionUID = 1L;

    public static <T extends Component> T addTo(
            final T component,
            final WicketRenderObservationDescriptor descriptor) {
        component.add(new WicketRenderObservationBehavior(descriptor));
        return component;
    }

    /** Table actions retain their original UI Where semantics; telemetry eligibility is checked at render time. */
    public static <T extends Component> T addToParentedTableMember(final T component,
            final WicketRenderObservationDescriptor descriptor) {
        component.add(new WicketRenderObservationBehavior(descriptor, true));
        return component;
    }

    private final WicketRenderObservationDescriptor descriptor;
    private final boolean parentedTableOnly;
    private transient WicketObservationCoordinator.Admission activeClosure;

    public WicketRenderObservationBehavior(final WicketRenderObservationDescriptor descriptor) {
        this(descriptor, false);
    }

    private WicketRenderObservationBehavior(final WicketRenderObservationDescriptor descriptor, final boolean parentedTableOnly) {
        this.descriptor = Objects.requireNonNull(descriptor, "descriptor");
        this.parentedTableOnly = parentedTableOnly;
    }

    @Override
    public void beforeRender(final Component component) {
        if(activeClosure != null) {
            throw new IllegalStateException("Wicket render observation lifecycle is already active");
        }
        if(parentedTableOnly) {
            final var table = component.findParent(org.apache.causeway.viewer.wicket.ui.components.table.CausewayAjaxDataTable.class);
            if(table == null || !table.isObservedCollection()) return;
        }
        final HasMetaModelContext context = contextOf(component);
        if(context == null) return;
        descriptor.nominateSemanticTraceName();
        final var admission = WicketObservationCoordinator.begin(context, descriptor, getClass());
        if(!admission.isTracked()) return;
        activeClosure = admission;
        WicketRenderObservationTracker.register(component.getRequestCycle(), this, admission);
    }

    static HasMetaModelContext contextOf(final Component component) {
        for(Component current = component; current != null; current = current.getParent()) {
            if(current instanceof HasMetaModelContext context) return context;
        }
        return null;
    }

    @Override
    public void afterRender(final Component component) {
        if(activeClosure != null) {
            WicketRenderObservationTracker.complete(component.getRequestCycle(), this);
        }
    }

    @Override
    public void onException(final Component component, final RuntimeException failure) {
        WicketRenderObservationTracker.cleanup(component.getRequestCycle(), failure);
    }

    @Override
    public void detach(final Component component) {
        if (activeClosure != null) WicketRenderObservationTracker.complete(component.getRequestCycle(), this);
    }


    void activate(final org.apache.causeway.commons.internal.observation.ObservationClosure closure) {
        activeClosure = WicketObservationCoordinator.Admission.adopt(closure);
    }

    void clearActiveClosure() {
        activeClosure = null;
    }

    WicketRenderObservationDescriptor descriptor() {
        return descriptor;
    }

    boolean isActive() {
        return activeClosure != null;
    }
}
