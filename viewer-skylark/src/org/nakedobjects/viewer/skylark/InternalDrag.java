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
package org.nakedobjects.viewer.skylark;


/** 
 * Details a drag event that is internal to view.
 */
public class InternalDrag extends Drag {
    protected View overlay;

	public static InternalDrag create(View source, Location absolute, Location relative, int modifiers) {
		InternalDrag drag = new InternalDrag(source, absolute, relative, modifiers);
		
		return drag;
	}
	
   
    /**
	 * Creates a new drag event.  The source view has its pickup(), and then, exited() methods called on it.  
	 * The view returned by the pickup method becomes this event overlay view, which is moved 
	 * continuously so that it tracks the pointer,
	 * 
	 * @param source	the view over which the pointer was when this event started
 	 * @param locationWithinViewer  the location within the viewer (the Frame/Applet/Window etc)
 	 * @param location  the location within the specified view
 	 * @param modifiers  the button and key modifiers (@see java.awt.event.MouseEvent)
	 */
    private InternalDrag(View source, Location locationWithinViewer, Location location, int modifiers) {
        super(source, locationWithinViewer, location, modifiers);
        overlay = source.dragFrom(this);
    }
    
    protected void drag() {
    	getTargetView().drag(this);
    }

    protected void end() {
    	getTargetView().dragTo(this);
    }
 
    /**
     * Returns the view that is shown in the overlay to provide feedback about the drag actions..
     */
    public View getDragOverlay() {
        return overlay;
    }
	
    /**
     * Target is always the same as the source.
     */
    public View getTargetView() {
		return getSourceView();
	}

    public String toString() {
    	return "InternalDrag [" + super.toString() + "]";
    }

	public void cancel() {
		getTargetView().dragCancel(this);
	}


    public void move(Offset offset) {
        move(offset.getDeltaX(), offset.getDeltaY());
    }
}
