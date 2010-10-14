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


package org.apache.isis.extensions.dnd.histogram;

import org.apache.isis.extensions.dnd.drawing.Canvas;
import org.apache.isis.extensions.dnd.drawing.Color;
import org.apache.isis.extensions.dnd.drawing.ColorsAndFonts;
import org.apache.isis.extensions.dnd.drawing.Text;
import org.apache.isis.extensions.dnd.view.Content;
import org.apache.isis.extensions.dnd.view.Toolkit;
import org.apache.isis.extensions.dnd.view.ViewSpecification;
import org.apache.isis.extensions.dnd.view.base.ObjectView;


class HistogramBar extends ObjectView {
    private static final int colors[] = new int[] {0xccccff, 0x99ff99, 0xffccff};
    private final HistogramAxis histogramAxis;

    protected HistogramBar(Content content, HistogramAxis histogramAxis, ViewSpecification specification) {
        super(content, specification);
        this.histogramAxis = histogramAxis;
    }

    public void draw(Canvas canvas) {
        super.draw(canvas);
        int y = 0;
        int height = (getSize().getHeight() - 5) / histogramAxis.getNoBars();
        Text text = Toolkit.getText(ColorsAndFonts.TEXT_LABEL);
        Color color = Toolkit.getColor(ColorsAndFonts.COLOR_PRIMARY1);
        canvas.drawText(getContent().title(), 0, height / 2 + text.getAscent() / 2, color, text);

        for (int i = 0; i < histogramAxis.getNoBars(); i++) {
            double length = (getSize().getWidth() - 160) * histogramAxis.getLengthFor(getContent(), i);
            canvas.drawSolidRectangle(160, y, (int) length, height, Toolkit.getColor(colors[i % colors.length]));
            canvas.drawRectangle(160, y, (int) length, height, Toolkit.getColor(ColorsAndFonts.COLOR_BLACK));
            y += height + 2;
        }
    }

}

