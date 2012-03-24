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

import org.apache.isis.viewer.dnd.drawing.Location;
import org.apache.isis.viewer.dnd.drawing.Size;
import org.apache.isis.viewer.dnd.view.View;
import org.apache.isis.viewer.dnd.view.base.Layout;

public class TableRowLayout implements Layout {
    private final TableAxis axis;

    public TableRowLayout(final TableAxis axis) {
        this.axis = axis;
    }

    @Override
    public Size getRequiredSize(final View row) {
        int maxHeight = 0;
        int totalWidth = 0;
        final View[] cells = row.getSubviews();
        final int maxBaseline = maxBaseline(cells);

        for (int i = 0; i < cells.length; i++) {
            totalWidth += axis.getColumnWidth(i);

            final Size s = cells[i].getRequiredSize(Size.createMax());// TODO
                                                                      // Need to
                                                                      // pass in
                                                                      // a max
                                                                      // size
                                                                      // (is 0
                                                                      // at the
                                                                      // moment)
            final int b = cells[i].getBaseline();
            final int baselineOffset = Math.max(0, maxBaseline - b);
            maxHeight = Math.max(maxHeight, s.getHeight() + baselineOffset);
        }

        return new Size(totalWidth, maxHeight);
    }

    @Override
    public void layout(final View row, final Size maximumSize) {
        final View[] cells = row.getSubviews();
        final int maxBaseline = maxBaseline(cells);

        int x = 0;
        for (int i = 0; i < cells.length; i++) {
            final View cell = cells[i];
            final Size s = cell.getRequiredSize(Size.createMax()); // TODO Need
                                                                   // to pass in
                                                                   // a max size
                                                                   // (is 0 at
                                                                   // the
                                                                   // moment)
            s.setWidth(axis.getColumnWidth(i));
            cell.setSize(s);

            final int b = cell.getBaseline();
            final int baselineOffset = Math.max(0, maxBaseline - b);
            cell.setLocation(new Location(x, baselineOffset));

            x += s.getWidth();
        }
    }

    private int maxBaseline(final View[] cells) {
        int maxBaseline = 0;
        for (final View cell : cells) {
            maxBaseline = Math.max(maxBaseline, cell.getBaseline());
        }
        return maxBaseline;
    }

}
