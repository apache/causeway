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
package org.nakedobjects.viewer.lightweight.view;

import org.nakedobjects.object.NakedClass;
import org.nakedobjects.object.NakedCollection;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectManager;
import org.nakedobjects.object.collection.InternalCollection;
import org.nakedobjects.object.control.Permission;
import org.nakedobjects.object.reflect.OneToManyAssociation;
import org.nakedobjects.viewer.lightweight.Canvas;
import org.nakedobjects.viewer.lightweight.Color;
import org.nakedobjects.viewer.lightweight.DragSource;
import org.nakedobjects.viewer.lightweight.DragTarget;
import org.nakedobjects.viewer.lightweight.InternalView;
import org.nakedobjects.viewer.lightweight.Location;
import org.nakedobjects.viewer.lightweight.MenuOptionSet;
import org.nakedobjects.viewer.lightweight.ObjectDrag;
import org.nakedobjects.viewer.lightweight.ObjectView;
import org.nakedobjects.viewer.lightweight.Padding;
import org.nakedobjects.viewer.lightweight.Size;
import org.nakedobjects.viewer.lightweight.Style;
import org.nakedobjects.viewer.lightweight.Style.Text;
import org.nakedobjects.viewer.lightweight.options.ClassOption;
import org.nakedobjects.viewer.lightweight.util.ViewFactory;


public abstract class StandardList extends CollectionView implements DragSource, DragTarget {
    public int getBaseline() {
        if (getComponents().length == 0) {
            int containerHeight = getSize().getHeight();
            int iconCentre = containerHeight - getPadding().getBottom() / 2;
            int yt = iconCentre + (Style.NORMAL.getAscent() / 2);

            return yt;
        } else {
            return super.getBaseline();
        }
    }

    public Padding getPadding() {
        Padding in = super.getPadding();

        if (isModifiableCollection()) {
            in.extendBottom(titleSize().getHeight());
        }

        return in;
    }

    public String getName() {
        return "List";
    }

    public Size getRequiredSize() {
        Size size = super.getRequiredSize();
        int minWidth = Style.NORMAL.getHeight() + HPADDING +
            Style.NORMAL.stringWidth(name()) + (HPADDING * 2);

        size.ensureWidth(minWidth);

        if (isModifiableCollection()) {
            int iconHeight = (Style.NORMAL.getAscent() * 125) / 100;
            int iconWidth = (iconHeight * 80) / 100;
            int width = HPADDING + iconWidth + HPADDING + Style.NORMAL.stringWidth(name()) + HPADDING;

            size.ensureWidth(width);
        }

        return size;
    }

    public void dragObjectIn(ObjectDrag drag) {
    	NakedObject source = drag.getSourceObject();
    	Permission add = canAdd(source);
    	if (add.isAllowed()) {
    		NakedCollection target = ((NakedCollection) getObject());

            if (source instanceof NakedClass) {
                getState().setCanDrop();

                return;
            } else if ((target != null) && target.canAdd(source).isAllowed()) {
                getState().setCanDrop();
            } else {
                getState().setCantDrop();
            }
        } else {
            getState().setCantDrop();
            getWorkspace().setStatus(add.getReason());
//            getWorkspace().setStatus("Collection cannot be changed");
        }

        redraw();
    }

    public void dropObject(ObjectDrag drag) {
    	NakedObject source = drag.getSourceObject();
    	if(canAdd(source).isAllowed()) {
	    	
	        NakedCollection target = ((NakedCollection) getObject());
	/*
	        if (source instanceof NakedClass) {
	            source = ((NakedClass) source).acquireInstance();
	
	            try {
	                source.makePersistent();
	            } catch (ObjectStoreException e) {
	                source = new NakedError("Failed to create object", e);
	
	                RootView view = ViewFactory.getViewFactory().createRootView(source);
	                view.setLocation(drag.getRelativeLocation());
	                getWorkspace().addRootView(view);
	
	                return;
	            }
	
	            source.created();
	        }
	*/
	        if ((target != null) && target.canAdd(source).isAllowed()) {
	        	target.add(drag.getSourceObject());
	            invalidateLayout();
	            validateLayout();
	        }
    	}
    }

    protected Permission canAdd(NakedObject source) {
		NakedCollection collection = (NakedCollection) getObject();
		return collection.canAdd(source);
	}

	public NakedClass forNakedClass() {
        return null;
    }

    protected void init(NakedObject object) {
		((NakedCollection) object).first();
		refresh();
		invalidateLayout();
	}

    public void draw(Canvas canvas) {
        super.draw(canvas);

        Color color;
        if (isModifiableCollection()) {
            if (getState().canDrop()) {
                color = Style.VALID;
            } else if (getState().cantDrop()) {
                color = Style.INVALID;
            } else if (getState().isViewIdentified()) {
                color = Style.ACTIVE;
            } else {
                color = Style.IN_BACKGROUND;
            }

            int iconHeight = (Style.NORMAL.getAscent() * 125) / 100;
            int iconWidth = (iconHeight * 80) / 100;
            int containerHeight = getSize().getHeight();
            int iconCentre = containerHeight - getPadding().getBottom() / 2;
            int xi = getPadding().getLeft() + HPADDING;
            int yi = iconCentre - iconHeight / 2;
            int xt = xi + iconWidth + HPADDING;
            int yt = iconCentre + (Style.NORMAL.getAscent() / 2);

            canvas.drawFullOval(xi, yi, iconWidth, iconHeight, color);
            canvas.drawText(name(), xt, yt, color, Style.NORMAL);

            if (DEBUG) {
                canvas.drawRectangle(xi, yi, iconWidth - 1, iconHeight - 1, Color.DEBUG3);
                canvas.drawLine(0, containerHeight, getSize().getWidth(), containerHeight, Color.DEBUG3);

                int last = containerHeight - getPadding().getBottom();
                canvas.drawLine(0, last, getSize().getWidth(), last, Color.DEBUG3);
                canvas.drawLine(5, iconCentre, getSize().getWidth() - 10, iconCentre, Color.DEBUG3);
            }
        }

        if (DEBUG) {
            canvas.drawRectangle(getSize(), Color.DEBUG3);
        }
    }

    public void viewMenuOptions(MenuOptionSet options) {
    	ClassOption.menuOptions(forNakedClass(), options);
    }
    
    /**
     * Objects returned by menus are added to this list before being passed on the 
     * parent.
     */
    public void objectMenuReturn(NakedObject object, Location at) {
    	NakedObject target = ((ObjectView) getParent()).getObject();
    	((OneToManyAssociation) getFieldOf()).setAssociation(target, object);
    	invalidateLayout();
    	validateLayout();
    	super.objectMenuReturn(object, at);
    }

    
    protected Text getTitleTextStyle() {
        return Style.TITLE;
    }

    protected InternalView createListElement(NakedObject obj) {
        return ViewFactory.getViewFactory().createInternalView(obj, getFieldOf(), true);
    }
/*
    protected boolean includeIcon() {
        return isRoot();
    }

	protected boolean includeTitle() {
		return isRoot();
	}
*/
     protected boolean isModifiableCollection() {
     	return false;
    }

    private boolean forInternalCollection() {
        return getObject() instanceof InternalCollection;
    }

    protected String name() {
        if (forInternalCollection()) {
            Class c = ((InternalCollection) getObject()).getType();

            return NakedObjectManager.getInstance().getNakedClass(c.getName()).getPluralName();
        } else {
            return "";
        }
    }
}
