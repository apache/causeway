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

package org.apache.isis.viewer.dnd.view.border;

import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.viewer.dnd.drawing.Canvas;
import org.apache.isis.viewer.dnd.drawing.Color;
import org.apache.isis.viewer.dnd.drawing.ColorsAndFonts;
import org.apache.isis.viewer.dnd.drawing.Location;
import org.apache.isis.viewer.dnd.drawing.Offset;
import org.apache.isis.viewer.dnd.drawing.Size;
import org.apache.isis.viewer.dnd.interaction.ViewDragImpl;
import org.apache.isis.viewer.dnd.view.DragEvent;
import org.apache.isis.viewer.dnd.view.DragStart;
import org.apache.isis.viewer.dnd.view.Toolkit;
import org.apache.isis.viewer.dnd.view.View;
import org.apache.isis.viewer.dnd.view.base.AbstractBorder;
import org.apache.isis.viewer.dnd.view.base.DragViewOutline;

/**
 * A drag view border provides a line and handle that appears when the mouse
 * moves over the contained view and allows the view to be dragged.
 */
public class DragViewBorder extends AbstractBorder {
    private final int handleWidth = 14;

    public DragViewBorder(final View wrappedView) {
        this(1, wrappedView);
    }

    public DragViewBorder(final int size, final View wrappedView) {
        super(wrappedView);

        top = size;
        left = size;
        bottom = size;
        right = size + handleWidth;
    }

    @Override
    protected void debugDetails(final DebugBuilder debug) {
        debug.append("SimpleBorder " + top + " pixels\n");
        debug.append("           handle " + handleWidth + " pixels");
    }

    @Override
    public DragEvent dragStart(final DragStart drag) {
        if (overBorder(drag.getLocation())) {
            final Location location = drag.getLocation();
            final DragViewOutline dragOverlay = new DragViewOutline(getView());
            return new ViewDragImpl(this, new Offset(location.getX(), location.getY()), dragOverlay);
        } else {
            return super.dragStart(drag);
        }
    }

    @Override
    public void entered() {
        getState().setContentIdentified();
        getState().setViewIdentified();
        wrappedView.entered();
        markDamaged();
    }

    @Override
    public void exited() {
        getState().clearObjectIdentified();
        getState().clearViewIdentified();
        wrappedView.exited();
        markDamaged();
    }

    @Override
    public void draw(final Canvas canvas) {
        if (getState().isViewIdentified()) {
            final Color color = Toolkit.getColor(ColorsAndFonts.COLOR_SECONDARY2);
            final Size s = getSize();
            final int width = s.getWidth();
            for (int i = 0; i < left; i++) {
                canvas.drawRectangle(i, i, width - 2 * i - 1, s.getHeight() - 2 * i - 1, color);
            }
            final int w2 = width - left - 2;
            final int w3 = w2 - handleWidth;
            for (int x = w2; x > w3; x -= 2) {
                canvas.drawLine(x, top, x, s.getHeight() - top, color);
            }
        }
        super.draw(canvas);
    }

    @Override
    public String toString() {
        return wrappedView.toString() + "/SimpleBorder";
    }
}
