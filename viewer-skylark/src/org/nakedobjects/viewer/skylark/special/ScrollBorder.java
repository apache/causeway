package org.nakedobjects.viewer.skylark.special;

import org.nakedobjects.viewer.skylark.Bounds;
import org.nakedobjects.viewer.skylark.Canvas;
import org.nakedobjects.viewer.skylark.Click;
import org.nakedobjects.viewer.skylark.Color;
import org.nakedobjects.viewer.skylark.IdentifiedView;
import org.nakedobjects.viewer.skylark.InternalDrag;
import org.nakedobjects.viewer.skylark.Location;
import org.nakedobjects.viewer.skylark.Offset;
import org.nakedobjects.viewer.skylark.Size;
import org.nakedobjects.viewer.skylark.Style;
import org.nakedobjects.viewer.skylark.View;
import org.nakedobjects.viewer.skylark.ViewAreaType;
import org.nakedobjects.viewer.skylark.core.AbstractBorder;


public class ScrollBorder extends AbstractBorder {
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

       // Color color = isOnBorder() ? Style.PRIMARY1 : Style.PRIMARY2;
         Color color = Style.PRIMARY1;
        if(horizontalScrollPosition > 0 || horizontalVisibleAmount != contents.getWidth()) {
            canvas.drawSolidRectangle(0, contents.getHeight() + 1, contents.getWidth(),
                    SCROLLBAR_WIDTH - 2, Style.SECONDARY3);
            canvas.drawSolidRectangle(horizontalScrollPosition, contents.getHeight() + 1, horizontalVisibleAmount,
                    SCROLLBAR_WIDTH - 3, color);
            canvas.drawRectangle(horizontalScrollPosition, contents.getHeight() + 1, horizontalVisibleAmount,
                    SCROLLBAR_WIDTH - 3, Style.SECONDARY1);
        }
        canvas.drawRectangle(0, contents.getHeight(), contents.getWidth(),
                SCROLLBAR_WIDTH - 1, Style.SECONDARY2);
        
        if(verticalScrollPosition > 0 || verticalVisibleAmount != contents.getHeight()) {
            canvas.drawSolidRectangle(contents.getWidth() + 1, 0, 
                    SCROLLBAR_WIDTH - 2, contents.getHeight(),  Style.SECONDARY3);
            canvas.drawSolidRectangle(contents.getWidth() + 1, verticalScrollPosition, 
                    SCROLLBAR_WIDTH - 3, verticalVisibleAmount,             color);
            canvas.drawRectangle(contents.getWidth() + 1, verticalScrollPosition, 
                    SCROLLBAR_WIDTH - 3, verticalVisibleAmount,  Style.SECONDARY1);
        }
        canvas.drawRectangle(contents.getWidth(), 0, 
                SCROLLBAR_WIDTH - 1, contents.getHeight(),  Style.SECONDARY2);

        Offset offset = offset();
        canvas.setClip(offset.getDeltaX(), offset.getDeltaY(), contents.getWidth(), contents.getHeight());
        super.draw(canvas);
    }
	
    public Size getSize() {
        return new Size(size);
    }

    public void setSize(Size size) {
        // TODO need to restore the offset after size change
  //      float verticalRatio = ((float) verticalScrollPosition) / contentArea().getHeight();

        this.size = new Size(size);
        Bounds displayArea = contentArea();
        Size contentSize = super.getRequiredSize();

        int displayHeight = displayArea.getHeight();
        int contentHeight = Math.max(displayHeight, contentSize.getHeight());
        verticalVisibleAmount = displayHeight * displayHeight / contentHeight;
        verticalMinimum = 0;
        verticalMaximum = displayHeight - verticalVisibleAmount;
 //       verticalScrollPosition = (int) (verticalScrollPosition * verticalRatio);

        int displayWidth = displayArea.getWidth();
        int contentWidth = Math.max(displayWidth, contentSize.getWidth());
        horizontalVisibleAmount = displayWidth * displayWidth / contentWidth;
        horizontalMinimum = 0;
        horizontalMaximum = displayWidth - horizontalVisibleAmount;
        
        wrappedView.setSize(wrappedView.getRequiredSize());
    }

    public void setVerticalPostion(final int position) {
        getViewManager().getSpy().addAction("Move to vertical position " + position);
        verticalScrollPosition = position;
        verticalScrollPosition = Math.min(verticalScrollPosition, verticalMaximum);
        verticalScrollPosition = Math.max(verticalScrollPosition, verticalMinimum);
        markDamaged();
    }

    public void setHorizontalPostion(final int position) {
        getViewManager().getSpy().addAction("Move to horizontal position " + position);
        horizontalScrollPosition = position;
        horizontalScrollPosition = Math.min(horizontalScrollPosition, horizontalMaximum);
        horizontalScrollPosition = Math.max(horizontalScrollPosition, horizontalMinimum);
        markDamaged();
    }

    public ViewAreaType viewAreaType(Location location) {
        if (overContent(location)) {
            addOffset(location);
            return super.viewAreaType(location);
        } else {
            return ViewAreaType.INTERNAL;
        }
    }

    private void addOffset(Location location) {
        location.add(offset());
    }

    /**
     * Moves the scrollbar to beginning or the end when a double click occurs on
     * that side.
     */
    public void secondClick(Click click) {
        Bounds contents = contentArea();
        int x = click.getLocation().getX();
        int y = click.getLocation().getY();

        if (x >= contents.getWidth()) {
            int position = (y < contents.getHeight() / 2) ? verticalMinimum : verticalMaximum;
            setVerticalPostion(position);
        } else if (y >= contents.getHeight()) {
            int position = (x < contents.getWidth() / 2) ? horizontalMinimum : horizontalMaximum;
            setHorizontalPostion(position);
        } else {
            addOffset(click);
            super.firstClick(click);
        }
    }

    public void firstClick(Click click) {
        Bounds contents = contentArea();
        int x = click.getLocation().getX();
        int y = click.getLocation().getY();

        if (x >= contents.getWidth()) {
            if (click.isButton2()) {
                setVerticalPostion(y - verticalVisibleAmount / 2);
            } else {
                if (y < verticalScrollPosition) {
                    setVerticalPostion(verticalScrollPosition - verticalVisibleAmount);
                } else if (y > verticalScrollPosition + verticalVisibleAmount) {
                    setVerticalPostion(verticalScrollPosition + verticalVisibleAmount);
                }
            }

        } else if (y >= contents.getHeight()) {
            if (click.isButton2()) {
                setHorizontalPostion(x - horizontalVisibleAmount / 2);
            } else {
                if (x < horizontalScrollPosition) {
                    setHorizontalPostion(horizontalScrollPosition - horizontalVisibleAmount);
                } else if (x > horizontalScrollPosition + horizontalVisibleAmount) {
                    setHorizontalPostion(horizontalScrollPosition + horizontalVisibleAmount);
                }
            }

        } else {
            addOffset(click);
            super.firstClick(click);
        }
    }

    private void addOffset(Click click) {
        click.add(offset());
    }

    private Offset offset() {
        Bounds contents = contentArea();
        int x = horizontalScrollPosition * wrappedView.getRequiredSize().getWidth() / contents.getWidth();
        int y = verticalScrollPosition * wrappedView.getRequiredSize().getHeight() / contents.getHeight();
        return new Offset(x, y);
    }

    public View dragFrom(InternalDrag drag) {
        Bounds contents = contentArea();
        int x = drag.getRelativeLocation().getX(); // -  getView().getPadding().getLeft();
        int y = drag.getRelativeLocation().getY(); // -  getView().getPadding().getTop();

        dragOffset = -1;
        if (x >= contents.getWidth()) {
            if (y > verticalScrollPosition && y < verticalScrollPosition + verticalVisibleAmount) {
                dragOffset = y - verticalScrollPosition;
                verticalDrag = true;
            }
            return null;

        } else if (y >= contents.getHeight()) {
            if (x > horizontalScrollPosition && x < horizontalScrollPosition + horizontalVisibleAmount) {
                dragOffset = x - horizontalScrollPosition;
                verticalDrag = false;
            }
            return null;

        } else {
            drag.add(offset());
            return super.dragFrom(drag);
        }
    }

    public void drag(InternalDrag drag) {
        if (dragOffset == -1) {
            drag.add(offset());
            super.drag(drag);
        } else {
            if (verticalDrag) {
                int y = drag.getRelativeLocation().getY(); // -  getView().getPadding().getTop();
                setVerticalPostion(y - dragOffset);
            } else {
                int x = drag.getRelativeLocation().getX(); // -  getView().getPadding().getLeft();
                setHorizontalPostion(x - dragOffset);
            }
        }
    }

    public IdentifiedView identify(Location locationWithinViewer, Offset offset) {
        Location locationWithinBorder = new Location(locationWithinViewer);
        locationWithinBorder.translate(offset);
        getViewManager().getSpy().addTrace(this, "mouse location within border", locationWithinBorder);
        getViewManager().getSpy().addTrace(this, "non border area", contentArea());

       if(overBorder(locationWithinBorder)) {
            getViewManager().getSpy().addTrace(this, "over border area", contentArea());
            return new IdentifiedView(getView(), locationWithinViewer, getLocation());
        } else {
            offset.add(-left, -top);
	        offset.add(offset().getDeltaX(), offset().getDeltaY());
            return  wrappedView.identify(locationWithinViewer, offset);
        }
    }
    
    public void mouseMoved(Location location) {
        if (contentArea().contains(location)) {
            addOffset(location);
        }
        super.mouseMoved(location);
    }

    public void markDamaged(Bounds bounds) {
        Offset offset = offset();
        bounds.translate(-offset.getDeltaX(), -offset.getDeltaY());
        super.markDamaged(bounds);
    }
    
    public Location getLocationWithinViewer() {
        Location location = super.getLocationWithinViewer();
        getViewManager().getSpy().addTrace(this, "original location ", location);

        Offset offset = offset();
        getViewManager().getSpy().addTrace(this, "scroll offset ", offset);

        location.move(-offset.getDeltaX(), -offset.getDeltaY());
        getViewManager().getSpy().addTrace(this, "new location ", location);
        return location;
       }

  }

/*
 * Naked Objects - a framework that exposes behaviourally complete business
 * objects directly to the user. Copyright (C) 2000 - 2004 Naked Objects Group
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