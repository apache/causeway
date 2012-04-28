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

package org.apache.isis.viewer.dnd.view.composite;

import org.apache.isis.viewer.dnd.drawing.Location;
import org.apache.isis.viewer.dnd.drawing.Size;
import org.apache.isis.viewer.dnd.view.View;
import org.apache.isis.viewer.dnd.view.ViewAxis;
import org.apache.isis.viewer.dnd.view.base.Layout;

/**
 */
public class GridLayout implements Layout, ViewAxis {
    public final static int COLUMNS = 0;
    public final static int ROWS = 1;
    private int orientation = COLUMNS;
    private int size = 1;

    @Override
    public Size getRequiredSize(final View view) {
        final View views[] = view.getSubviews();

        final int max[] = new int[size];
        final int total[] = new int[size];

        int column = 0;
        for (final View v : views) {
            final Size s = v.getRequiredSize(new Size(Integer.MAX_VALUE, Integer.MAX_VALUE));
            if (orientation == COLUMNS) {
                total[column] += s.getHeight();
                max[column] = Math.max(max[column], s.getWidth());
            } else {
                total[column] += s.getWidth();
                max[column] = Math.max(max[column], s.getHeight());
            }
            column++;
            if (column >= size) {
                column = 0;
            }
        }

        int height = 0;
        int width = 0;
        for (int i = 0; i < size; i++) {
            if (orientation == COLUMNS) {
                height = Math.max(height, total[i]);
                width += max[i];
            } else {
                width = Math.max(width, total[i]);
                height += max[i];
            }
        }
        return new Size(width, height);
    }

    @Override
    public void layout(final View view, final Size maximumSize) {
        int x = 0;
        int y = 0;
        final View views[] = view.getSubviews();
        final int max[] = new int[size];

        int column = 0;
        for (final View v : views) {
            final Size s = v.getRequiredSize(new Size(maximumSize));
            if (orientation == COLUMNS) {
                max[column] = Math.max(max[column], s.getWidth());
            } else {
                max[column] = Math.max(max[column], s.getHeight());
            }
            column++;
            if (column >= size) {
                column = 0;
            }
        }

        column = 0;
        for (final View v : views) {
            final Size s = v.getRequiredSize(new Size(maximumSize));
            v.setLocation(new Location(x, y));
            if (orientation == COLUMNS) {
                x += max[column];
                s.ensureWidth(max[column]);
            } else {
                y += max[column];
                s.ensureHeight(max[column]);
            }
            v.setSize(s);
            column++;
            if (column >= size) {
                column = 0;
                if (orientation == COLUMNS) {
                    x = 0;
                    y += s.getHeight();
                } else {
                    y = 0;
                    x += s.getWidth();
                }
            }

        }
    }

    public int getOrientation() {
        return orientation;
    }

    public void setOrientation(final int orientation) {
        this.orientation = orientation;
    }

    public int getSize() {
        return size;
    }

    public void setSize(final int size) {
        this.size = size;
    }

}
