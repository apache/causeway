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
import org.nakedobjects.object.ObjectStoreException;
import org.nakedobjects.object.collection.InstanceCollection;
import org.nakedobjects.object.control.ActionAbout;
import org.nakedobjects.object.reflect.Action;
import org.nakedobjects.security.Session;
import org.nakedobjects.viewer.lightweight.AbstractObjectView;
import org.nakedobjects.viewer.lightweight.Bounds;
import org.nakedobjects.viewer.lightweight.Canvas;
import org.nakedobjects.viewer.lightweight.ClassView;
import org.nakedobjects.viewer.lightweight.Click;
import org.nakedobjects.viewer.lightweight.Color;
import org.nakedobjects.viewer.lightweight.DesktopView;
import org.nakedobjects.viewer.lightweight.DragSource;
import org.nakedobjects.viewer.lightweight.DragTarget;
import org.nakedobjects.viewer.lightweight.DragView;
import org.nakedobjects.viewer.lightweight.Icon;
import org.nakedobjects.viewer.lightweight.Location;
import org.nakedobjects.viewer.lightweight.MenuOptionSet;
import org.nakedobjects.viewer.lightweight.ObjectDrag;
import org.nakedobjects.viewer.lightweight.RootView;
import org.nakedobjects.viewer.lightweight.Size;
import org.nakedobjects.viewer.lightweight.Style;
import org.nakedobjects.viewer.lightweight.ViewDrag;
import org.nakedobjects.viewer.lightweight.options.ClassOption;
import org.nakedobjects.viewer.lightweight.util.ImageFactory;
import org.nakedobjects.viewer.lightweight.util.ViewFactory;


public class ClassIcon extends AbstractObjectView implements DragSource, DragView, DragTarget,
    DesktopView, ClassView {
    private static Style.Text style = Style.CLASS;
    protected static final int ICON_SIZE = style.getAscent() * 3;
    protected Icon icon;
    protected NakedClass cls;
//    private ClassIcon dragView;
    private int DRAG_AREA = 18;

    public ClassIcon() {
    }

    public String getName() {
        return "class icon";
    }

    public NakedObject getObject() {
        return cls;
    }

    /**
     * Class icons are alway icons.
     */
    public boolean isOpen() {
        return false;
    }

    /**
     * Class icons cannot be replaced by windows
     */
    public boolean isReplaceable() {
        return false;
    }

    public Size getRequiredSize() {
        String title = cls == null ? "" : cls.title().toString();
        int iconWidth = icon == null ? 0 : icon.getWidth();
        int iconHeight = icon == null ? 0 : icon.getHeight();
        int textWidth = style.stringWidth(title);
        int width = Math.max(iconWidth + DRAG_AREA, textWidth) + 2 * HPADDING;
        int height = iconHeight + style.getAscent() + 3 * VPADDING;

        return new Size(width, height);
    }

    public void dragObjectIn(ObjectDrag drag) {
        NakedObject source = drag.getSourceObject();
        Action target = ((NakedClass) getObject()).getClassAction(Action.USER, new NakedClass[] {source.getNakedClass()});
        
        if ((target != null) && target.getAbout(Session.getSession().getSecurityContext(), source, getObject()).canUse().isAllowed()) {
            getState().setCanDrop();
        } else {
            getState().setCantDrop();
        }

        redraw();
    }

    public void draw(Canvas canvas) {
        super.draw(canvas);

        Bounds r = getBounds();

        Color color;

        if (getState().canDrop()) {
            color = Style.VALID;
        } else if (getState().cantDrop()) {
            color = Style.INVALID;
        } else if (getState().isObjectIdentified()) {
            color = Style.IDENTIFIED;
        } else if (getState().isViewIdentified()) {
            color = Style.ACTIVE;
        } else {
            color = Style.IN_BACKGROUND;
        }

        String title = cls.title().toString();
        int iconWidth = icon.getWidth();
        int iconHeight = icon.getHeight();
        int xi = HPADDING;
        int yi = VPADDING;
        int xt = HPADDING;
        int yt = iconHeight + 2 * VPADDING + style.getAscent();

        canvas.drawIcon(icon, xi, yi);
        canvas.drawText(title, xt, yt, color, style);

        if (isIdentified()) {
            if (getState().canDrop()) {
                color = Style.VALID;
            } else if (getState().cantDrop()) {
                color = Style.INVALID;
            } else if (getState().isObjectIdentified()) {
                color = Style.ACTIVE;
            } else if (getState().isViewIdentified()) {
                color = Style.IDENTIFIED;
            } else {
                color = Style.APPLICATION_BACKGROUND;
            }

            canvas.drawRectangle(0, 0, r.getWidth() - 1, r.getHeight() - 1, color);

            int x = xi + iconWidth + HPADDING;
            int x2 = getSize().getWidth() - VPADDING;

            for (int y = yi; y < yi + 20; y += 2) {
                canvas.drawLine(x, y, x2, y, color);
            }
        }

        if (DEBUG) {
            canvas.drawRectangle(0, 0, r.getWidth() - 1, r.getHeight() - 1, Color.DEBUG1);
            canvas.drawRectangle(xi, yi, iconWidth - 1, iconHeight - 1, Color.DEBUG1);
        }
    }

    public void dropObject(ObjectDrag drag) {
        NakedObject source = drag.getSourceObject();
        NakedClass target = (NakedClass) getObject();
        Action action = target.getClassAction(Action.USER, new NakedClass[] {source.getNakedClass()});
        
        if ((action != null) && action.getAbout(org.nakedobjects.security.Session.getSession().getSecurityContext(), source).canUse().isAllowed()) {
            NakedObject result = action.execute(target, source);

            if (result != null) {
                RootView view = ViewFactory.getViewFactory().createRootView(result);
                Location at = drag.getViewLocation();
                at.translate(50, 20);
                view.setLocation(at);
                getWorkspace().addRootView(view);
            }
        }
    }

    public NakedClass forNakedClass() {
        return cls;
    }

    public boolean indicatesForView(Location mouseLocation) {
        int iconWidth = icon.getWidth();
        int iconHeight = icon.getHeight();
        int xi = HPADDING;
        int yi = VPADDING;
        Bounds i = new Bounds(xi, yi, iconWidth, iconHeight);

        String title = cls == null ? "" : cls.title().toString();
        int yt = yi + iconHeight;
        int wt = style.stringWidth(title);

        int wh = style.getAscent() + VPADDING;
        Bounds t = new Bounds(xi, yt, wt, wh);

        return !(i.contains(mouseLocation) || t.contains(mouseLocation));
    }
    
    protected void init(NakedObject object) {
    	cls =  (NakedClass) object;
		String name = cls.fullName();
		name = name.substring(name.lastIndexOf('.') + 1);
		icon = ImageFactory.getImageFactory().createIcon(name, ICON_SIZE, null);
    }
    
    public boolean objectLocatedAt(Location mouseLocation) {
        int iconWidth = icon.getWidth();
        int iconHeight = icon.getHeight();
        int xi = HPADDING;
        int yi = VPADDING;
        Bounds i = new Bounds(xi, yi, iconWidth, iconHeight);

        String title = cls == null ? "" : cls.title().toString();
        int yt = yi + iconHeight;
        int wt = style.stringWidth(title);

        int wh = style.getAscent() + VPADDING;
        Bounds t = new Bounds(xi, yt, wt, wh);

        return i.contains(mouseLocation) || t.contains(mouseLocation);
    }

    public void objectMenuOptions(MenuOptionSet options) {
    	ClassOption.menuOptions(forNakedClass(), options);
    }

    public DragView pickupObject(ObjectDrag drag) {
    	DragView dragView = (DragView) makeView(getObject(), null);
//		dragView.setLocation(drag.getViewLocation());
        return dragView;
    }

    public DragView pickupView(ViewDrag drag) {
        getWorkspace().showMoveCursor();

        DragView dragOutline;
        dragOutline = ViewFactory.getViewFactory().createDragOutline(getObject());
        dragOutline.setSize(getSize());
        
        dragOutline.setLocation(drag.getViewLocation());
        getWorkspace().setOverlayView(dragOutline);

        return dragOutline;
    }

    public void secondClick(Click click) {
        if (click.isForView()) {
            super.secondClick(click);
        } else {
        	try {
        		ActionAbout	about =new ActionAbout(Session.getSession().getSecurityContext(), getObject());
        		cls.aboutActionInstances(about);
        		if (about.canUse().isAllowed()) { //cls.getObjectStore().hasInstances(cls.acquireInstance())) {
        			InstanceCollection instances = cls.actionInstances();
        			RootView list = ViewFactory.getViewFactory().createRootView(instances);
        			Location loc = click.getAbsoluteLocation();
        			loc.translate(60, 30);
        			list.setLocation(loc);
        			getWorkspace().addRootView(list);
        		}
        	} catch(ObjectStoreException e) {
        		LOG.error(e);
        	}
        }
    }

    protected boolean transparentBackground() {
        return true;
    }
}
