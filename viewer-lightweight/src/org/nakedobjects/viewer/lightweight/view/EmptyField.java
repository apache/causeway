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
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectManager;
import org.nakedobjects.object.control.Permission;
import org.nakedobjects.object.reflect.OneToOneAssociation;
import org.nakedobjects.security.Session;
import org.nakedobjects.viewer.lightweight.ClassView;
import org.nakedobjects.viewer.lightweight.DragSource;
import org.nakedobjects.viewer.lightweight.DragTarget;
import org.nakedobjects.viewer.lightweight.InternalView;
import org.nakedobjects.viewer.lightweight.Location;
import org.nakedobjects.viewer.lightweight.MenuOptionSet;
import org.nakedobjects.viewer.lightweight.ObjectDrag;
import org.nakedobjects.viewer.lightweight.ObjectIconView;
import org.nakedobjects.viewer.lightweight.ObjectView;
import org.nakedobjects.viewer.lightweight.RootView;
import org.nakedobjects.viewer.lightweight.Size;
import org.nakedobjects.viewer.lightweight.Style;
import org.nakedobjects.viewer.lightweight.View;
import org.nakedobjects.viewer.lightweight.options.ClassOption;
import org.nakedobjects.viewer.lightweight.util.ViewFactory;


public class EmptyField extends ObjectIconView implements InternalView,
    DragTarget, ClassView {
    private static Style.Text style = Style.NORMAL;

    private boolean canDrop(ObjectDrag drag) {
        NakedObject source = ((ObjectView) drag.getSource()).getObject();

        if (source instanceof NakedClass) {
            return true;
        } else {
            NakedObject target = ((ObjectView) getParent()).getObject();
            OneToOneAssociation field = (OneToOneAssociation) getFieldOf();

            Permission perm = field.getAbout(Session.getSession().getSecurityContext(), target, source).canUse();

            if (perm.getReason() != null) {
                getWorkspace().setStatus(perm.getReason());
            }

            //            about  = perm.isAllowed() &&
            //			field.getType().isAssignableFrom(source.getClass());
            return perm.isAllowed();
        }
    }

    protected String iconName() {
        OneToOneAssociation field = (OneToOneAssociation) getFieldOf();
        String clsName = field.getType().getName();
        return clsName.substring(clsName.lastIndexOf('.') + 1);
	}

    public void dragObjectIn(ObjectDrag drag) {
        if (canDrop(drag)) {
            getState().setCanDrop();
        } else {
            getState().setCantDrop();
        }

        redraw();
    }
/*public void draw(Canvas canvas) {
        super.draw(canvas);

        Color color;

        if (getState().canDrop()) {
            color = Style.VALID;
        } else if (getState().cantDrop()) {
            color = Style.INVALID;
        } else if (getState().isViewIdentified()) {
            color = Style.ACTIVE;
        } else {
            color = Style.IN_BACKGROUND;
        }

        int iconHeight = (style.getAscent() * 125) / 100;
        int iconWidth = (iconHeight * 80) / 100;
        int containerHeight = getSize().getHeight();
        int iconCentre = containerHeight / 2;

        int xt = iconWidth + (HPADDING * 2);
        int yt = getBaseline();

        int xi = getPadding().getLeft() + HPADDING;
        int yi = iconCentre - (iconHeight / 2);

        canvas.drawFullOval(xi, yi, iconWidth, iconHeight, color);
        canvas.drawText(name(), xt, yt, color, style);

        if (AbstractView.DEBUG) {
            Size size = getSize();
            canvas.drawRectangle(0, 0, size.getWidth() - 1,
                size.getHeight() - 1, Color.DEBUG3);
            canvas.drawLine(0, size.getHeight() / 2, size.getWidth() - 1,
                size.getHeight() / 2, Color.DEBUG3);
        }
    }
*/
    public void dropObject(ObjectDrag drag) {
        DragSource dragSource = drag.getSource();

        if (canDrop(drag)) {
            NakedObject source = ((ObjectView) dragSource).getObject();
            NakedObject target = ((ObjectView) getParent()).getObject();

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
            OneToOneAssociation field = (OneToOneAssociation) getFieldOf();

            if (field.getType().isAssignableFrom(source.getClass())) {
                field.setAssociation(target, source);
            } else {
                RootView view = ViewFactory.getViewFactory().createRootView(source);
                view.setLocation(drag.getRelativeLocation());
                getWorkspace().addRootView(view);
            }
        }
    }

    public NakedClass forNakedClass() {
        OneToOneAssociation field = (OneToOneAssociation) getFieldOf();

        String name = ((OneToOneAssociation) field).getType().getName();
        return NakedObjectManager.getInstance().getNakedClass(name);
    }

    /**
     * @see View#getBaseline()
     */
    public int getBaseline() {
        int containerHeight = getSize().getHeight();
        int iconCentre = containerHeight / 2;
        int yt = iconCentre + (style.getAscent() / 2);

        return yt;
    }

    public Size getRequiredSize() {
    	Size size = super.getRequiredSize();
    	size.extend(titleSize());
        return size;
    }

    public boolean indicatesForView(Location mouseLocation) {
        return true;
    }

    protected void init(NakedObject object) {
        if (object != null) {
            throw new IllegalArgumentException(
                "An EmptyField view must be created with a null object");
        }
    }

    /**
     * An empty field is an icon.
     */
    public boolean isOpen() {
        return false;
    }

    /**
     * An empty field should not be replaced by another view.
     */
    public boolean isReplaceable() {
        return false;
    }
    
    protected boolean shaded() {
		return true;
	}

    private String name() {
        OneToOneAssociation field = (OneToOneAssociation) getFieldOf();

        if (field == null) {
            return "";
        } else {
            return NakedObjectManager.getInstance().getNakedClass(field.getType().getName()).getSingularName();
        }
    }

    /**
     * Objects returned by menus are used to set this field before passing the
     * call on to the parent.
     */
    public void objectMenuReturn(NakedObject object, Location at) {
        NakedObject target = ((ObjectView) getParent()).getObject();
        OneToOneAssociation field = (OneToOneAssociation) getFieldOf();
        field.setAssociation(target, object);
        super.objectMenuReturn(object, at);
    }

    protected String  title() {
		return  name();
	}

    public void viewMenuOptions(MenuOptionSet options) {
        ClassOption.menuOptions(forNakedClass(), options);
    }
}
