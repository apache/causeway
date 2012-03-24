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

import org.apache.isis.noa.adapter.ObjectAdapter;
import org.apache.isis.noa.util.DebugString;
import org.apache.isis.extensions.dndviewer.ColorsAndFonts;
import org.apache.isis.viewer.dnd.Canvas;
import org.apache.isis.viewer.dnd.Content;
import org.apache.isis.viewer.dnd.Toolkit;
import org.apache.isis.viewer.dnd.View;
import org.apache.isis.viewer.dnd.Workspace;
import org.apache.isis.viewer.dnd.drawing.Bounds;
import org.apache.isis.viewer.dnd.drawing.Color;
import org.apache.isis.viewer.dnd.drawing.Location;
import org.apache.isis.viewer.dnd.drawing.Size;
import org.apache.isis.viewer.dnd.view.simple.AbstractView;

import java.util.Vector;


public class TestWorkspaceView extends AbstractView implements Workspace {
    private static final Color markerDark = Toolkit.getColor(0xcccccc);
    private static final Color markerLight = Toolkit.getColor(0xf0f0f0);
    private final Vector views = new Vector();
    private boolean showOutline;

    public View[] getSubviews() {
        View[] array = new View[views.size()];
        views.copyInto(array);
        return array;
    }

    protected TestWorkspaceView(final Content content) {
        super(content, new TestWorkspaceSpecification(), null);
    }

    public void debug(DebugString debug) {
        View[] subviews = getSubviews();
        for (int i = 0; i < subviews.length; i++) {
            subviews[i].debug(debug);
            debug.append("\nContent: ");
            debug.append(subviews[i].getContent() == null ? "none" : ("" + subviews[i].getContent().getAdapter()));
            debug.append("\n----------------\n");
        }
    }

    public void draw(final Canvas canvas) {
        Bounds bounds = getBounds();
        canvas.drawRectangle(0, 0, bounds.getWidth(), bounds.getHeight(), Toolkit.getColor(0x0000ff));
        canvas.drawText("Test Workspace", 10, 20, Toolkit.getColor(0x0000ff), Toolkit.getText(ColorsAndFonts.TEXT_TITLE));

        for (int i = 0; i < views.size(); i++) {
            View view = (View) views.elementAt(i);

            if (showOutline()) {
                final Size requiredSize = view.getSize();
                final Location location = view.getLocation();

                final int width = requiredSize.getWidth();
                final int height = requiredSize.getHeight();
                final int baseline = location.getY() + view.getBaseline();
                final int left = location.getX() - 10;
                final int top = location.getY() - 10;
                final int right = left + 10 + width - 1 + 10;
                final int bottom = top + 10 + height - 1 + 10;

                // horizontal lines
                canvas.drawLine(left, top + 10, right, top + 10, markerDark);
                canvas.drawLine(left, bottom - 10, right, bottom - 10, markerDark);

                // vertical lines
                canvas.drawLine(left + 10, top, left + 10, bottom, markerDark);
                canvas.drawLine(right - 10, top, right - 10, bottom, markerDark);

                canvas.drawRectangle(left + 10, top + 10, width - 1, height - 1, markerLight);

                canvas.drawLine(left, baseline, left + 10, baseline, markerDark);
                canvas.drawLine(right - 10, baseline, right, baseline, markerDark);
                canvas.drawLine(left + 10, baseline, right - 10, baseline, markerLight);
            }
            Canvas subcanvas = canvas.createSubcanvas(view.getBounds());
            view.draw(subcanvas);
        }

    }

    private boolean showOutline() {
        return showOutline;
    }

    public void setShowOutline(final boolean showOutline) {
        this.showOutline = showOutline;
    }

    public View subviewFor(final Location location) {
        for (int i = 0; i < views.size(); i++) {
            View view = (View) views.elementAt(i);
            if (view.getBounds().contains(location)) {
                return view;
            }
        }

        return null;
    }

    public void layout(final Size maximumSize) {
        for (int i = 0; i < views.size(); i++) {
            View view = (View) views.elementAt(i);
            view.layout(new Size());
        }

    }

    public Size getRequiredSize(final Size maximumSize) {
        return new Size(600, 400);
    }

    public Workspace getWorkspace() {
        return this;
    }

    public View addIconFor(final ObjectAdapter adapter, final Location at) {
        return null;
    }

    public View addOpenViewFor(final ObjectAdapter object, final Location at) {
        return null;
    }

    public View createSubviewFor(final ObjectAdapter object, final boolean asIcon) {
        return null;
    }

    public void lower(final View view) {}

    public void raise(final View view) {}

    public void removeViewsFor(final ObjectAdapter object) {}

    public void removeView(final View view) {
        System.out.println("remove view " + view);
    }

    public void addView(final View view) {
        views.addElement(view);
        view.setParent(this);
    }

    public void removeObject(ObjectAdapter object) {}

}
