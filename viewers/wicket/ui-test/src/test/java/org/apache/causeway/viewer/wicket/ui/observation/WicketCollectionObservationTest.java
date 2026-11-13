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

import java.util.*;
import io.micrometer.observation.*;
import org.junit.jupiter.api.Test;
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
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WicketCollectionObservationTest {
    static final String TYPE = "demo.Owner", ID = TYPE + "#items";
    @Test void actualTableFullPageAndAjaxRespectAllDetailLevelsAndPagination() {
        for(var detail : Detail.values()) verify(detail, 0);
    }
    @Test void actualTableSharesHardBudgetAndResetsForAjax() { verify(Detail.MEMBERS, 5); }
    @Test void actualTableWithUnavailableIntegrationStillRenders() { verify(Detail.MEMBERS, 0, true); }
    @Test void observedReuseKeepsMainIndexBasedStrategy() {
        var recording = new WicketObservationCoordinatorTest.Recording();
        var integration = WicketObservationCoordinatorTest.integration(recording);
        var config = mock(CausewayConfiguration.class, RETURNS_DEEP_STUBS);
        when(config.viewer().wicket().observation()).thenReturn(new CausewayConfiguration.Viewer.Wicket.Observation(Detail.MEMBERS, 0));
        var tester = new WicketTester();
        try {
            var table = new Table(new Provider(), integration, config);
            table.observeCollection(TYPE, "demo.Item", ID);
            var oldModel = mock(org.apache.causeway.viewer.wicket.model.models.coll.DataRowWkt.class);
            var newModel = mock(org.apache.causeway.viewer.wicket.model.models.coll.DataRowWkt.class);
            when(oldModel.rowIndex()).thenReturn(17); when(newModel.rowIndex()).thenReturn(17);
            var oldItem = new Item<DataRow>("old", 7, oldModel);
            var rows = ((org.apache.wicket.markup.repeater.RefreshingView<DataRow>) table.getBody().get("rows")).getItemReuseStrategy().getItems(
                    (index, model) -> { throw new AssertionError("existing row should be reused"); },
                    List.<IModel<DataRow>>of(newModel).iterator(), List.of(oldItem).iterator());
            assertSame(oldItem, rows.next());
            assertEquals(0, oldItem.getIndex());
            assertFalse(rows.hasNext());
            assertNull(integration.observationRegistry().getCurrentObservation());
        } finally { tester.destroy(); }
    }
    @Test void tableActionCandidateOutsideCanonicalTableDoesNotOpenObservation() {
        var recording = new WicketObservationCoordinatorTest.Recording();
        var integration = WicketObservationCoordinatorTest.integration(recording);
        var config = mock(CausewayConfiguration.class, RETURNS_DEEP_STUBS);
        when(config.viewer().wicket().observation()).thenReturn(new CausewayConfiguration.Viewer.Wicket.Observation(Detail.MEMBERS, 0));
        var tester = new WicketTester();
        try {
            var container = new CollectionRegion(integration, config);
            var action = new Label("action", "action");
            WicketRenderObservationBehavior.addToParentedTableMember(action, WicketRenderObservationDescriptor.action(TYPE, TYPE + "#update()"));
            container.add(action);
            tester.startComponentInPage(container, Markup.of("<div wicket:id='component'><a wicket:id='action'></a></div>"));
            assertFalse(recording.started.stream().anyMatch(c -> c.getName().equals("causeway.wicket.action.render")));
            assertNull(integration.observationRegistry().getCurrentObservation());
        } finally { tester.destroy(); }
    }
    private void verify(Detail detail, int maximum) { verify(detail, maximum, false); }
    private void verify(Detail detail, int maximum, boolean noop) {
        var recording = new WicketObservationCoordinatorTest.Recording();
        var integration = noop ? new CausewayObservationIntegration(ObservationRegistry.NOOP)
                : WicketObservationCoordinatorTest.integration(recording);
        var config = mock(CausewayConfiguration.class, RETURNS_DEEP_STUBS);
        when(config.viewer().wicket().observation()).thenReturn(new CausewayConfiguration.Viewer.Wicket.Observation(detail, maximum));
        var tester = new WicketTester();
        try {
            tester.getApplication().getRequestCycleListeners().add(new org.apache.wicket.request.cycle.IRequestCycleListener() {
                public void onEndRequest(org.apache.wicket.request.cycle.RequestCycle cycle) { WicketRenderObservationTracker.cleanup(cycle, null); }
            });
            var provider = new Provider();
            var collection = new CollectionRegion(integration, config);
            var table = new Table(provider, integration, config);
            table.observeCollection(TYPE, "demo.Item", ID);
            collection.add(table);
            var refresh = new AjaxLink<Void>("refresh") {
                public void onClick(AjaxRequestTarget target) { target.add(collection); }
            };
            collection.add(refresh);
            tester.startComponentInPage(collection, Markup.of("<div wicket:id='component'><table wicket:id='table'></table><a wicket:id='refresh'>refresh</a></div>"));
            int firstStarts = recording.started.size();
            assertEquals(1, provider.iterations);
            assertEquals(2, provider.models);
            assertEquals(2, provider.limit);
            if(maximum > 0) assertTrue(firstStarts <= maximum);
            assertNull(integration.observationRegistry().getCurrentObservation());
            tester.executeAjaxEvent(refresh, "click");
            assertEquals(2, provider.iterations);
            assertEquals(4, provider.models);
            assertNull(integration.observationRegistry().getCurrentObservation());
            if(maximum > 0) assertTrue(recording.started.size() - firstStarts <= maximum);
            if(noop || detail == Detail.NONE) assertTrue(recording.started.isEmpty());
            else {
                if(detail.ordinal() >= Detail.REGIONS.ordinal()) assertTrue(recording.started.stream().anyMatch(c -> c.getName().equals("causeway.wicket.collection.table.render")));
                if(detail.ordinal() >= Detail.REGIONS.ordinal() && maximum==0) {
                    var summaries = recording.stopped.stream().filter(c -> c.getName().equals("causeway.wicket.collection.render")).toList();
                    assertEquals(2, summaries.size());
                    for(var c : summaries) {
                        assertEquals(2, WicketObservationCoordinatorTest.tag(c, WicketObservationCoordinator.ROW_COUNT_TAG));
                        assertEquals(2, WicketObservationCoordinatorTest.tag(c, WicketObservationCoordinator.CELL_COUNT_TAG));
                    }
                }
                assertEquals(recording.started.size(), recording.stopped.size());
            }
        } finally { tester.destroy(); }
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
        Table(Provider provider, CausewayObservationIntegration integration, CausewayConfiguration config) {
            super("table", List.of(new AbstractColumn<DataRow,String>(Model.of("Name")) {
                public void populateItem(Item<ICellPopulator<DataRow>> item, String id, IModel<DataRow> model) {
                    var label = new Label(id, "value");
                    WicketRenderObservationBehavior.addTo(label, WicketRenderObservationDescriptor.property("demo.Item", "demo.Item#name"));
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
