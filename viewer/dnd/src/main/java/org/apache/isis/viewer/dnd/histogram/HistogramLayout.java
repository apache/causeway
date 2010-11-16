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
import org.apache.isis.viewer.dnd.view.base.Layout;


public class HistogramLayout implements Layout {
    private int width = 500;
    private int barHeight = 24;

    public void layout(View view, Size maximumSize) {
        HistogramAxis axis = view.getViewAxes().getAxis(HistogramAxis.class);
        axis.determineMaximum(view.getContent());
        int noBars = axis.getNoBars();
        int height = barHeight * noBars;
        View[] subviews = view.getSubviews();
        int y = View.VPADDING;
        for (View bar : subviews) {
            bar.setSize(new Size(width, height));
            bar.setLocation(new Location(View.HPADDING, y));
            y += height;
        }
    }

    public Size getRequiredSize(View view) {
        HistogramAxis axis = view.getViewAxes().getAxis(HistogramAxis.class);
        int noBars = axis.getNoBars();
        View[] subviews = view.getSubviews();
        int graphHeight = subviews.length * barHeight * noBars + View.VPADDING * 2;
        return new Size(width + View.HPADDING * 2   , graphHeight);
    }

}

