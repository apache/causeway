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
import org.apache.isis.viewer.dnd.view.base.Layout;

/**
 * A column layout places component views side by side. Each component is given
 * the space it request. A components width will be give a consistent width if
 * that component's view specification's <code>isAligned</code> method returns
 * <code>true</code>, or the layout's <code>fixedWidth</code> flag is set (via
 * the two parameter constructor).
 */
public class ColumnLayout implements Layout {
    private final boolean fixedWidth;

    public ColumnLayout() {
        this.fixedWidth = false;
    }

    public ColumnLayout(final boolean fixedWidth) {
        this.fixedWidth = fixedWidth;
    }

    @Override
    public Size getRequiredSize(final View view) {
        int height = 0;
        int width = 0;
        int maxWidth = 0;
        final View views[] = view.getSubviews();

        for (final View v : views) {
            final Size s = v.getRequiredSize(new Size(Integer.MAX_VALUE, Integer.MAX_VALUE));
            width += s.getWidth();
            maxWidth = Math.max(maxWidth, s.getWidth());
            height = Math.max(height, s.getHeight());
        }

        if (fixedWidth) {
            width = maxWidth / 2 * views.length;
        }
        return new Size(width, height);
    }

    @Override
    public void layout(final View view, final Size maximumSize) {
        int x = 0;
        final int y = 0;
        final View subviews[] = view.getSubviews();

        int maxWidth = 0;
        for (final View v : subviews) {
            final Size s = v.getRequiredSize(new Size(maximumSize));
            maxWidth = Math.max(maxWidth, s.getWidth());
        }

        for (final View v : subviews) {
            final Size s = v.getRequiredSize(new Size(maximumSize));
            v.setLocation(new Location(x, y));
            if (fixedWidth || v.getSpecification().isAligned()) {
                x += maxWidth / 2;
                s.setWidth(maxWidth / 2);
            } else {
                x += s.getWidth();
            }
            v.setSize(s);
        }
    }

}
