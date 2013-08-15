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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.viewer.dnd.drawing.Bounds;
import org.apache.isis.viewer.dnd.drawing.Canvas;
import org.apache.isis.viewer.dnd.drawing.Location;
import org.apache.isis.viewer.dnd.drawing.Size;
import org.apache.isis.viewer.dnd.view.DragEvent;
import org.apache.isis.viewer.dnd.view.DragStart;
import org.apache.isis.viewer.dnd.view.InternalDrag;
import org.apache.isis.viewer.dnd.view.UserActionSet;
import org.apache.isis.viewer.dnd.view.View;
import org.apache.isis.viewer.dnd.view.ViewAreaType;
import org.apache.isis.viewer.dnd.view.Workspace;
import org.apache.isis.viewer.dnd.view.base.AbstractBorder;
import org.apache.isis.viewer.dnd.view.option.UserActionAbstract;

public abstract class ResizeBorder extends AbstractBorder {
    private static final Logger LOG = LoggerFactory.getLogger(ResizeBorder.class);
    private static final Logger UI_LOG = LoggerFactory.getLogger("ui." + ResizeBorder.class.getName());
    public static final int LEFT = 1;
    public static final int RIGHT = 2;
    public static final int UP = 4;
    public static final int DOWN = 8;
    private int width;
    private int height;
    private int requiredDirection;
    private final int allowDirections;
    protected boolean resizing;
    private int onBorder;

    // TODO allow a minimum and maximum sizes to be specified and then ensure
    // the user doesn't go outside them.
    public ResizeBorder(final View view, final int allowDirections, final int widthOnMovingSides, final int widthOnStaticSides) {
        super(view);
        this.allowDirections = allowDirections;
        top = canExtend(UP) ? widthOnMovingSides : widthOnStaticSides;
        bottom = canExtend(DOWN) ? widthOnMovingSides : widthOnStaticSides;
        left = canExtend(LEFT) ? widthOnMovingSides : widthOnStaticSides;
        right = canExtend(RIGHT) ? widthOnMovingSides : widthOnStaticSides;

    }

    @Override
    protected void debugDetails(final DebugBuilder debug) {
        super.debugDetails(debug);
        debug.appendln("width", width == 0 ? "no change" : Integer.toString(width));
        debug.appendln("height ", height == 0 ? "no change" : Integer.toString(height));
        debug.appendln("resizable ", (canExtend(UP) ? "Up " : "") + (canExtend(DOWN) ? "Down " : "") + (canExtend(LEFT) ? "Left " : "") + (canExtend(RIGHT) ? "Right " : ""));
    }

    @Override
    public void draw(final Canvas canvas) {
        final Size size = getSize();
        final int width = size.getWidth();
        final int height = size.getHeight();
        drawResizeBorder(canvas, size);

        final Canvas subCanvas = canvas.createSubcanvas(left, top, width - left - right, height - top - bottom);
        wrappedView.draw(subCanvas);
    }

    protected abstract void drawResizeBorder(final Canvas canvas, final Size size);

    @Override
    public ViewAreaType viewAreaType(final Location mouseLocation) {
        if (isOnBorder()) {
            return ViewAreaType.INTERNAL;
        }
        return super.viewAreaType(mouseLocation);
    }

    @Override
    public void viewMenuOptions(final UserActionSet menuOptions) {
        super.viewMenuOptions(menuOptions);
        menuOptions.add(new UserActionAbstract("Clear resizing") {
            @Override
            public void execute(final Workspace workspace, final View view, final Location at) {
                width = 0;
                height = 0;
                invalidateLayout();
            }
        });
    }

    @Override
    public DragEvent dragStart(final DragStart drag) {
        final Location location = drag.getLocation();
        if (overBorder(location)) {
            requiredDirection = onBorder(location);
            if (requiredDirection > 0) {
                return new ResizeDrag(this, new Bounds(getAbsoluteLocation(), getView().getSize()), requiredDirection);
            }
            return null;
        } else {
            return super.dragStart(drag);
        }
    }

    @Override
    public void drag(final InternalDrag drag) {
        final ViewResizeOutline outline = ((ViewResizeOutline) drag.getOverlay());
        if (outline == null) {
            super.drag(drag);
        }
    }

    @Override
    public void dragTo(final InternalDrag drag) {
        getFeedbackManager().showDefaultCursor();
        final ViewResizeOutline outline = ((ViewResizeOutline) drag.getOverlay());
        if (outline != null) {
            resizing = false;
            onBorder = 0;

            if (requiredDirection == ResizeDrag.RIGHT || requiredDirection == ResizeDrag.BOTTOM_RIGHT) {
                width = outline.getSize().getWidth();
            }
            if (requiredDirection == ResizeDrag.BOTTOM || requiredDirection == ResizeDrag.BOTTOM_RIGHT) {
                height = outline.getSize().getHeight();
            }

            LOG.debug("resizing view " + width + "," + height);
            invalidateLayout();
        } else {
            super.dragTo(drag);
        }
    }

    @Override
    public Size getRequiredSize(final Size maximumSize) {
        maximumSize.contract(getLeft() + getRight(), getTop() + getBottom());
        if (width > 0 && maximumSize.getWidth() > width) {
            maximumSize.setWidth(width);
        }
        if (height > 0 && maximumSize.getHeight() > height) {
            maximumSize.setHeight(height);
        }
        final Size size = wrappedView.getRequiredSize(maximumSize);
        size.extend(getLeft() + getRight(), getTop() + getBottom());
        if (width > 0) {
            size.setWidth(width);
        }
        if (height > 0) {
            size.setHeight(height);
        }
        return size;
    }

    /**
     * Detects whether the point is on the resize border, and if so changes the
     * cursor to show it can be resized.
     */
    @Override
    public void mouseMoved(final Location at) {
        final int onBorder = onBorder(at);
        if (this.onBorder != onBorder) {
            switch (onBorder) {
            case ResizeDrag.RIGHT:
                getFeedbackManager().showResizeRightCursor();
                resizing = true;
                markDamaged();
                break;

            case ResizeDrag.BOTTOM:
                getFeedbackManager().showResizeDownCursor();
                resizing = true;
                markDamaged();
                break;

            case ResizeDrag.BOTTOM_RIGHT:
                getFeedbackManager().showResizeDownRightCursor();
                resizing = true;
                markDamaged();
                break;

            default:
                getFeedbackManager().showDefaultCursor();
                super.mouseMoved(at);
                resizing = false;
                markDamaged();
                break;
            }
            UI_LOG.debug("on resize border " + onBorder + " " + resizing);
        }
        this.onBorder = onBorder;
    }

    @Override
    public void exited() {
        getFeedbackManager().showDefaultCursor();
        resizing = false;
        onBorder = 0;
        markDamaged();
        UI_LOG.debug("off resize border " + onBorder + " " + resizing);
        super.exited();
    }

    private int onBorder(final Location at) {
        final Bounds area = contentArea();
        final boolean right = canExtend(RIGHT) && at.getX() >= area.getWidth() && at.getX() <= area.getWidth() + getRight();
        final boolean bottom = canExtend(DOWN) && at.getY() >= area.getHeight() && at.getY() <= area.getHeight() + getBottom();

        final int status;
        if (right && bottom) {
            status = ResizeDrag.BOTTOM_RIGHT;
        } else if (right) {
            status = ResizeDrag.RIGHT;
        } else if (bottom) {
            status = ResizeDrag.BOTTOM;
        } else {
            status = 0;
        }

        return status;
    }

    private boolean canExtend(final int extend) {
        return (extend & allowDirections) == extend;
    }

}
