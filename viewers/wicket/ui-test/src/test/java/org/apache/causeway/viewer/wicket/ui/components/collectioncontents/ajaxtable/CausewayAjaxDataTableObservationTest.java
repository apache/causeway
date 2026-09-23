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
package org.apache.causeway.viewer.wicket.ui.components.collectioncontents.ajaxtable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import org.apache.wicket.markup.repeater.IItemFactory;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.tester.WicketTester;

import org.apache.causeway.core.config.observation.CausewayObservationIntegration;
import org.apache.causeway.core.metamodel.context.HasMetaModelContext;
import org.apache.causeway.core.metamodel.tabular.interactive.DataRow;
import org.apache.causeway.viewer.wicket.model.models.interaction.coll.DataRowWkt;
import org.apache.causeway.viewer.wicket.ui.observation.WicketRenderObservationBehavior;
import org.apache.causeway.viewer.wicket.ui.observation.WicketRenderObservationDescriptor;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationHandler;
import io.micrometer.observation.ObservationRegistry;

class CausewayAjaxDataTableObservationTest {

    private static final String ELEMENT_TYPE = "demo.Order";
    private static final String COLLECTION_ID = "demo.Customer#orders";

    @Test
    void parentedRowsReceiveRenderObservationWhileStandaloneRowsDoNot() {
        final WicketTester tester = new WicketTester();
        try {
            final TestTable parented = new TestTable(ELEMENT_TYPE, COLLECTION_ID);
            final Item<DataRow> parentedRow = parented.newObservedRow();

            assertTrue(parentedRow instanceof HasMetaModelContext);
            assertEquals(1,
                    parentedRow.getBehaviors(WicketRenderObservationBehavior.class).size());

            final TestTable standalone = new TestTable(ELEMENT_TYPE, null);
            assertEquals(0,
                    standalone.newObservedRow()
                            .getBehaviors(WicketRenderObservationBehavior.class).size());
        } finally {
            tester.destroy();
        }
    }

    @Test
    void itemReuseStrategyCreatesOnlyRowsProvidedForTheCurrentPage() {
        final WicketTester tester = new WicketTester();
        try {
            final DataRowWkt first = mock(DataRowWkt.class);
            final DataRowWkt second = mock(DataRowWkt.class);
            final List<IModel<DataRow>> visibleModels = List.of(first, second);
            final IItemFactory<DataRow> factory = (index, model) ->
                    new Item<>(Integer.toString(index), index, model);
            final CausewayAjaxDataTable.RowItemReuseStrategy strategy =
                    new CausewayAjaxDataTable.RowItemReuseStrategy(null);

            final Iterator<Item<DataRow>> rows = strategy.getItems(
                    factory,
                    visibleModels.iterator(),
                    Collections.emptyIterator());

            int count = 0;
            while(rows.hasNext()) {
                rows.next();
                count++;
            }
            assertEquals(2, count);
        } finally {
            tester.destroy();
        }
    }

    @Test
    void itemReuseStrategyObservesEachSynchronousRowPopulationCallback() {
        final WicketTester tester = new WicketTester();
        try {
            final ObservationRegistry registry = ObservationRegistry.create();
            registry.observationConfig().observationHandler(
                    new ObservationHandler<Observation.Context>() {
                        @Override
                        public boolean supportsContext(final Observation.Context context) {
                            return true;
                        }
                    });
            final CausewayObservationIntegration integration =
                    new CausewayObservationIntegration(registry);
            final WicketRenderObservationDescriptor descriptor =
                    WicketRenderObservationDescriptor.rowPreparation(
                            ELEMENT_TYPE, COLLECTION_ID);
            final CausewayAjaxDataTable.RowItemReuseStrategy strategy =
                    new CausewayAjaxDataTable.RowItemReuseStrategy(
                            descriptor, integration);
            final List<String> activeObservations = new ArrayList<>();
            final IItemFactory<DataRow> factory = (index, model) -> {
                activeObservations.add(
                        registry.getCurrentObservation().getContext().getName());
                return new Item<>(Integer.toString(index), index, model);
            };
            final List<IModel<DataRow>> visibleModels = List.of(
                    mock(DataRowWkt.class), mock(DataRowWkt.class));

            final Iterator<Item<DataRow>> rows = strategy.getItems(
                    factory,
                    visibleModels.iterator(),
                    Collections.emptyIterator());
            while(rows.hasNext()) {
                rows.next();
            }

            assertEquals(List.of(
                    "causeway.wicket.collection.row.prepare",
                    "causeway.wicket.collection.row.prepare"),
                    activeObservations);
            assertNull(registry.getCurrentObservation());
        } finally {
            tester.destroy();
        }
    }

    @Test
    void itemReuseStrategyRetainsOnlySerializableStaticPreparationMetadata()
            throws Exception {
        final WicketRenderObservationDescriptor descriptor =
                WicketRenderObservationDescriptor.rowPreparation(
                        ELEMENT_TYPE, COLLECTION_ID);
        final CausewayAjaxDataTable.RowItemReuseStrategy restored = roundTrip(
                new CausewayAjaxDataTable.RowItemReuseStrategy(descriptor));

        assertEquals("causeway.wicket.collection.row.prepare",
                restored.descriptor().getRegion().getObservationName());
        assertEquals("prepare row demo.Order",
                restored.descriptor().getContextualName());
        assertEquals(ELEMENT_TYPE, restored.descriptor().getObjectType());
        assertEquals(COLLECTION_ID, restored.descriptor().getMemberId());
    }

    @SuppressWarnings("unchecked")
    private static <T> T roundTrip(final T object) throws Exception {
        final byte[] serialized;
        try (ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                ObjectOutputStream output = new ObjectOutputStream(bytes)) {
            output.writeObject(object);
            serialized = bytes.toByteArray();
        }
        try (ObjectInputStream input = new ObjectInputStream(
                new ByteArrayInputStream(serialized))) {
            return (T) input.readObject();
        }
    }

    private static final class TestTable extends CausewayAjaxDataTable {

        private static final long serialVersionUID = 1L;

        private TestTable(
                final String elementObjectType,
                final String collectionId) {
            super(
                    "table",
                    Collections.emptyList(),
                    mock(CollectionContentsSortableDataProvider.class),
                    10,
                    null,
                    elementObjectType,
                    collectionId);
        }

        private Item<DataRow> newObservedRow() {
            return newRowItem("row", 0, new IModel<DataRow>() {
                private static final long serialVersionUID = 1L;

                @Override
                public DataRow getObject() {
                    return null;
                }
            });
        }
    }
}
