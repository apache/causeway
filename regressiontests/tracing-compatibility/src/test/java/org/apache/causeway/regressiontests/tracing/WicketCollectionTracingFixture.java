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

import java.util.*;
import io.micrometer.observation.*;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.markup.html.repeater.data.table.*;
import org.apache.wicket.markup.Markup;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.model.*;
import org.apache.wicket.util.tester.WicketTester;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.config.CausewayConfiguration.Viewer.Wicket.Observation.Detail;
import org.apache.causeway.core.config.observation.CausewayObservationIntegration;
import org.apache.causeway.core.metamodel.context.HasMetaModelContext;
import org.apache.causeway.core.metamodel.tabular.DataRow;
import org.apache.causeway.viewer.wicket.ui.components.collection.present.ajaxtable.CollectionContentsSortableDataProvider;
import org.apache.causeway.viewer.wicket.ui.components.table.CausewayAjaxDataTable;

import org.apache.causeway.viewer.wicket.ui.observation.*;
import org.apache.causeway.core.runtimeservices.ia.InteractionServiceDefault;
import org.apache.causeway.applib.services.iactn.InteractionContext;
import org.apache.causeway.applib.services.user.UserMemento;
import org.apache.causeway.viewer.wicket.viewer.integration.*;
/** Actual main DataTable population/render/Ajax with production tracing ownership. */
final class WicketCollectionTracingFixture {
    static final String TYPE = "fixture.CollectionOwner", ID = TYPE + "#items";
    static void render(CausewayObservationIntegration integration, CausewayConfiguration config,
            InteractionServiceDefault interactions, Runnable work, boolean fail) {
        var enclosing = integration.observationRegistry().getCurrentObservation();
        var tester = new WicketTester();
        var application = tester.getApplication();
        application.setRequestCycleProvider(RequestCycle2::new);
        application.getRequestCycleListeners().add(new TelemetryStartHandler(integration));
        application.getRequestCycleListeners().add(new org.apache.wicket.request.cycle.IRequestCycleListener() {
            public void onBeginRequest(org.apache.wicket.request.cycle.RequestCycle cycle) {
                interactions.openInteraction(InteractionContext.ofUserWithSystemDefaults(UserMemento.ofName("sentinel-user")));
            }
            public org.apache.wicket.request.IRequestHandler onException(org.apache.wicket.request.cycle.RequestCycle cycle, Exception ex) {
                WicketRenderObservationTracker.cleanup(cycle, ex);
                ((RequestCycle2) cycle).observationClosure.onError(ex);
                // Consume the deliberate fixture failure without WicketTester's
                // error-page rethrow bypassing normal request completion.
                return new org.apache.wicket.request.IRequestHandler() {
                    public void respond(org.apache.wicket.request.IRequestCycle requestCycle) { }
                    public void detach(org.apache.wicket.request.IRequestCycle requestCycle) { }
                };
            }
            public void onEndRequest(org.apache.wicket.request.cycle.RequestCycle cycle) {
                new WebRequestCycleForCauseway(interactions, null, null, null, null).onEndRequest(cycle);
            }
        });
        try {
            var collection = new CollectionRegion(integration, config);
            var table = new Table(new Provider(), integration, config, work, fail);
            table.observeCollection(TYPE, TYPE, ID);
            collection.add(table);
            var refresh = new AjaxLink<Void>("refresh") {
                public void onClick(AjaxRequestTarget target) { target.add(collection); }
            };
            collection.add(refresh);
            tester.startComponentInPage(collection, Markup.of("<div wicket:id='component'><table wicket:id='table'></table><a wicket:id='refresh'>refresh</a></div>"));
            if(fail) throw new IllegalStateException("Expected collection rendering failure");
            tester.executeAjaxEvent(refresh, "click");
        } catch(RuntimeException failure) {
            if(!fail) throw failure;
            throw new IllegalStateException("Expected collection rendering failure", failure);
        } finally {
            tester.destroy();
            if(integration.observationRegistry().getCurrentObservation() != enclosing) throw new AssertionError("collection scope leaked");
        }
    }
    static final class Provider extends CollectionContentsSortableDataProvider {
        int iterations, models; long limit;
        Provider() { super(null); }
        public long size() { return 7; }
        public Iterator<DataRow> iterator(long first, long count) {
            iterations++; limit=count;
            return Collections.<DataRow>nCopies((int)Math.min(count,7-first), null).iterator();
        }
        public IModel<DataRow> model(DataRow row) { models++; return new IModel<>() { public DataRow getObject() { return null; } }; }
    }
    static final class Table extends CausewayAjaxDataTable {
        private final transient CausewayObservationIntegration integration;
        private final transient CausewayConfiguration config;
        Table(Provider provider, CausewayObservationIntegration integration, CausewayConfiguration config, Runnable work, boolean fail) {
            super("table", List.of(new AbstractColumn<DataRow,String>(Model.of("Name")) {
                public void populateItem(Item<ICellPopulator<DataRow>> item, String id, IModel<DataRow> model) {
                    var label = new Label(id, "value") {
                        protected void onRender() {
                            work.run();
                            if(fail) throw new IllegalStateException("collection row failed");
                            super.onRender();
                        }
                    };
                    WicketRenderObservationBehavior.addTo(label, WicketRenderObservationDescriptor.property(TYPE, TYPE + "#name"));
                    item.add(label);
                }
            }), provider, 2);
            this.integration=integration; this.config=config;
        }
        protected void buildGui() {
            addTopToolbar(new org.apache.wicket.extensions.markup.html.repeater.data.table.HeadersToolbar<>(this,
                    (org.apache.wicket.extensions.markup.html.repeater.data.sort.ISortStateLocator<String>) getDataProvider()));
            addBottomToolbar(new NoRecordsToolbar(this));
        }
        public <T> Optional<T> lookupService(Class<T> type) {
            return type == CausewayObservationIntegration.class ? Optional.of(type.cast(integration))
                    : type == CausewayConfiguration.class ? Optional.of(type.cast(config)) : Optional.empty();
        }
    }
    static final class CollectionRegion extends WebMarkupContainer implements HasMetaModelContext {
        private final transient CausewayObservationIntegration integration;
        private final transient CausewayConfiguration config;
        private final WicketPagePreparationObservation preparation = new WicketPagePreparationObservation(
                WicketRenderObservationDescriptor.collectionPreparation(TYPE, ID));
        CollectionRegion(CausewayObservationIntegration integration, CausewayConfiguration config) {
            super("component"); this.integration=integration; this.config=config; setOutputMarkupId(true);
            WicketRenderObservationBehavior.addTo(this, WicketRenderObservationDescriptor.collection(TYPE, ID));
        }
        protected void onBeforeRender() { preparation.prepare(this, () -> super.onBeforeRender()); }
        protected void onDetach() { preparation.detach(() -> super.onDetach()); }
        public <T> Optional<T> lookupService(Class<T> type) {
            return type == CausewayObservationIntegration.class ? Optional.of(type.cast(integration))
                    : type == CausewayConfiguration.class ? Optional.of(type.cast(config)) : Optional.empty();
        }
    }
}
