package org.nakedobjects.viewer.skylark;

import org.nakedobjects.utility.NotImplementedException;


/**
 * Details a drag event that is internal to view.
 */
public class InternalDrag extends Drag {

    static InternalDrag create(View source, Location mouseLocation, int modifiers) {
       return new InternalDrag(source, mouseLocation, modifiers);
    }
    
    private final Location absoluteViewLocation;
    private final View dragOverlay;
    private final Location mouseLocation;
    private final Location originalMouseLocation;

    /**
     * Creates a new drag event. The source view has its pickup(), and then,
     * exited() methods called on it. The view returned by the pickup method
     * becomes this event overlay view, which is moved continuously so that it
     * tracks the pointer,
     * @param source
     *                       the view over which the pointer was when this event started
     * @param mouseLocation
     *                       the location within the viewer (the Frame/Applet/Window etc)
     * @param modifiers
     *                       the button and key modifiers (@see java.awt.event.MouseEvent)
     */
    private InternalDrag(View source, Location mouseLocation, int modifiers) {
        super(source, modifiers);
        this.mouseLocation = new Location(mouseLocation);
        originalMouseLocation = new Location(mouseLocation);
        absoluteViewLocation = source.getAbsoluteLocation();
        dragOverlay = source.dragFrom(this);
    }

    protected void cancel() {
        view.dragCancel(this);
    }

    protected void drag() {
        view.drag(this);
    }

    protected void end() {
        view.dragTo(this);
    }

    /**
     * Returns the view that is shown in the overlay to provide feedback about
     * the drag actions..
     */
    public View getDragOverlay() {
        return dragOverlay;
    }

    public Location getMouseLocation() {
        return mouseLocation;
    }

    public Location getMouseLocationRelativeToView() {
        Location location = new Location(mouseLocation);
        location.subtract(absoluteViewLocation);
        return location;
    }

    /**
     * Offset from the location of the mouse when the drag started.
     */
    public Offset getOffset() {
        return mouseLocation.offsetFrom(originalMouseLocation);
    }

    public View getView() {
        return view;
    }

    public void subtract(int x, int y) {
        mouseLocation.subtract(x, y);
    }

    public String toString() {
        return "InternalDrag [location=" + mouseLocation + ",relative=" + getMouseLocationRelativeToView() + ",offset="
                + absoluteViewLocation + "," + super.toString() + "]";
    }

    // TODO remove
    public Offset totalMovement() {
        throw new NotImplementedException();
        //        return new Offset(originalLocationWithinView.getX() -
        // mouseLocation.getX(), originalLocationWithinView.getY() -
        // mouseLocation.getY());
    }

    void update(Location mouseLocation, View target) {
        this.mouseLocation.x = mouseLocation.x;
        this.mouseLocation.y = mouseLocation.y;
    }

    public void subtract(Offset offset) {
        subtract(offset.getDeltaX(), offset.getDeltaY());
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