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

import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.viewer.dnd.view.View;
import org.apache.isis.viewer.dnd.view.ViewAxis;

public interface TableAxis extends ViewAxis {

    void ensureOffset(final int offset);

    /**
     * Returns the number of the column found at the specificied position,
     * ignoring the columns two borders. Returns 0 for the first column, 1 for
     * second column, etc.
     * 
     * If over the column border then returns -1.
     */
    int getColumnAt(final int xPosition);

    /**
     * Returns 0 for left side of first column, 1 for right side of first
     * column, 2 for right side of second column, etc.
     * 
     * If no column border is identified then returns -1.
     */
    int getColumnBorderAt(final int xPosition);

    int getColumnCount();

    String getColumnName(final int column);

    int getColumnWidth(final int column);

    ObjectAssociation getFieldForColumn(final int column);

    int getHeaderOffset();

    int getLeftEdge(final int resizeColumn);

    void invalidateLayout();

    void setOffset(final int offset);

    void setRoot(final View view);

    void setupColumnWidths(final ColumnWidthStrategy strategy);

    void setWidth(final int index, final int width);
}
