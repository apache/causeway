package org.nakedobjects.viewer.skylark;

import org.apache.log4j.Logger;

/** 
 * Details a drag event that affects a view.  The target of a ViewDrag is always the workspace of the source
 * view.
 * 
 * <p>An overlay view, as returned by the pickup() method on the source view, is moved by this 
 * drag descripter so its location follows the pointer.
 */
public class ViewDrag extends Drag {
	private static final Logger LOG = Logger.getLogger(ViewDrag.class);
    protected View dragging;
	private final View workspace;

	public static ViewDrag create(View source, Location absolute, Location relative, int modifiers) {
		ViewDrag drag = new ViewDrag(source, absolute, relative, modifiers);
		
		return drag.dragging == null ? null : drag;
	}
	
	/**
	 * Creates a new drag event.  The source view has its pickup(), and then, exited() methods called on it.  
	 * The view returned by the pickup method becomes this event overlay view, which is moved 
	 * continuously so that it tracks the pointer,
	 * 
	 * @param source	the view over which the pointer was when this event started
 	 * @param absolute  the location within the viewer (the Frame/Applet/Window etc)
 	 * @param relative  the location within the specified view
 	 * @param modifiers  the button and key modifiers (@see java.awt.event.MouseEvent)
	 */
    private ViewDrag(View source, Location absolute, Location relative, int modifiers) {
        super(source, absolute, relative, modifiers);

        workspace = source.getWorkspace().getView();
        dragging = source.pickup(this);
        
        
	    if(dragging != null) {
		    source.exited();
		    LOG.debug("pickup " + this);
		    source.getViewManager().setOverlayView(dragging);
		    source.getViewManager().showMoveCursor();
		    // need to update the view location to reset the pointer location that changed 
		    // during the pickup method above
	        updateLocationWithinViewer(dragging, absolute);
		    setDraggingLocation();
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
        if (dragging != null) {
            dragging.markDamaged();
            setDraggingLocation();
            dragging.markDamaged();
        }
    }
    
    private void setDraggingLocation() {
        LOG.debug("pointer location  " + getPointerLocation());
		Location viewOffset = new Location(getPointerLocation());
		viewOffset.move(-sourceLocation.getX(), -sourceLocation.getY());
		dragging.setLocation(viewOffset);
	}
    
    /**
     * Ends the drag by calling drop() on the workspace.
     */
    protected  void end() {
        workspace.drop(this);
    }

    /**
     * Returns the workspace as the targer view.
     */
    public View getTargetView() {
		return workspace;
	}
    
    public String toString() {
		return "ViewDrag [" + super.toString() + "]";
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