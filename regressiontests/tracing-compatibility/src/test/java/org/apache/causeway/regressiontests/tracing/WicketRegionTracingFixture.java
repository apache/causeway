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
package org.apache.causeway.regressiontests.tracing;

import java.util.Optional;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.Markup;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.request.cycle.IRequestCycleListener;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.util.tester.WicketTester;

import org.apache.causeway.applib.services.iactn.InteractionContext;
import org.apache.causeway.applib.services.user.UserMemento;
import org.apache.causeway.core.config.observation.CausewayObservationIntegration;
import org.apache.causeway.core.metamodel.context.HasMetaModelContext;
import org.apache.causeway.core.runtimeservices.ia.InteractionServiceDefault;
import org.apache.causeway.viewer.wicket.ui.observation.WicketPagePreparationObservation;
import org.apache.causeway.viewer.wicket.ui.observation.WicketRenderObservationBehavior;
import org.apache.causeway.viewer.wicket.ui.observation.WicketRenderObservationDescriptor;
import org.apache.causeway.viewer.wicket.ui.observation.WicketRenderObservationTracker;
import org.apache.causeway.viewer.wicket.viewer.integration.RequestCycle2;
import org.apache.causeway.viewer.wicket.viewer.integration.TelemetryStartHandler;
import org.apache.causeway.viewer.wicket.viewer.integration.WebRequestCycleForCauseway;

/** Real Wicket component and Ajax callbacks using the production observation helpers. */
final class WicketRegionTracingFixture {
    private static final String TYPE = "fixture.UpperCaseOwner";

    static void render(CausewayObservationIntegration integration, InteractionServiceDefault interactions,
            Runnable memberWork, boolean fail) {
        var enclosingObservation = integration.observationRegistry().getCurrentObservation();
        var tester = new WicketTester();
        var userContext = InteractionContext.ofUserWithSystemDefaults(UserMemento.ofName("sentinel-user"));
        var application = tester.getApplication();
        application.setRequestCycleProvider(RequestCycle2::new);
        application.getRequestCycleListeners().add(new TelemetryStartHandler(integration));
        application.getRequestCycleListeners().add(new IRequestCycleListener() {
            public void onBeginRequest(RequestCycle cycle) { interactions.openInteraction(userContext); }
            public org.apache.wicket.request.IRequestHandler onException(RequestCycle cycle, Exception ex) {
                WicketRenderObservationTracker.cleanup(cycle, ex);
                return null;
            }
            public void onEndRequest(RequestCycle cycle) {
                new WebRequestCycleForCauseway(interactions, null, null, null, null).onEndRequest(cycle);
            }
        });
        try {
            var page = new PreparedRegion("component", integration);
            var fieldset = new Region("fieldset", integration,
                    WicketRenderObservationDescriptor.fieldset(TYPE, "identity"));
            var property = new Region("property", integration,
                    WicketRenderObservationDescriptor.property(TYPE, TYPE + "#name")) {
                @Override protected void onRender() {
                    interactions.call(userContext, () -> {
                        memberWork.run();
                        if (fail) throw new IllegalStateException("Wicket rendering failed");
                        return null;
                    });
                    super.onRender();
                }
            };
            property.setOutputMarkupId(true);
            fieldset.add(property);
            var refresh = new AjaxLink<Void>("refresh") {
                public void onClick(AjaxRequestTarget target) { target.add(property); }
            };
            page.add(fieldset, refresh,
                    new Region("collection", integration, WicketRenderObservationDescriptor.collection(TYPE, TYPE + "#items")),
                    new Region("action", integration, WicketRenderObservationDescriptor.action(TYPE, TYPE + "#updateName(java.lang.String)")),
                    new Region("prompt", integration, WicketRenderObservationDescriptor.actionPrompt(TYPE, TYPE + "#updateName(java.lang.String)", "updateName")));
            tester.startComponentInPage(page, Markup.of("<div wicket:id='component'>"
                    + "<div wicket:id='fieldset'><span wicket:id='property'></span></div>"
                    + "<div wicket:id='collection'></div><a wicket:id='action'>action</a>"
                    + "<div wicket:id='prompt'></div><a wicket:id='refresh'>refresh</a></div>"));
            tester.executeAjaxEvent(refresh, "click");
        } catch (RuntimeException failure) {
            if (!fail) throw failure;
            throw new IllegalStateException("Expected Wicket render failure", failure);
        } finally {
            tester.destroy();
            if (integration.observationRegistry().getCurrentObservation() != enclosingObservation)
                throw new AssertionError("Wicket scope leaked");
        }
    }

    private static class Region extends WebMarkupContainer implements HasMetaModelContext {
        private final transient CausewayObservationIntegration integration;
        Region(String id, CausewayObservationIntegration integration, WicketRenderObservationDescriptor descriptor) {
            super(id);
            this.integration = integration;
            WicketRenderObservationBehavior.addTo(this, descriptor);
        }
        public <T> Optional<T> lookupService(Class<T> type) {
            return type == CausewayObservationIntegration.class ? Optional.of(type.cast(integration)) : Optional.empty();
        }
    }

    private static final class PreparedRegion extends Region {
        private final WicketPagePreparationObservation preparation = new WicketPagePreparationObservation(
                WicketRenderObservationDescriptor.pagePreparation(TYPE));
        PreparedRegion(String id, CausewayObservationIntegration integration) {
            super(id, integration, WicketRenderObservationDescriptor.page(TYPE));
        }
        protected void onInitialize() { preparation.configure(this, () -> super.onInitialize()); }
        protected void onConfigure() { preparation.configure(this, () -> super.onConfigure()); }
        protected void onBeforeRender() { preparation.beforeRender(() -> super.onBeforeRender()); }
        protected void onDetach() { preparation.detach(() -> super.onDetach()); }
    }
}
