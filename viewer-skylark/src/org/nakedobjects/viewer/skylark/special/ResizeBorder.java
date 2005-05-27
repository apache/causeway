package org.nakedobjects.viewer.skylark.special;

import org.nakedobjects.viewer.skylark.Bounds;
import org.nakedobjects.viewer.skylark.Canvas;
import org.nakedobjects.viewer.skylark.Color;
import org.nakedobjects.viewer.skylark.Drag;
import org.nakedobjects.viewer.skylark.DragStart;
import org.nakedobjects.viewer.skylark.InternalDrag;
import org.nakedobjects.viewer.skylark.Location;
import org.nakedobjects.viewer.skylark.Shape;
import org.nakedobjects.viewer.skylark.Size;
import org.nakedobjects.viewer.skylark.View;
import org.nakedobjects.viewer.skylark.ViewAreaType;
import org.nakedobjects.viewer.skylark.core.AbstractBorder;

import org.apache.log4j.Logger;


public class ResizeBorder extends AbstractBorder {
    private static final Logger LOG = Logger.getLogger(ResizeBorder.class);
    public static final int BORDER_WIDTH = 5;
    private Size resize;
    private int direction;
    private boolean resizing;
    private int onBorder;

    // TODO allow a minimum and maximum sizes to be specified and then ensure the user doesn't go outside them.
    // TODO allow the direction of resizing be limited, ie left/right only 
    public ResizeBorder(View view) {
        super(view);
        bottom = right = BORDER_WIDTH;
    }

    protected void debugDetails(StringBuffer b) {
        super.debugDetails(b);
        b.append("\n           resized to " + resize);
    }

    public void draw(Canvas canvas) {
        Size size = getSize();
        int width = size.getWidth();
        int height = size.getHeight();
        if (resizing) {
            Shape shape = new Shape(0, 0);
            int resizeMarkerSize = 10;
            shape.addLine(resizeMarkerSize, 0);
            shape.addLine(0, resizeMarkerSize);
            shape.addLine(-resizeMarkerSize, -resizeMarkerSize);
            Color color = new Color(0xffff00);
            canvas.drawSolidShape(shape, size.getWidth() - resizeMarkerSize, size.getHeight(), color);
            canvas.drawRectangle(0, 0, size.getWidth() - 1, size.getHeight() - 1, color);
        }
        
        Canvas subCanvas = canvas.createSubcanvas(0,0, width - BORDER_WIDTH, height - BORDER_WIDTH);
        wrappedView.draw(subCanvas);
    }

    public ViewAreaType viewAreaType(Location mouseLocation) {
        if(isOnBorder()) {
            return ViewAreaType.INTERNAL;
        }
        return super.viewAreaType(mouseLocation);
    }

    
    public Drag dragStart(DragStart drag) {
        Location location = drag.getLocation();
        if(overBorder(location)) {
	        direction = onBorder(location);
	        if (direction > 0) {
	            return new ResizeDrag(this, new Bounds(getAbsoluteLocation(), getView().getSize()), direction);
	        }
	        return null;
        } else {
            return super.dragStart(drag);
        }
    }

    public void drag(InternalDrag drag) {
        ViewResizeOutline outline = ((ViewResizeOutline) drag.getOverlay());
        if(outline == null) {
            super.drag(drag);
        }		
    }

    public void dragTo(InternalDrag drag) {
        getViewManager().showDefaultCursor();
        ViewResizeOutline outline = ((ViewResizeOutline) drag.getOverlay());
        if(outline != null) {
            getView().setRequiredSize(outline.getSize());
            Logger.getLogger(getClass()).debug("resizing view " + resize);
            invalidateLayout();
        } else {
            super.dragTo(drag);
        }
    }
    
    public Size getRequiredSize() {
        if(resize == null ) {
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

    /**
     * Detects wheter the point is on the resize border, and if so changes
     * the cursor to show it can be resized.
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
            LOG.debug("on resize border " + onBorder + " " + resizing);
        }
        this.onBorder = onBorder;
    }
    
    public void exited() {
        getViewManager().showDefaultCursor();
        resizing = false;
        LOG.debug("off resize border " + onBorder + " " + resizing);
        super.exited();
    }

    private int onBorder(Location at) {
        Bounds area = contentArea();
        boolean right = at.getX() >= area.getWidth() && at.getX() <= area.getWidth() + getRight();
        boolean bottom = at.getY() >= area.getHeight() && at.getY() <= area.getHeight() + getBottom();
        
        final int status;
        if(right && bottom) {
            status = ResizeDrag.BOTTOM_RIGHT;
        } else if(right) {
            status = ResizeDrag.RIGHT;
        } else if(bottom) {
            status = ResizeDrag.BOTTOM;
        } else {
            status = 0;
        }
        
        return status;
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