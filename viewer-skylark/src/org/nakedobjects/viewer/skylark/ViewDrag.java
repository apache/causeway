package org.nakedobjects.viewer.skylark;

/**
 * Details a drag event that affects a view. The target of a ViewDrag is always
 * the workspace of the source view.
 * 
 * <p>
 * An overlay view, as returned by the pickup() method on the source view, is
 * moved by this drag objects so its location follows the pointer by an offset
 * equivalent to the mouse location within the view.
 */
public class ViewDrag extends Drag {
    private final View dragView;
    private Location location;
    /**
     * Offset from the view's top-left corner to the pointer (relative to the
     * view).
     */
    private final Offset overlayOffset;
    private final View view;
    private final View viewsDecoratedWorkspace;
    private final Workspace viewsWorkspace;

    /**
     * Creates a new drag event. The source view has its pickup(), and then,
     * exited() methods called on it. The view returned by the pickup method
     * becomes this event overlay view, which is moved continuously so that it
     * tracks the pointer.
     * 
     * @param view
     *                       the view over which the pointer was when this event started
     */
    public ViewDrag(View view, Offset offset, View dragView) {
        this.view = view;
        this.dragView = dragView;
        this.overlayOffset = offset;

        viewsWorkspace = view.getParent().getWorkspace();
        viewsDecoratedWorkspace = viewsWorkspace.getView();
    }

    /**
     * Cancel drag by changing cursor back to pointer.
     */
    protected void cancel(Viewer viewer) {
        getSourceView().getViewManager().showDefaultCursor();
    }

    /**
     * Moves the overlay view so it follows the pointer
     */
    protected void drag(Viewer viewer) {
        if (dragView != null) {
            dragView.markDamaged();
            updateDraggingLocation();
            dragView.markDamaged();
        }
    }

    protected void drag(Viewer viewer, Location location, int mods) {
        this.location = location;
        if (dragView != null) {
            dragView.markDamaged();
            updateDraggingLocation();
            dragView.markDamaged();
        }
    }

    /**
     * Ends the drag by calling drop() on the workspace.
     */
    protected void end(Viewer viewer) {
        viewsDecoratedWorkspace.drop(this);
    }

    public View getOverlay() {
        return dragView;
    }

    public Location getLocation() {
        return location;
    }

    public View getSourceView() {
        return view;
    }

    public Location getViewDropLocation() {
        Location viewLocation = new Location(location);
        viewLocation.subtract(overlayOffset);
        viewLocation.subtract(viewsDecoratedWorkspace.getAbsoluteLocation());
        viewLocation.move(-viewsDecoratedWorkspace.getPadding().left, -viewsDecoratedWorkspace.getPadding().top);
        return viewLocation;
    }

    protected void start(Viewer viewer) {

    }

    public void subtract(Location location) {
        location.subtract(location);
    }

    public String toString() {
        return "ViewDrag [" + super.toString() + "]";
    }

    private void updateDraggingLocation() {
        Location viewLocation = new Location(location);
        viewLocation.subtract(overlayOffset);
        dragView.setLocation(viewLocation);
        dragView.limitBoundsWithin(new Bounds(viewsWorkspace.getSize()));
//        viewsWorkspace.limitBounds(dragView);
    }

    public void subtract(int x, int y) {
        location.subtract(x, y);
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