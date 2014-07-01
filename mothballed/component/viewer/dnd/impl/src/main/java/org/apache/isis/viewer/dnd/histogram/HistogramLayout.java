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

package org.apache.isis.viewer.dnd.histogram;

import org.apache.isis.viewer.dnd.drawing.Location;
import org.apache.isis.viewer.dnd.drawing.Size;
import org.apache.isis.viewer.dnd.view.View;
import org.apache.isis.viewer.dnd.view.ViewConstants;
import org.apache.isis.viewer.dnd.view.base.Layout;

public class HistogramLayout implements Layout {
    private final int width = 500;
    private final int barHeight = 24;

    @Override
    public void layout(final View view, final Size maximumSize) {
        final HistogramAxis axis = view.getViewAxes().getAxis(HistogramAxis.class);
        axis.determineMaximum(view.getContent());
        final int noBars = axis.getNoBars();
        final int height = barHeight * noBars;
        final View[] subviews = view.getSubviews();
        int y = ViewConstants.VPADDING;
        for (final View bar : subviews) {
            bar.setSize(new Size(width, height));
            bar.setLocation(new Location(ViewConstants.HPADDING, y));
            y += height;
        }
    }

    @Override
    public Size getRequiredSize(final View view) {
        final HistogramAxis axis = view.getViewAxes().getAxis(HistogramAxis.class);
        final int noBars = axis.getNoBars();
        final View[] subviews = view.getSubviews();
        final int graphHeight = subviews.length * barHeight * noBars + ViewConstants.VPADDING * 2;
        return new Size(width + ViewConstants.HPADDING * 2, graphHeight);
    }

}
