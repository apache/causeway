package org.nakedobjects.viewer.skylark;

/**
 * Describes a mouse click event.
 */
public class Click extends PointerEvent {
    private final Location locationWithinView;
    private final Location mouseLocation;
    private final Location workspaceOffset;

    /**
     * Creates a new click event object.
     * 
     * @param source
     *                       the view over which the pointer was when this click occurred
     * @param locationWithinView
     *                       the location within the specified view
     * @param modifiers
     *                       the button and key held down during the click (@see
     *                       java.awt.event.MouseEvent)
     */
    public Click(View source, Location locationWithinView, Location mouseLocation, int modifiers) {
        super(source, locationWithinView, modifiers);

        this.locationWithinView = new Location(locationWithinView);
        this.mouseLocation = new Location(mouseLocation);

        View parent = source.getParent();
        if(parent == null) {
            workspaceOffset = new Location(0, 0);
        } else {
	        View viewsWorkspace = parent.getWorkspace().getView();
	        workspaceOffset = viewsWorkspace.getAbsoluteLocation();
	        Padding padding = viewsWorkspace.getPadding();
            workspaceOffset.move(padding.getLeft(), padding.getTop());
        }
    }

    /**
     * Returns the location of the mouse, within the view that was clicked on.
     */
    public Location getLocation() {
        return locationWithinView;
    }

    public Location getMouseLocation() {
        return mouseLocation;
    }
    
    public Location getLocationWithinWorkspace() {
        Location location = new Location(mouseLocation);
        location.subtract(workspaceOffset);
        return location;
    }

    /**
     * Returns the view that clicked on.
     */
    public View getView() {
        return view;
    }

    /**
     * Translate the location of this event by the specified offset.
     */
    public void subtract(int x, int y) {
        locationWithinView.subtract(x, -y);
    }

    public String toString() {
        return "Click [type=" + type + "," + super.toString() + "]";
    }

    public void add(Offset offset) {
        locationWithinView.add(offset.getDeltaX(), offset.getDeltaY());
    }
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