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

package org.apache.isis.viewer.dnd.table;

import java.util.List;

import com.google.common.collect.Lists;

import org.apache.isis.core.commons.util.ToString;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.Contributed;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.viewer.dnd.view.View;
import org.apache.isis.viewer.dnd.view.collection.CollectionContent;

public class TableAxisImpl implements TableAxis {
    private final List<ObjectAssociation> columns;
    private final String[] columnName;
    private int rowHeaderOffet;
    private View table;
    private final int[] widths;

    public TableAxisImpl(final CollectionContent content) {
        // TODO create axis first, then after view built set up the axis
        // details?
        final ObjectSpecification elementSpecification = (content).getElementSpecification();
        final List<ObjectAssociation> accessibleFields = elementSpecification.getAssociations(Contributed.EXCLUDED, ObjectAssociation.Filters.VISIBLE_AT_LEAST_SOMETIMES);

        this.columns = tableFields(accessibleFields, content);
        widths = new int[columns.size()];
        columnName = new String[columns.size()];
        for (int i = 0; i < widths.length; i++) {
            columnName[i] = columns.get(i).getName();
        }

        // TODO make the setting of the column width strategy external so it can
        // be changed
        setupColumnWidths(new TypeBasedColumnWidthStrategy());
    }

    /*
     * public TableAxis(final ObjectAssociation[] columns) { this.columns =
     * columns; widths = new int[columns.length]; columnName = new
     * String[columns.length]; for (int i = 0; i < widths.length; i++) {
     * columnName[i] = columns[i].getName(); } }
     */
    private List<ObjectAssociation> tableFields(final List<ObjectAssociation> viewFields, final CollectionContent content) {
        for (int i = 0; i < viewFields.size(); i++) {
            // final ObjectAssociation objectAssociation = viewFields[i];
            // TODO reinstate check to skip unsuitable types
            /*
             * if (viewFields[i].getSpecification().isOfType(
             * IsisContext.getSpecificationLoader
             * ().loadSpecification(ImageValue.class))) { continue; }
             */

            // if
            // (!objectAssociation.isVisible(IsisContext.getAuthenticationSession(),
            // content.getAdapter()).isAllowed()) {
            // continue;
            // }
            // LOG.debug("column " + objectAssociation.getSpecification());
            // if(viewFields[i].getSpecification().isOfType(Isis.getSpecificationLoader().lo));
        }

        final List<ObjectAssociation> tableFields = Lists.newArrayList();
        for (int i = 0; i < viewFields.size(); i++) {
            if (!(viewFields.get(i) instanceof OneToManyAssociation)) {
                tableFields.add(viewFields.get(i));
            }
        }

        return tableFields;
    }

    @Override
    public void ensureOffset(final int offset) {
        rowHeaderOffet = Math.max(rowHeaderOffet, offset + 5);
    }

    /**
     * Returns the number of the column found at the specificied position,
     * ignoring the columns two borders. Returns 0 for the first column, 1 for
     * second column, etc.
     * 
     * If over the column border then returns -1.
     */
    @Override
    public int getColumnAt(final int xPosition) {
        int edge = getHeaderOffset();
        for (int i = 0, cols = getColumnCount() + 1; i < cols; i++) {
            if (xPosition >= edge - 1 && xPosition <= edge + 1) {
                return -1;
            }
            if (xPosition < edge - 1) {
                return i;
            }
            edge += getColumnWidth(i);
        }

        return -1;
    }

    /**
     * Returns 0 for left side of first column, 1 for right side of first
     * column, 2 for right side of second column, etc.
     * 
     * If no column border is identified then returns -1.
     */
    @Override
    public int getColumnBorderAt(final int xPosition) {
        int edge = getHeaderOffset();
        for (int i = 0, cols = getColumnCount(); i < cols; i++) {
            if (xPosition >= edge - 1 && xPosition <= edge + 1) {
                return i;
            }
            edge += getColumnWidth(i);
        }
        if (xPosition >= edge - 1 && xPosition <= edge + 1) {
            return getColumnCount();
        }

        return -1;
    }

    @Override
    public int getColumnCount() {
        return columnName.length;
    }

    @Override
    public String getColumnName(final int column) {
        return columnName[column];
    }

    @Override
    public int getColumnWidth(final int column) {
        return widths[column];
    }

    @Override
    public ObjectAssociation getFieldForColumn(final int column) {
        return columns.get(column);
    }

    @Override
    public int getHeaderOffset() {
        return rowHeaderOffet;
    }

    @Override
    public int getLeftEdge(final int resizeColumn) {
        int width = getHeaderOffset();
        for (int i = 0, cols = getColumnCount(); i < resizeColumn && i < cols; i++) {
            width += getColumnWidth(i);
        }
        return width;
    }

    @Override
    public void invalidateLayout() {
        final View[] rows = table.getSubviews();
        for (final View row : rows) {
            row.invalidateLayout();
        }
        table.invalidateLayout();
    }

    @Override
    public void setOffset(final int offset) {
        rowHeaderOffet = offset;
    }

    @Override
    public void setRoot(final View view) {
        table = view;
    }

    @Override
    public void setupColumnWidths(final ColumnWidthStrategy strategy) {
        for (int i = 0; i < widths.length; i++) {
            widths[i] = strategy.getPreferredWidth(i, columns.get(i));
        }
    }

    @Override
    public void setWidth(final int index, final int width) {
        widths[index] = width;
    }

    @Override
    public String toString() {
        final ToString str = new ToString(this);
        str.append("columns", columnName.length);
        str.append("header offset", rowHeaderOffet);
        str.append("table", table);
        return str.toString();
    }
}
