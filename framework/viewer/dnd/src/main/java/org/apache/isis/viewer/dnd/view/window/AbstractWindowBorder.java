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

package org.apache.isis.viewer.dnd.view.window;

import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.viewer.dnd.drawing.Bounds;
import org.apache.isis.viewer.dnd.drawing.Canvas;
import org.apache.isis.viewer.dnd.drawing.Color;
import org.apache.isis.viewer.dnd.drawing.ColorsAndFonts;
import org.apache.isis.viewer.dnd.drawing.Location;
import org.apache.isis.viewer.dnd.drawing.Offset;
import org.apache.isis.viewer.dnd.drawing.Size;
import org.apache.isis.viewer.dnd.interaction.ViewDragImpl;
import org.apache.isis.viewer.dnd.view.Click;
import org.apache.isis.viewer.dnd.view.DragEvent;
import org.apache.isis.viewer.dnd.view.DragStart;
import org.apache.isis.viewer.dnd.view.Toolkit;
import org.apache.isis.viewer.dnd.view.View;
import org.apache.isis.viewer.dnd.view.ViewState;
import org.apache.isis.viewer.dnd.view.Workspace;
import org.apache.isis.viewer.dnd.view.base.AbstractBorder;
import org.apache.isis.viewer.dnd.view.border.BorderDrawing;

public abstract class AbstractWindowBorder extends AbstractBorder {
    protected static BorderDrawing borderRender;
    protected WindowControl controls[];
    private WindowControl overControl;

    public static void setBorderRenderer(final BorderDrawing borderRender) {
        AbstractWindowBorder.borderRender = borderRender;
    }

    public AbstractWindowBorder(final View enclosedView) {
        super(enclosedView);
        left = borderRender.getLeft();
        right = borderRender.getRight();
        top = borderRender.getTop();
        bottom = borderRender.getBottom();
    }

    @Override
    public void debugDetails(final DebugBuilder debug) {
        super.debugDetails(debug);
        borderRender.debugDetails(debug);
        if (controls.length > 0) {
            debug.appendln("controls:-");
            debug.indent();
            for (final WindowControl control : controls) {
                debug.append(control);
                debug.appendln();
            }
            debug.unindent();
        }
    }

    @Override
    public DragEvent dragStart(final DragStart drag) {
        if (overBorder(drag.getLocation())) {
            final Location location = drag.getLocation();
            final View dragOverlay = Toolkit.getViewFactory().createDragViewOutline(getView());
            return new ViewDragImpl(this, new Offset(location.getX(), location.getY()), dragOverlay);
        } else {
            return super.dragStart(drag);
        }
    }

    protected void setControls(final WindowControl[] controls) {
        this.controls = controls;
    }

    @Override
    public void setSize(final Size size) {
        super.setSize(size);
        layoutControls(size);
    }

    @Override
    public void setBounds(final Bounds bounds) {
        super.setBounds(bounds);
        layoutControls(bounds.getSize());
    }

    private void layoutControls(final Size size) {
        left = borderRender.getLeft();
        right = borderRender.getRight();
        top = borderRender.getTop();
        bottom = borderRender.getBottom();

        borderRender.layoutControls(size, controls);
    }

    @Override
    public void draw(final Canvas canvas) {
        // blank background
        final Bounds bounds = getBounds();
        final Color color = Toolkit.getColor(ColorsAndFonts.COLOR_WINDOW + "." + getSpecification().getName());
        canvas.drawSolidRectangle(1, 1, bounds.getWidth() - 2, bounds.getHeight() - 2, color);

        final boolean hasFocus = containsFocus();
        final ViewState state = getState();
        borderRender.draw(canvas, getSize(), hasFocus, state, controls, title() + " (" + getSpecification().getName() + ")");
        // canvas.drawRectangle(0, 0, getSize().getWidth(),
        // borderRender.getTop(), Toolkit.getColor(0xfff));

        // controls
        for (int i = 0; controls != null && i < controls.length; i++) {
            final Canvas controlCanvas = canvas.createSubcanvas(controls[i].getBounds());
            controls[i].draw(controlCanvas);
        }

        super.draw(canvas);
    }

    protected abstract String title();

    @Override
    public Size getRequiredSize(final Size maximumSize) {
        left = borderRender.getLeft();
        right = borderRender.getRight();
        top = borderRender.getTop();
        bottom = borderRender.getBottom();

        final Size size = super.getRequiredSize(maximumSize);
        borderRender.getRequiredSize(size, title(), controls);
        return size;
    }

    @Override
    public void secondClick(final Click click) {
        final View control = overControl(click.getLocation());
        if (control == null) {
            super.secondClick(click);
        }
    }

    @Override
    public void thirdClick(final Click click) {
        final View control = overControl(click.getLocation());
        if (control == null) {
            super.thirdClick(click);
        }
    }

    @Override
    public void firstClick(final Click click) {
        final View control = overControl(click.getLocation());
        if (control == null) {
            if (overBorder(click.getLocation())) {
                final Workspace workspace = getWorkspace();
                if (workspace != null) {
                    if (click.button2()) {
                        workspace.lower(getView());
                    } else if (click.button1()) {
                        workspace.raise(getView());
                    }
                }
            } else {
                super.firstClick(click);
            }

        } else {
            control.firstClick(click);
        }
    }

    @Override
    public void mouseMoved(final Location at) {
        final WindowControl control = (WindowControl) overControl(at);
        if (control != null) {
            if (control != overControl) {
                control.entered();
                overControl = control;
                return;
            }
        } else {
            if (control != overControl) {
                overControl.exited();
                overControl = null;
                return;
            }
        }
        super.mouseMoved(at);
    }

    private View overControl(final Location location) {
        for (final WindowControl control : controls) {
            if (control.getBounds().contains(location)) {
                return control;
            }
        }
        return null;
    }

}
