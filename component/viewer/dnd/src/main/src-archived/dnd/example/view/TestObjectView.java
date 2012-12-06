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


package org.apache.isis.viewer.dnd.example.view;

import org.apache.isis.extensions.dndviewer.ColorsAndFonts;
import org.apache.isis.viewer.dnd.Canvas;
import org.apache.isis.viewer.dnd.Click;
import org.apache.isis.viewer.dnd.Content;
import org.apache.isis.viewer.dnd.Drag;
import org.apache.isis.viewer.dnd.DragStart;
import org.apache.isis.viewer.dnd.Toolkit;
import org.apache.isis.viewer.dnd.ViewAxis;
import org.apache.isis.viewer.dnd.ViewSpecification;
import org.apache.isis.viewer.dnd.drawing.Location;
import org.apache.isis.viewer.dnd.drawing.Size;
import org.apache.isis.viewer.dnd.view.simple.AbstractView;


public class TestObjectView extends AbstractView {

    private int requiredWidth;
    private int requiredHeight;
    private final String label;

    public TestObjectView(final Content content, final ViewSpecification specification, final ViewAxis axis, final int width, final int height, final String label) {
        super(content, specification, axis);
        this.requiredWidth = width;
        this.requiredHeight = height;
        this.label = label;
    }

    public void draw(final Canvas canvas) {
        super.draw(canvas);
        int width = getSize().getWidth();
        int height = getSize().getHeight();
        canvas.clearBackground(this, Toolkit.getColor(0xeeeeee));
        canvas.drawRectangle(0, 0, width - 1, height - 1, Toolkit.getColor(0xcccccc));
        canvas.drawLine(0, 0, width - 1, height - 1, Toolkit.getColor(0xff0000));
        canvas.drawLine(width - 1, 0, 0, height - 1, Toolkit.getColor(0xff0000));
        canvas.drawText(label, 2, Toolkit.getText(ColorsAndFonts.TEXT_NORMAL).getAscent() + 2, Toolkit.getColor(0), Toolkit.getText(ColorsAndFonts.TEXT_NORMAL));
    }

    public Size getRequiredSize(final Size maximumSize) {
        return new Size(requiredWidth, requiredHeight);
    }

    public void setMaximumSize(final Size size) {
        requiredHeight = size.getHeight();
        requiredWidth = size.getWidth();

        setSize(size);
    }

    public void firstClick(final Click click) {
        debug("first click " + click);
        super.firstClick(click);
    }

    public void secondClick(final Click click) {
        debug("second click " + click);
        super.secondClick(click);
    }

    public void mouseMoved(final Location location) {
        debug("mouse moved " + location);
        super.mouseMoved(location);
    }

    private void debug(final String str) {
        getViewManager().getSpy().addAction(str);
    }

    public Drag dragStart(final DragStart drag) {
        debug("drag start " + drag);
        return super.dragStart(drag);
    }
}
