package org.nakedobjects.viewer.skylark;

/**
 * Details a drag event - from drag start to drop,
 */
public abstract class Drag extends PointerEvent {
    /*
    private Offset offset;
    protected final Location sourceLocationWithinView;
    private final Location pointerLocation;
    private Location locationInTarget;
*/
    /**
     * Creates a new drag event object.
     * 
     * @param source
     *                       the view over which the pointer was when this event started
     * @param locationWithinViewer
     *                       the location within the viewer (the Frame/Applet/Window etc)
     * @param location
     *                       the location within the specified view
     * @param mods
     *                       the button and key modifiers (@see java.awt.event.MouseEvent)
     */
    protected Drag(View source, Location locationWithinViewer, Location location, int mods) {
        super(source, location, mods);
  /*
        sourceLocationWithinView = location;
         locationInTarget = new Location(location);
        pointerLocation = new Location(locationWithinViewer);
        */
    }

    /**
     * Indicates the drag has been cancelled; no action should be taken.
     */
    public abstract void cancel();

    /**
     * Indicates that the drag state has changed.
     */
    protected abstract void drag();

    /**
     * Indicates the drag has properly ended (the mouse button has been
     * released)
     *  
     */
    protected abstract void end();
    
    /*
    public Offset getOffset() {
        return offset;
    }

    /**
     * Returns the original location of the pointer within the source view,
     * i.e., where the mouse button was pressed. This location remains constant
     * througout the term of the extended drag event.
     * /
    public Location getSourceLocation() {
        return sourceLocationWithinView;
    }

    /**
     * Returns the view that the mouse was originally over, i.e., the view under
     * the mouse when its button was pressed.
     */
    public View getSourceView() {
        return view;
    }

    /**
     * The current location of the pointer within the target view. This location
     * changes as the drag progresses.
     * /
    public Location getTargetLocation() {
        return locationInTarget;
    }

    public Location getPointerLocation() {
        return pointerLocation;
    }

    /**
     * The view currently under the mouse as it is dragging. The returned view
     * will change as the drag progresses over different views.
     * /
    public abstract View getTargetView();

    /**
     * Translate the location of this event by the specified offset.
     */
    public abstract void move(int x, int y);

    public void move(Offset offset) {
        move(offset.getDeltaX(), offset.getDeltaY());
    }
    /*
    public void setOffset(Offset offset) {
        this.offset = offset;
    }
    /*

    /**
     * Updates the location with the viewer to reflect the mouse pointer's
     * position.
      */
    abstract void updateLocationWithinViewer(Location locationInViewer, View target, Location locationInTarget);
    /*{
        pointerLocation.x = locationInViewer.getX();
        pointerLocation.y = locationInViewer.getY();
        this.locationInTarget = locationInTarget;
    }
    */
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business
 * objects directly to the user. Copyright (C) 2000 - 2003 Naked Objects Group
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
