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

package org.apache.isis.viewer.dnd.viewer.basic;

import org.apache.isis.core.commons.util.ToString;
import org.apache.isis.viewer.dnd.view.FocusManager;
import org.apache.isis.viewer.dnd.view.View;

public class TableFocusManager implements FocusManager {
    private int row;
    private int cell;
    private final View table;

    public TableFocusManager(final View table) {
        this.table = table;

        focusInitialChildView();
    }

    @Override
    public void focusNextView() {
        View r = table.getSubviews()[row];
        View[] cells = r.getSubviews();
        for (int j = cell + 1; j < cells.length; j++) {
            if (cells[j].canFocus()) {
                cells[cell].markDamaged();
                cell = j;
                // setFocus(cells[cell]);
                cells[j].markDamaged();
                return;
            }
        }

        row++;
        if (row == table.getSubviews().length) {
            row = 0;
        }

        r = table.getSubviews()[row];
        cells = r.getSubviews();
        for (int j = 0; j < cells.length; j++) {
            if (cells[j].canFocus()) {
                cells[cell].markDamaged();
                cell = j;
                cells[j].markDamaged();
                // setFocus(cells[cell]);
                return;
            }
        }
    }

    @Override
    public void focusPreviousView() {
        View r = table.getSubviews()[row];
        View[] cells = r.getSubviews();
        for (int j = cell - 1; j >= 0; j--) {
            if (cells[j].canFocus()) {
                cells[cell].markDamaged();
                cell = j;
                cells[j].markDamaged();
                return;
            }
        }

        row--;
        if (row == -1) {
            row = table.getSubviews().length - 1;
        }

        r = table.getSubviews()[row];
        cells = r.getSubviews();
        for (int j = cells.length - 1; j >= 0; j--) {
            if (cells[j].canFocus()) {
                cells[cell].markDamaged();
                cell = j;
                cells[j].markDamaged();
                return;
            }
        }
    }

    @Override
    public void focusParentView() {
    }

    @Override
    public void focusFirstChildView() {
    }

    @Override
    public void focusLastChildView() {
    }

    @Override
    public void focusInitialChildView() {
        row = cell = 0;

        final View[] rows = table.getSubviews();
        if (rows.length > 0) {
            row = 0;
            final View[] cells = rows[0].getSubviews();
            for (int j = 0; j < cells.length; j++) {
                if (cells[j].canFocus()) {
                    cells[cell].markDamaged();
                    cell = j;
                    cells[j].markDamaged();
                    // setFocus(cells[cell]);
                    return;
                }
            }
        }
    }

    @Override
    public View getFocus() {
        final View[] rows = table.getSubviews();
        if (row < 0 || row >= rows.length) {
            return table;
        }
        final View rowView = rows[row];
        final View[] cells = rowView.getSubviews();
        if (cell < 0 || cell >= cells.length) {
            return rowView;
        }
        return cells[cell];
    }

    @Override
    public void setFocus(final View view) {
        if (view == table) {
            return;
        }

        final View[] rows = table.getSubviews();
        for (row = 0; row < rows.length; row++) {
            final View[] cells = rows[row].getSubviews();
            for (int j = 0; j < cells.length; j++) {
                if (view == cells[j] && cells[j].canFocus()) {
                    cells[cell].markDamaged();
                    cell = j;
                    cells[j].markDamaged();
                    return;
                }
            }
        }
    }

    @Override
    public String toString() {
        final ToString str = new ToString(this);
        str.append("row", row);
        str.append("cell", cell);
        return str.toString();
    }
}
