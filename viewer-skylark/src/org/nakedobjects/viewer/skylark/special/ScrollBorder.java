package org.nakedobjects.viewer.skylark.special;

import org.nakedobjects.viewer.skylark.Bounds;
import org.nakedobjects.viewer.skylark.Canvas;
import org.nakedobjects.viewer.skylark.Click;
import org.nakedobjects.viewer.skylark.Color;
import org.nakedobjects.viewer.skylark.Drag;
import org.nakedobjects.viewer.skylark.DragStart;
import org.nakedobjects.viewer.skylark.InternalDrag;
import org.nakedobjects.viewer.skylark.Location;
import org.nakedobjects.viewer.skylark.Offset;
import org.nakedobjects.viewer.skylark.SimpleInternalDrag;
import org.nakedobjects.viewer.skylark.Size;
import org.nakedobjects.viewer.skylark.Style;
import org.nakedobjects.viewer.skylark.View;
import org.nakedobjects.viewer.skylark.ViewAreaType;
import org.nakedobjects.viewer.skylark.core.AbstractBorder;
import org.nakedobjects.viewer.skylark.core.AbstractView;

import org.apache.log4j.Logger;


public class ScrollBorder extends AbstractBorder {
    private static final Logger LOG = Logger.getLogger(ScrollBorder.class);
    private static final int SCROLLBAR_WIDTH = 16;

    private int horizontalScrollPosition = 0;
    private Size size = new Size();
    private int verticalScrollPosition = 0;

    private int verticalMinimum;
    private int verticalMaximum;
    private int verticalVisibleAmount;
    private int horizontalMinimum;
    private int horizontalMaximum;
    private int horizontalVisibleAmount;
    private int dragOffset;
    private boolean verticalDrag;

    public ScrollBorder(View view) {
        super(view);
        bottom = right = SCROLLBAR_WIDTH;
        setHorizontalPostion(0);
        setVerticalPostion(0);
     }

    protected void debugDetails(StringBuffer b) {
        super.debugDetails(b);
        b.append("\n           Vertical scrollbar ");
        b.append("\n             position " + verticalScrollPosition);
        b.append("\n             minimum " + verticalMinimum);
        b.append("\n             maximum " + verticalMaximum);
        b.append("\n             visible amount " + verticalVisibleAmount);

        b.append("\n           Horizontal scrollbar ");
        b.append("\n             position " + horizontalScrollPosition);
        b.append("\n             minimum " + horizontalMinimum);
        b.append("\n             maximum " + horizontalMaximum);
        b.append("\n             visible amount " + horizontalVisibleAmount);
        b.append("\n           Offset " + offset());
    }

    public void draw(Canvas canvas) {
         Bounds contents = contentArea();

        int contentWidth = contents.getWidth();
        int contentHeight = contents.getHeight();
        
        drawScrollBars(canvas, contentWidth, contentHeight);
        
        canvas.drawRectangle(contents.getX(), contents.getY(), contents.getWidth(), contents.getHeight(), Color.DEBUG_DRAW_BOUNDS);
        
        drawContent(canvas, contentWidth, contentHeight);
        
        if (AbstractView.debug) {
            Size size = getSize();
            canvas.drawRectangle(0, 0, size.getWidth() - 1, size.getHeight() - 1, Color.DEBUG_VIEW_BOUNDS);
            canvas.drawLine(0, size.getHeight() / 2, size.getWidth() - 1, size.getHeight() / 2, Color.DEBUG_VIEW_BOUNDS);
            canvas.drawLine(0, getBaseline(), size.getWidth() - 1, getBaseline(), Color.DEBUG_BASELINE);
        }

    }
	
    private void drawContent(Canvas canvas, int contentWidth, int contentHeight) {
        Offset offset = offset();
        int x = offset.getDeltaX();
        int y = offset.getDeltaY();
        Canvas subCanvas = canvas.createSubcanvas(-x, -y, contentWidth + x , contentHeight + y);
        subCanvas.offset(left, top);
        wrappedView.draw(subCanvas);
    }

    private void drawScrollBars(Canvas canvas, int contentWidth, int contentHeight) {
        Color color = isOnBorder() ? Style.PRIMARY1 : Style.PRIMARY2;
        
        if(horizontalScrollPosition > left || horizontalVisibleAmount < contentWidth) {
            canvas.drawSolidRectangle(left, contentHeight + top + 1, contentWidth,
                    SCROLLBAR_WIDTH - 2, Style.SECONDARY3);
            canvas.drawSolidRectangle(horizontalScrollPosition, contentHeight + top + 1, horizontalVisibleAmount,
                    SCROLLBAR_WIDTH - 3, color);
            canvas.drawRectangle(horizontalScrollPosition, contentHeight + top + 1, horizontalVisibleAmount,
                    SCROLLBAR_WIDTH - 3, Style.SECONDARY1);
	        canvas.drawRectangle(left, contentHeight + top, contentWidth,
	                SCROLLBAR_WIDTH - 1, Style.SECONDARY2);
        }

        if(verticalScrollPosition > top || verticalVisibleAmount < contentHeight) {
            canvas.drawSolidRectangle(contentWidth + left + 1, top, 
                    SCROLLBAR_WIDTH - 2, contentHeight,  Style.SECONDARY3);
            canvas.drawSolidRectangle(contentWidth + left + 1, verticalScrollPosition, 
                    SCROLLBAR_WIDTH - 3, verticalVisibleAmount,             color);
            canvas.drawRectangle(contentWidth + left + 1, verticalScrollPosition, 
                    SCROLLBAR_WIDTH - 3, verticalVisibleAmount,  Style.SECONDARY1);
	        canvas.drawRectangle(contentWidth + left, top, 
	                SCROLLBAR_WIDTH - 1, contentHeight,  Style.SECONDARY2);
        }
    }

    public Size getSize() {
        return new Size(size);
    }
    
    public void setBounds(Bounds bounds) {
        setLocation(bounds.getLocation());
        setSize(bounds.getSize());
    }
    
    public Bounds getBounds() {
        return new Bounds(getLocation(), getSize());
    }
    
    public void setSize(Size size) {
        // TODO need to restore the offset after size change
  //      float verticalRatio = ((float) verticalScrollPosition) / contentArea().getHeight();

        this.size = new Size(size);
        Bounds displayArea = contentArea();
        Size contentSize = super.getRequiredSize();

        int displayHeight = displayArea.getHeight();
        int contentHeight = Math.max(displayHeight, contentSize.getHeight());
        verticalVisibleAmount = displayHeight * displayHeight / (contentHeight - SCROLLBAR_WIDTH);
        verticalMinimum = top;
        verticalMaximum = displayHeight - verticalVisibleAmount;
 //       verticalScrollPosition = (int) (verticalScrollPosition * verticalRatio);

        int displayWidth = displayArea.getWidth();
        int contentWidth = Math.max(displayWidth, contentSize.getWidth());
        horizontalVisibleAmount = displayWidth * displayWidth / (contentWidth - SCROLLBAR_WIDTH);
        horizontalMinimum = left;
        horizontalMaximum = displayWidth - horizontalVisibleAmount;
        
        wrappedView.setSize(wrappedView.getRequiredSize());
    }

    public void setVerticalPostion(final int position) {
        getViewManager().getSpy().addAction("Move to vertical position " + position);
        verticalScrollPosition = Math.min(position + top, verticalMaximum);
        verticalScrollPosition = Math.max(verticalScrollPosition, verticalMinimum);
        markDamaged();
    }

    public void setHorizontalPostion(final int position) {
        getViewManager().getSpy().addAction("Move to horizontal position " + position);
        horizontalScrollPosition = Math.min(position + left, horizontalMaximum);
        horizontalScrollPosition = Math.max(horizontalScrollPosition, horizontalMinimum);
        markDamaged();
    }
    
     public ViewAreaType viewAreaType(Location location) {
        if (overContent(location)) {
            offsetLocation(location);
            return super.viewAreaType(location);
        } else {
            return ViewAreaType.INTERNAL;
        }
    }

    private void offsetLocation(Location location) {
        location.add(offset());
    }

    /**
     * Moves the scrollbar to beginning or the end when a double click occurs on
     * that side.
     */
    public void secondClick(Click click) {
        int x = click.getLocation().getX();
        int y = click.getLocation().getY();

        Bounds contents = contentArea();
        if (x >= contents.getWidth()) {
            int position = (y < contents.getHeight() / 2) ? verticalMinimum : verticalMaximum;
            setVerticalPostion(position);
        } else if (y >= contents.getHeight()) {
            int position = (x < contents.getWidth() / 2) ? horizontalMinimum : horizontalMaximum;
            setHorizontalPostion(position);
        } else {
            click.add(offset());
            super.secondClick(click);
        }
    }

    public void firstClick(Click click) {
         int x = click.getLocation().getX();
        int y = click.getLocation().getY();

        Bounds contents = contentArea();
        if (x >= contents.getWidth()) {
            if (click.button3()) {
                setVerticalPostion(y - verticalVisibleAmount / 2);
            } else if (click.button1()) {
                if (y < verticalScrollPosition) {
                    setVerticalPostion(verticalScrollPosition - verticalVisibleAmount);
                } else if (y > verticalScrollPosition + verticalVisibleAmount) {
                    setVerticalPostion(verticalScrollPosition + verticalVisibleAmount);
                }
            }

        } else if (y >= contents.getHeight()) {
            if (click.button3()) {
                setHorizontalPostion(x - horizontalVisibleAmount / 2);
            } else if (click.button1()) {
                if (x < horizontalScrollPosition) {
                    setHorizontalPostion(horizontalScrollPosition - horizontalVisibleAmount);
                } else if (x > horizontalScrollPosition + horizontalVisibleAmount) {
                    setHorizontalPostion(horizontalScrollPosition + horizontalVisibleAmount);
                }
            }

        } else {
            click.add(offset());
            super.firstClick(click);
        }
    }

    private Offset offset() {
        Bounds contents = contentArea();
        int x = horizontalScrollPosition * wrappedView.getRequiredSize().getWidth() / contents.getWidth();
        int y = verticalScrollPosition * wrappedView.getRequiredSize().getHeight() / contents.getHeight();
        return new Offset(x, y);
    }

    public Drag dragStart(DragStart drag) {
        Location location = drag.getLocation();
        int x = location.getX();
        int y = location.getY();

        Bounds contents = contentArea();
        dragOffset = -1;
        if (x >= contents.getWidth()) {
            if (y > verticalScrollPosition && y < verticalScrollPosition + verticalVisibleAmount) {
                dragOffset = y - verticalScrollPosition;
                verticalDrag = true;
	            return new SimpleInternalDrag(this, location);
            }

        } else if (y >= contents.getHeight()) {
            if (x > horizontalScrollPosition && x < horizontalScrollPosition + horizontalVisibleAmount) {
                dragOffset = x - horizontalScrollPosition;
                verticalDrag = false;
	            return new SimpleInternalDrag(this, location);
            }

        } else {
            Offset offset = offset();
            drag.add(offset);
            return super.dragStart(drag);
        }
        
        return null;
    }

    public void drag(InternalDrag drag) {
        LOG.debug("drag " + drag);
        if (dragOffset == -1) {
            super.drag(drag);
        } else {
            if (verticalDrag) {
                int y = drag.getLocation().getY();
                setVerticalPostion(y - dragOffset);
            } else {
                int x = drag.getLocation().getX();
                setHorizontalPostion(x - dragOffset);
            }
        }
    }

    public View identify(Location location) {
        getViewManager().getSpy().addTrace(this, "mouse location within border", location);
        getViewManager().getSpy().addTrace(this, "non border area", contentArea());

       if(overBorder(location)) {
            getViewManager().getSpy().addTrace(this, "over border area", contentArea());
            return getView();
        } else {
            location.add(-left, -top);
            location.add(offset().getDeltaX(), offset().getDeltaY());
            return  wrappedView.identify(location);
        }
    }
    
    public void mouseMoved(Location location) {
        LOG.debug("moved " + location);
        if (contentArea().contains(location)) {
            offsetLocation(location);
	        super.mouseMoved(location);
        }
    }

    public void markDamaged(Bounds bounds) {
        Offset offset = offset();
        bounds.translate(-offset.getDeltaX(), -offset.getDeltaY());
        super.markDamaged(bounds);
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