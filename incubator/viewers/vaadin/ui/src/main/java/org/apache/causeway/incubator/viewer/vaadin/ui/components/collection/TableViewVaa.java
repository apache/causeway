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
package org.apache.causeway.incubator.viewer.vaadin.ui.components.collection;

import java.util.Optional;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.core.metamodel.interactions.managed.nonscalar.DataRow;
import org.apache.causeway.core.metamodel.interactions.managed.nonscalar.DataTableModel;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.causeway.incubator.viewer.vaadin.model.context.UiContextVaa;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.val;
import lombok.extern.log4j.Log4j2;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Log4j2
public class TableViewVaa extends VerticalLayout {

    private static final long serialVersionUID = 1L;

    private static final String NULL_LITERAL = "<NULL>";

    public static TableViewVaa empty() {
        return new TableViewVaa();
    }

    /**
     * Constructs a (page-able) {@link Grid} from given {@code managedCollection}
     * @param managedCollection
     * @param where
     */
    public static Component forDataTableModel(
            final @NonNull UiContextVaa uiContext,
            final @NonNull DataTableModel dataTableModel,
            final @NonNull Where where) { //TODO not used yet (or is redundant)
        return dataTableModel.getElementCount()==0
                ? empty()
                : new TableViewVaa(dataTableModel);
    }

    /**
     *
     * @param elementSpec - as is common to all given {@code objects} aka elements
     * @param objects - (wrapped) domain objects to be rendered by this table
     */
    private TableViewVaa(
            final @NonNull DataTableModel dataTableModel) {

        //            final ComboBox<ManagedObject> listBox = new ComboBox<>();
        //            listBox.setLabel(label + " #" + objects.size());
        //            listBox.setItems(objects);
        //            if (!objects.isEmpty()) {
        //                listBox.setValue(objects.get(0));
        //            }
        //            listBox.setItemLabelGenerator(o -> o.titleString());

        val objectGrid = new Grid<DataRow>();
        add(objectGrid);

        val rows = dataTableModel.getDataRowsFiltered().getValue();

        if (rows.isEmpty()) {
            //TODO show placeholder: "No rows to display"
            return;
        }

        val columns = dataTableModel.getDataColumns().getValue();

        // object link as first column
        objectGrid.addColumn(row->{
            // TODO provide icon with link
            return "obj. ref [" + row.getRowElement().getBookmark().orElse(null) + "]";
        });

        // property columns
        columns.forEach(column->{
            val property = column.getPropertyMetaModel();
            objectGrid.addColumn(row -> {
                log.debug("about to get property value for property {}", property.getId());
                return stringifyPropertyValue(property, row.getRowElement());
            })
            .setHeader(property.getCanonicalFriendlyName());
            //TODO add column description as is provided via property.getColumnDescription()
        });

        // populate the model
        objectGrid.setItems(rows.toList());
        objectGrid.recalculateColumnWidths();
        objectGrid.setColumnReorderingAllowed(true);

    }

    private String stringifyPropertyValue(
            final ObjectAssociation property,
            final ManagedObject targetObject) {
        try {
            val propertyValue = property.get(targetObject);
            return propertyValue == null
                    ? NULL_LITERAL
                    : propertyValue.getTitle();
        } catch (Exception e) {
            return Optional.ofNullable(e.getMessage()).orElse(e.getClass().getName());
        }
    }


}
