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
package org.nakedobjects.viewer.lightweight;

import java.util.Enumeration;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedClass;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.collection.InstanceCollection;
import org.nakedobjects.object.collection.InternalCollection;
import org.nakedobjects.object.control.About;
import org.nakedobjects.object.reflect.Action;
import org.nakedobjects.object.reflect.Field;
import org.nakedobjects.security.Session;
import org.nakedobjects.utility.Assert;
import org.nakedobjects.viewer.lightweight.options.CloseViewOption;
import org.nakedobjects.viewer.lightweight.options.DebugOption;
import org.nakedobjects.viewer.lightweight.options.DestroyObjectOption;
import org.nakedobjects.viewer.lightweight.options.FindAllOption;
import org.nakedobjects.viewer.lightweight.options.FindFirstOption;
import org.nakedobjects.viewer.lightweight.options.InvalidateOption;
import org.nakedobjects.viewer.lightweight.options.LayoutOption;
import org.nakedobjects.viewer.lightweight.options.ObjectOption;
import org.nakedobjects.viewer.lightweight.options.OpenViewOption;
import org.nakedobjects.viewer.lightweight.options.ReplaceViewOption;
import org.nakedobjects.viewer.lightweight.util.ViewFactory;


public abstract class AbstractObjectView extends AbstractView implements ObjectView {
    protected static final Logger LOG = Logger.getLogger(AbstractObjectView.class);
    private static final UserAction DESTROY_OPTION = new DestroyObjectOption();
    private static final UserAction FIND_ALL_OPTION = new FindAllOption();
    private static final UserAction FIND_FIRST_OPTION = new FindFirstOption();
	private static final UserAction DEBUG_OPTION = new DebugOption();
    private static final UserAction INVALIDATE_OPTION = new InvalidateOption();
    private static final UserAction LAYOUT_OPTION = new LayoutOption();
    private static final UserAction CLOSE_OPTION = new CloseViewOption();
    private Field fieldOf;
    private NakedObject object;
    private ObjectViewState state = new ObjectViewState();

    /**
     * The object field that this view is for.
     * @return The object field, or null if it is not a field
     */
    public Field getFieldOf() {
        return fieldOf;
    }

    public boolean isIdentified() {
        return getWorkspace().isIdentified(this);
    }

    public NakedObject getObject() {
        return object;
    }

    public Size getRequiredSize() {
        Size size = new Size(0, 0);
        size.addPadding(getPadding());

        return size;
    }

    public View getRoot() {
        return (getParent() == null) ? this : ((AbstractObjectView) getParent()).getRoot();
    }

    public boolean isRoot() {
        return getRoot() == this;
    }

    public void setRootViewIdentified() {
    	if(!state.isRootViewIdentified()) {
    		state.setRootViewIdentified();
    		redraw();
    	}
    }

    public ObjectViewState getState() {
        return state;
    }

    public void clearRootViewIdentified() {
        if(state.isRootViewIdentified()) {
        	state.clearRootViewIdentified();
        	redraw();
        }
    }

    public void collectionAddUpdate(Object collection, NakedObject element) {
    }

    public void collectionRemoveUpdate(Object collection, NakedObject element) {
    }

    public String debugDetails() {
        StringBuffer b = new StringBuffer();
        b.append(super.debugDetails());

        b.append("\nState:     " + (isOpen() ? "OPEN" : "CLOSED"));

        b.append("\nRepl'able: " + (isReplaceable() ? "yes" : "no"));

        b.append("\nField:     ");
        b.append(fieldOf);
        
        MenuOptionSet options = new MenuOptionSet(true);
        menuOptions(options);
		b.append("\nView menu:     ");
		Vector items = options.getMenuOptions(true, true);
		for (int i = 0; i < items.size(); i++) {
			b.append("\n     " + items.elementAt(i));
		}
		
		options = new MenuOptionSet(false);
		menuOptions(options);
		b.append("\nObject menu:     ");
		items = options.getMenuOptions(true, true);
		for (int i = 0; i < items.size(); i++) {
			b.append("\n     " + items.elementAt(i));
		}
        
		b.append("\n");
        
        return b.toString();
    }

    public void dispose() {
        if (getObject() != null) {
            getWorkspace().removeFromNotificationList(this);
        }
        LOG. debug(this + " disposed");
    }

    public void dragCancel(DragHandler drag) {
        getWorkspace().showArrowCursor();
    }

    public void dragObjectIn(ObjectDrag drag) {
        NakedObject source = drag.getSourceObject();
        Action target = getObject().getNakedClass().getObjectAction(Action.USER, new NakedClass[] {source.getNakedClass()});
        
        if(target != null) {
	        About about = target.getAbout(Session.getSession().getSecurityContext(), getObject(), source);
	        if (about.canUse().isAllowed()) {
	        	getWorkspace().setStatus(about.getDescription());
	            state.setCanDrop();
	        } else {
	        	getWorkspace().setStatus(about.getDescription() + ": " + about.canUse().getReason());
	            state.setCantDrop();
	        }
        } else {
        	getWorkspace().setStatus("");
        	state.setCantDrop();
        }

        redraw();
    }

    public void dragObjectOut(ObjectDrag drag) {
        state.clearObjectIdentified();
        redraw();
    }

    public void dragging(DragHandler drag) {
        setLocation(drag.getViewLocation());
        limitBounds();
        redraw();
    }

    public void draw(Canvas canvas) {
        super.draw(canvas);

        if (AbstractView.DEBUG) {
            Color color = new Color(0xff0000);

            Padding in = getPadding();
            Size size = getSize();
            canvas.drawRectangle(in.left, in.top, size.width - in.left - in.right - 1,
                size.height - in.top - in.bottom - 1, color);
            canvas.drawRectangle(0, 0, size.width - 1, size.height - 1, color);
        }
    }

    /**
     * Called when a dragged object is dropped onto this view.  The default
     * behaviour implemented here calls the action method on the target, passing the
     * source object in as the only parameter.
     */
    public void dropObject(ObjectDrag drag) {
        Assert.assertTrue(drag.getSource() instanceof ObjectView);

        NakedObject source = ((ObjectView) drag.getSource()).getObject();
        Assert.assertNotNull(source);

        NakedObject target = getObject();
        Assert.assertNotNull(target);

        Action action = target.getNakedClass().getObjectAction(Action.USER, new NakedClass[] {source.getNakedClass()});
        
        if (action != null && action.getAbout(Session.getSession().getSecurityContext(), target, source).canUse().isAllowed()) {
            NakedObject result = action.execute(target, source);

            if (result != null) {
                RootView view = ViewFactory.getViewFactory().createRootView(result);
                Location at = drag.getViewLocation();
                at.translate(50, 20);
                view.setLocation(at);
                getWorkspace().addRootView(view);
            }

            redraw();
        }
    }

    /**
     * Default view drop behaviour that moves a dragged top level window, or creates a new top-level window
     * for a dragged internal view
     */
    public void dropView(ViewDrag drag) {
        getWorkspace().showArrowCursor();

        if (isRoot()) {
			setLocation(drag.getViewLocation());
            calculateRepaintArea();
            getWorkspace().limitBounds(this);
            redraw();
        } else {
            // create a top-level view for a dragged view
            RootView view = ViewFactory.getViewFactory().createRootView(getObject());
            view.setLocation(drag.getViewLocation());
            getWorkspace().addRootView(view);
        }
    }

    public void entered() {
        getWorkspace().setStatus(objectInfo());
        state.setViewIdentified();
		topView().setRootViewIdentified();
        redraw();
    }

    public void enteredSubview() {
        state.clearViewIdentified();
        redraw();
    }

    public void exited() {
        getWorkspace().setStatus("");
        state.clearViewIdentified();
        topView().clearRootViewIdentified();
        redraw();
    }

    public void exitedSubview() {
        state.setViewIdentified();
        redraw();
    }

    /**
     * Raises/lowers this view if it is a root view.  Button 1 raises, button 2 lowers.
     */
    public void firstClick(Click click) {
        super.firstClick(click);

        // raise/lower root view
        if (this instanceof RootView) {
            if (click.isButton1()) {
                getWorkspace().raise((RootView) this);
            } else if (click.isButton2()) {
                getWorkspace().lower((RootView) this);
            }
        }
    }

	public final View makeView(Naked object, Field fieldOf) {
		AbstractObjectView clone;
		
		try {
			clone = (AbstractObjectView) clone();
		} catch (CloneNotSupportedException e) {
			clone = new FallbackView();
			LOG.error("Failed to create view for " + object, e);
		}
		clone.assignId();
		
		clone.object = (NakedObject) object;
		clone.fieldOf = fieldOf;
		clone.state = new ObjectViewState();

		if (clone.object != null) {
			getWorkspace().addNotificationView(clone);
			clone.object.resolve();
		}

		clone.init(clone.object);

		return clone;
	}


    public void menuOptions(MenuOptionSet options) {
		super.menuOptions(options);
    	
        if (options.isForView()) {
            viewMenuOptions(options);

            if (getBorder() != null) {
                getBorder().viewMenuOptions(this, options);
            }

            options.setColor(Style.VIEW_MENU);
        } else {
            objectMenuOptions(options);

            options.setColor(Style.OBJECT_MENU);
        }
        
		options.add(MenuOptionSet.DEBUG, DEBUG_OPTION);
    }

    public void mouseMoved(Location at) {
        if (objectLocatedAt(at)) {
            if (!state.isObjectIdentified()) {
                state.setObjectIdentified();
                redraw();
            }
        } else {
            if (state.isObjectIdentified()) {
                state.clearObjectIdentified();
                redraw();
            }
        }
    }

    /**
     * The information about this object to be displayed on the status bar.
     */
    public String objectInfo() {
        if (getObject() != null) {
            String status = getObject().about().getDescription();

            if (status.equals("")) {
                status = getObject().title() + " (" + getObject().getShortClassName() + ")";
            }

            return status;
        } else {
            return "null";
        }
    }

    public boolean objectLocatedAt(Location mouseLocation) {
        return false;
    }

    public void objectMenuOptions(MenuOptionSet options) {
        NakedObject obj = (NakedObject) getObject();

        if (obj != null) { 
        	if(obj.isFinder()) {
	            options.add(MenuOptionSet.OBJECT, FIND_ALL_OPTION);
	            options.add(MenuOptionSet.OBJECT, FIND_FIRST_OPTION);
	        }

        	ObjectOption.menuOptions((NakedObject) getObject(), options);

	        if (!(obj instanceof NakedClass) && !(obj instanceof InstanceCollection) &&
	                obj.isPersistent()) {
	            options.add(MenuOptionSet.EXPLORATION, DESTROY_OPTION);
	        }
        }
    }
    
    /**
     * Default implementation: creates a new view and displays it.  If the ....
     */
    public void objectMenuReturn(NakedObject returned, Location at) {
    	ViewFactory vf = ViewFactory.getViewFactory();
    	if(vf.hasRootViews(returned)) {
    		RootView newView = vf.createRootView(returned);
    		newView.setLocation(at);
    		getWorkspace().addRootView(newView);
    	} else {
    		DesktopView newView = vf.createIconView(returned, null);
    		at.setX(topView().getAbsoluteLocation().getX() - newView.getSize().getWidth() / 2);
    		newView.setLocation(at);
    		getWorkspace().addIcon(newView);
    	}
    }

    public void objectUpdate(NakedObject object) {
    	invalidateLayout();
    	layout();
        //repaintAll();

        if (getParent() != null) {
            ObjectView p = parentObjectView();
            p.objectUpdate(object);
        }
        
        redraw();
    }

    /**
    * Returns the view that logically contains this view, i.e., for the associated object.  If this view is for a
    * one to one to one association, then the view will be the view up one level in the view tree.  However,
    * if this view is for a one to many association, then the view will be view, not of the internal collection,
    * but of the parent of the collection, i.e. up two levels.
    */
    public ObjectView parentObjectView() {
        ObjectView parent = (ObjectView) getParent();

        if (parent == null) {
            return null;
        } else if (parent.getObject() instanceof InternalCollection) {
            return (ObjectView) parent.getParent();
        } else {
            return parent;
        }
    }

    public DragView pickupObject(ObjectDrag drag) {
        DragView dragView;

        dragView = ViewFactory.getViewFactory().createDragView(getObject());
		dragView.setSize(getSize());
		
        LOG.debug("drag object start " + drag.getViewLocation());
        getWorkspace().setStatus("Dragging " + objectInfo());

        dragView.setLocation(drag.getViewLocation());
 
        return dragView;
    }

    public DragView pickupView(ViewDrag drag) {
        DragView dragView;

        getWorkspace().showMoveCursor();
        dragView = ViewFactory.getViewFactory().createDragOutline(object);
		dragView.setSize(getSize());
		
        LOG.debug("drag view start " + drag.getViewLocation());
		getWorkspace().setStatus("Moving " + objectInfo() + " view");

		dragView.setLocation(drag.getViewLocation());
        getWorkspace().setOverlayView(dragView);

        return dragView;
    }

    public void removeViewsFor(NakedObject object, Vector toRemove) {
    }

    public void secondClick(Click click) {
        if (click.isForView()) {
            if (getParent() != null) {
                if (isReplaceable()) {
                    replaceView((InternalView) ViewFactory.getViewFactory().createInternalView(getObject(),
                            fieldOf, isOpen()));
                }
            } else if (this instanceof DesktopView) {
                RootView view = ViewFactory.getViewFactory().createRootView(getObject());
                view.setLocation(getLocation());
                getWorkspace().addRootView(view);
                getWorkspace().removeView(this);
            } else {
                DesktopView newIcon = ViewFactory.getViewFactory().createIconView(getObject(), null);
                newIcon.setLocation(getLocation());
                getWorkspace().removeView(this);
                getWorkspace().addIcon(newIcon);
            }
        }
    }

    public String toString() {
        Bounds b = getBounds();
        String cls = getClass().getName();

        String bounds = b == null ? "unbounded" : b.width + "x" + b.height + "+" + b.x + "+" + b.y;

        return cls.substring(cls.lastIndexOf('.') + 1) + getId() + " [" + bounds + ",object=" +
        getObject() + "]";
    }

    public ObjectView topView() {
        View parent = getParent();

        if (parent == null) {
            return this;
        } else {
            return ((ObjectView) parent).topView();
        }
    }

    public void viewMenuOptions(MenuOptionSet options) {
        NakedObject obj = (NakedObject) getObject();

        options.add(MenuOptionSet.DEBUG, INVALIDATE_OPTION);
        options.add(MenuOptionSet.DEBUG, LAYOUT_OPTION);

        if (isReplaceable()) {
            // offer other/alternative views
            if (isRoot()) {
                replaceOptions(ViewFactory.getViewFactory().rootViews(obj), options);
            } else {
                replaceOptions(ViewFactory.getViewFactory().closedViews(obj), options);
                replaceOptions(ViewFactory.getViewFactory().internalViews(obj), options);
            }
        }

        if (isRoot()) {
            options.add(MenuOptionSet.WINDOW, CLOSE_OPTION);
        }

        Enumeration possibleViews = ViewFactory.getViewFactory().rootViews(obj);

        while (possibleViews.hasMoreElements()) {
            ObjectView view = (ObjectView) possibleViews.nextElement();
            MenuOption viewAs = new OpenViewOption((RootView) view);
            options.add(MenuOptionSet.VIEW, viewAs);
        }
    }

    protected Color backgroundColor() {
        return getObject().isFinder() ? new Color(0x99eecc) : super.backgroundColor();
    }

    protected Object clone() throws CloneNotSupportedException {
        AbstractObjectView clone = (AbstractObjectView) super.clone();
        clone.state = (ObjectViewState) state.clone();

        return clone;
    }

    protected void init(NakedObject object) {}
    
    /**
     * Root views should be opaque
     */
    protected boolean transparentBackground() {
        return !isRoot();
    }

    private void replaceOptions(Enumeration possibleViews, MenuOptionSet options) {
        while (possibleViews.hasMoreElements()) {
            ObjectView view = (ObjectView) possibleViews.nextElement();

            if (view.getClass() != getClass()) {
                MenuOption viewAs = new ReplaceViewOption(view);
                options.add(MenuOptionSet.VIEW, viewAs);
            }
        }
    }

    /**
     * Replaces this view with the specified view.  Calling this method with a
     * new view will ensure the new view appears in the same location as the
     * current view, will remove the current view from the workspace list, and
     * will add the new view to it.
     * @param form
     */
    private void replaceView(InternalView newView) {
        newView.setLocation(getBounds().getLocation());

        ((CompositeView) getParent()).replaceView((InternalView) this, newView);
    }
}
