package org.nakedobjects.viewer.skylark;

import org.apache.log4j.Logger;

/** 
 * Details a drag event that affects a view.  The target of a ViewDrag is always the workspace of the source
 * view.
 * 
 * <p>An overlay view, as returned by the pickup() method on the source view, is moved by this 
 * drag objects so its location follows the pointer by an offset equivalent to the mouse location within the view.
 */
public class ViewDrag extends Drag {
	private static final Logger LOG = Logger.getLogger(ViewDrag.class);
    private final View overlayView;
	private final View viewsWorkspace;
    private Location mouseLocation;
    /**
     * Offset from the view's top-left corner to the pointer (relative to the view).
     */
    private final Offset overlayOffset;

	public static ViewDrag create(View source, Location mouseLocationWithinViewer, Location locationWithinView, int modifiers) {
	    View parent = source.getParent();
        if(parent == null) {
            return null;
        }
	    
		ViewDrag drag = new ViewDrag(source, mouseLocationWithinViewer, locationWithinView, modifiers);
		return drag.overlayView == null ? null : drag;
	}
	
	/**
	 * Creates a new drag event.  The source view has its pickup(), and then, exited() methods called on it.  
	 * The view returned by the pickup method becomes this event overlay view, which is moved 
	 * continuously so that it tracks the pointer,
	 * 
	 * @param source	the view over which the pointer was when this event started
 	 * @param mouseLocationWithinViewer  the location within the viewer (the Frame/Applet/Window etc)
 	 * @param relative  the location within the specified view
 	 * @param modifiers  the button and key modifiers (@see java.awt.event.MouseEvent)
	 */
    private ViewDrag(View source, Location mouseLocationWithinViewer, Location relative, int modifiers) {
        super(source, mouseLocationWithinViewer, relative, modifiers);
        
        overlayView = source.pickup(this);
        overlayOffset = new Offset(relative.getX(), relative.getY());
        viewsWorkspace = source.getParent().getWorkspace().getView();
 
        if(overlayView != null) {
		    source.exited();
		    LOG.debug("pickup " + this);
		    source.getViewManager().setOverlayView(overlayView);
		    source.getViewManager().showMoveCursor();
		    // need to update the view location to reset the pointer location that changed 
		    // during the pickup method above
	        updateLocationWithinViewer(mouseLocationWithinViewer, overlayView, relative);
		    updateDraggingLocation();
	    }
    }

    /**
     * Cancel drag by changing cursor back to pointer.
     */
    public void cancel() {
		getSourceView().getViewManager().showDefaultCursor();
	}
    
    /**
     * Moves the overlay view so it follows the pointer
     */
    protected void drag() {
        if (overlayView != null) {
            overlayView.markDamaged();
            updateDraggingLocation();
            overlayView.markDamaged();
        }
    }
    
    private void updateDraggingLocation() {
        LOG.debug("mouse location  " + mouseLocation);
        Location viewLocation = new Location(mouseLocation);
		viewLocation.subtract(overlayOffset);
		overlayView.setLocation(viewLocation);
	}
    
    /**
     * Ends the drag by calling drop() on the workspace.
     */
    protected  void end() {
        viewsWorkspace.drop(this);
    }

    public String toString() {
		return "ViewDrag [" + super.toString() + "]";
	}

    public void move(int x, int y) {}

    void updateLocationWithinViewer(Location mouseLocation, View target, Location locationInTarget) {
        this.mouseLocation = mouseLocation;
    }

    public Location getViewDropLocation() {
        Location viewLocation = new Location(mouseLocation);
		viewLocation.subtract(overlayOffset);
        viewLocation.subtract(viewsWorkspace.getAbsoluteLocation());
        viewLocation.move(-viewsWorkspace.getPadding().left, -viewsWorkspace.getPadding().top);
        return viewLocation;
    }
}


/*
Naked Objects - a framework that exposes behaviourally complete
business objects directly to the user.
Copyright (C) 2000 - 2003  Naked Objects Group Ltd

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

The authors can be contacted via www.nakedobjects.org (the
registered address of Naked Objects Group is Kingsway House, 123 Goldworth
Road, Woking GU21 1NR, UK).
*/