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

package org.apache.isis.viewer.dnd.view.lookup;

import java.awt.event.KeyEvent;

import org.apache.isis.viewer.dnd.drawing.Canvas;
import org.apache.isis.viewer.dnd.drawing.Color;
import org.apache.isis.viewer.dnd.drawing.ColorsAndFonts;
import org.apache.isis.viewer.dnd.drawing.Location;
import org.apache.isis.viewer.dnd.drawing.Shape;
import org.apache.isis.viewer.dnd.drawing.Size;
import org.apache.isis.viewer.dnd.view.BackgroundTask;
import org.apache.isis.viewer.dnd.view.Click;
import org.apache.isis.viewer.dnd.view.KeyboardAction;
import org.apache.isis.viewer.dnd.view.Toolkit;
import org.apache.isis.viewer.dnd.view.View;
import org.apache.isis.viewer.dnd.view.ViewConstants;
import org.apache.isis.viewer.dnd.view.action.BackgroundWork;
import org.apache.isis.viewer.dnd.view.base.AbstractBorder;

/**
 * Field border that provides a drop-down selector.
 */
public abstract class OpenDropDownBorder extends AbstractBorder {
    private boolean over;

    public OpenDropDownBorder(final View wrappedView) {
        super(wrappedView);
        right = 18;
    }

    protected abstract View createDropDownView();

    @Override
    public void draw(final Canvas canvas) {
        final Size size = getSize();
        final int x = size.getWidth() - right + 5 - ViewConstants.HPADDING;
        final int y = (size.getHeight() - 6) / 2;

        if (isAvailable()) {
            final Shape triangle = new Shape(0, 0);
            triangle.addPoint(6, 6);
            triangle.addPoint(12, 0);

            canvas.drawShape(triangle, x, y, Toolkit.getColor(ColorsAndFonts.COLOR_SECONDARY3));
            if (over) {
                final Color color = over ? Toolkit.getColor(ColorsAndFonts.COLOR_SECONDARY1) : Toolkit.getColor(ColorsAndFonts.COLOR_PRIMARY2);
                canvas.drawSolidShape(triangle, x, y, color);
            }
        }

        super.draw(canvas);
    }

    @Override
    public void exited() {
        if (over) {
            markDamaged();
        }
        over = false;
        super.exited();
    }

    @Override
    public void firstClick(final Click click) {
        final float x = click.getLocation().getX() - 2;
        final float boundary = getSize().getWidth() - right;
        if (x >= boundary) {
            if (isAvailable()) {
                open();
            }
        } else {
            super.firstClick(click);
        }
    }

    @Override
    public Size getRequiredSize(final Size maximumSize) {
        maximumSize.contractWidth(ViewConstants.HPADDING);
        final Size size = super.getRequiredSize(maximumSize);
        return size;
    }

    protected boolean isAvailable() {
        return true;
    }

    @Override
    public boolean canFocus() {
        return isAvailable();
    }

    @Override
    public void keyPressed(final KeyboardAction key) {
        if (key.getKeyCode() == KeyEvent.VK_DOWN && isAvailable()) {
            open();
            key.consume();
        }

        super.keyPressed(key);
    }

    @Override
    public void mouseMoved(final Location at) {
        if (at.getX() >= getSize().getWidth() - right) {
            getFeedbackManager().showDefaultCursor();
            if (!over) {
                markDamaged();
            }
            over = true;
        } else {
            if (over) {
                markDamaged();
            }
            over = false;
            super.mouseMoved(at);
        }
    }

    private void open() {
        BackgroundWork.runTaskInBackground(this, new BackgroundTask() {
            @Override
            public void execute() {
                final View overlay = createDropDownView();
                final Location location = getView().getAbsoluteLocation();
                location.add(getView().getPadding().getLeft() - 1, getSize().getHeight() + 2);
                overlay.setLocation(location);
                getViewManager().setOverlayView(overlay);
            }

            @Override
            public String getDescription() {
                return "";
            }

            @Override
            public String getName() {
                return "Opening selector";
            }
        });
    }

    // TODO move into abstract subclass for selection list
    protected abstract void setSelection(OptionContent selectedContent);
}
