package org.nakedobjects.viewer.skylark.special;

import org.nakedobjects.viewer.skylark.Bounds;
import org.nakedobjects.viewer.skylark.Canvas;
import org.nakedobjects.viewer.skylark.Drag;
import org.nakedobjects.viewer.skylark.DragStart;
import org.nakedobjects.viewer.skylark.InternalDrag;
import org.nakedobjects.viewer.skylark.Location;
import org.nakedobjects.viewer.skylark.Size;
import org.nakedobjects.viewer.skylark.View;
import org.nakedobjects.viewer.skylark.ViewAreaType;
import org.nakedobjects.viewer.skylark.core.AbstractBorder;

import org.apache.log4j.Logger;


public abstract class ResizeBorder extends AbstractBorder {
    private static final Logger LOG = Logger.getLogger(ResizeBorder.class);
    private static final Logger UI_LOG = Logger.getLogger("ui." + ResizeBorder.class.getName());
    public static final int LEFT = 1;
    public static final int RIGHT = 2;
    public static final int UP = 4;
    public static final int DOWN = 8;
    private Size resize;
    private int requiredDirection;
    private int allowDirections;
 //   private Size minimumSize;
    protected boolean resizing;
    private int onBorder;

    // TODO allow a minimum and maximum sizes to be specified and then ensure
    // the user doesn't go outside them.
    public ResizeBorder(View view, int allowDirections, int width) {
        super(view);
        this.allowDirections = allowDirections;
        top = canExtend(UP) ? width : 0;
        bottom = canExtend(DOWN) ? width : 0;
        left = canExtend(LEFT) ? width : 0;
        right = canExtend(RIGHT) ? width : 0;

    }

    protected void debugDetails(StringBuffer b) {
        super.debugDetails(b);
        b.append("\n           resized to " + resize);
    }

    public void draw(Canvas canvas) {
        Size size = getSize();
        int width = size.getWidth();
        int height = size.getHeight();
        drawResizeBorder(canvas, size);

        Canvas subCanvas = canvas.createSubcanvas(0, 0, width - left - right, height - top - bottom);
        wrappedView.draw(subCanvas);
    }

    protected abstract void drawResizeBorder(Canvas canvas, Size size);

    public ViewAreaType viewAreaType(Location mouseLocation) {
        if (isOnBorder()) {
            return ViewAreaType.INTERNAL;
        }
        return super.viewAreaType(mouseLocation);
    }

    public Drag dragStart(DragStart drag) {
        Location location = drag.getLocation();
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

    public void drag(InternalDrag drag) {
        ViewResizeOutline outline = ((ViewResizeOutline) drag.getOverlay());
        if (outline == null) {
            super.drag(drag);
        }
    }

    public void dragTo(InternalDrag drag) {
        getViewManager().showDefaultCursor();
        ViewResizeOutline outline = ((ViewResizeOutline) drag.getOverlay());
        if (outline != null) {
            resizing = false;
            onBorder = 0;
            getView().setRequiredSize(outline.getSize());
            LOG.debug("resizing view " + resize);
            invalidateLayout();
        } else {
            super.dragTo(drag);
        }
    }

    public Size getRequiredSize() {
        if (resize == null) {
            Size size = wrappedView.getRequiredSize();
            size.extend(getLeft() + getRight(), getTop() + getBottom());
            return size;
        } else {
            return new Size(resize);
        }
    }

    public void setRequiredSize(Size size) {
        this.resize = size;
    }

    /*public void setMinimumSize(Size minimumSize) {
        this.minimumSize = minimumSize;
    }*/

    /**
     * Detects wheter the point is on the resize border, and if so changes the
     * cursor to show it can be resized.
     */
    public void mouseMoved(Location at) {
        int onBorder = onBorder(at);
        if (this.onBorder != onBorder) {
            switch (onBorder) {
            case ResizeDrag.RIGHT:
                getViewManager().showResizeRightCursor();
                resizing = true;
                markDamaged();
                break;

            case ResizeDrag.BOTTOM:
                getViewManager().showResizeDownCursor();
                resizing = true;
                markDamaged();
                break;

            case ResizeDrag.BOTTOM_RIGHT:
                getViewManager().showResizeDownRightCursor();
                resizing = true;
                markDamaged();
                break;

            default:
                getViewManager().showDefaultCursor();
                super.mouseMoved(at);
                resizing = false;
                markDamaged();
                break;
            }
            UI_LOG.debug("on resize border " + onBorder + " " + resizing);
        }
        this.onBorder = onBorder;
    }

    public void exited() {
        getViewManager().showDefaultCursor();
        resizing = false;
        onBorder = 0;
        markDamaged();
        UI_LOG.debug("off resize border " + onBorder + " " + resizing);
        super.exited();
    }

    private int onBorder(Location at) {
        Bounds area = contentArea();
        boolean right = canExtend(RIGHT) && at.getX() >= area.getWidth() && at.getX() <= area.getWidth() + getRight();
        boolean bottom = canExtend(DOWN) && at.getY() >= area.getHeight() && at.getY() <= area.getHeight() + getBottom();

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

    private boolean canExtend(int extend) {
        return (extend & allowDirections) == extend;
    }

}

/*
 * Naked Objects - a framework that exposes behaviourally complete business
 * objects directly to the user. Copyright (C) 2000 - 2005 Naked Objects Group
 * Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address
 * of Naked Objects Group is Kingsway House, 123 Goldworth Road, Woking GU21
 * 1NR, UK).
 */