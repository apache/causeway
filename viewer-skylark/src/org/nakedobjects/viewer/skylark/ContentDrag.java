package org.nakedobjects.viewer.skylark;



/**
 * Details a drag event that affects a view's content (as opposed to the view
 * itself).
 */
public class ContentDrag extends Drag {
    private final View dragView;
    private Location location;
    private View previousTarget;
    private final Content sourceContent;
    private View target;
    private final Workspace workspace;
    private Location offset;
    private final View source;

    /**
     * Creates a new drag event. The source view has its pickup(), and then,
     * exited() methods called on it. The view returned by the pickup method
     * becomes this event overlay view, which is moved continuously so that it
     * tracks the pointer,
     * 
     * @param source
     *                       the view over which the pointer was when this event started
     */
    public ContentDrag(View source, Location offset, View dragView) {
        if(dragView == null) {
            throw new IllegalArgumentException();
        }
        workspace = source.getWorkspace();
        sourceContent = source.getContent();
        this.dragView = dragView;
        this.offset = offset;
        this.source = source.getView();
    }

    /**
     * Cancels drag by calling dragOut() on the current target, and changes the
     * cursor back to the default.
     */
    protected void cancel(Viewer viewer) {
        if (target != null) {
            target.dragOut(this);
        }
        viewer.clearOverlayView();
        viewer.showDefaultCursor();
    }

    protected void drag(Viewer viewer, Location location, int mods) {
        this.location = location;
        target = viewer.identifyView(new Location(location), false);
        this.mods = mods;
   
        
        if (dragView != null) {
            dragView.markDamaged();
            Location location1 = new Location(this.location);
            location1.subtract(offset);
            dragView.setLocation(location1);
            workspace.limitBounds(dragView);
            dragView.markDamaged();
        }

        if (target != previousTarget) {
            if (previousTarget != null) {
                viewer.getSpy().addAction("drag out " + previousTarget);
                previousTarget.dragOut(this);
                previousTarget = null;
            }

            viewer.getSpy().addAction("drag in " + target);
            target.dragIn(this);
            previousTarget = target;
        }
    
    }

    /**
     * Ends the drag by calling drop() on the current target, and changes the
     * cursor back to the default.
     */
    protected void end(Viewer viewer) {
        viewer.getSpy().addAction("drop on " + target);
        target.drop(this);
        viewer.clearOverlayView();
        viewer.showDefaultCursor();
    }

    public View getOverlay() {
        return dragView;
    }

    public View getSource() {
        return source;
    }
    
    /**
     * Returns the Content object from the source view.
     */
    public Content getSourceContent() {
        return sourceContent;
    }

    public Location getTargetLocation() {
        Location location = new Location(this.location);
        location.subtract(target.getAbsoluteLocation());
        return location;
    }
    
    public Location getOffset() {
        return offset;
    }

    /**
     * Returns the current target view.
     */
    public View getTargetView() {
        return target;
    }

    public String toString() {
        return "ContentDrag [" + super.toString() + "]";
    }

    protected void start(Viewer viewer) {}

    public void subtract(int left, int top) {}
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