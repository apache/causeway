package org.nakedobjects.viewer.skylark.basic;

import org.nakedobjects.object.InternalCollection;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedClass;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.reflect.NakedObjectField;
import org.nakedobjects.object.security.ClientSession;
import org.nakedobjects.utility.NotImplementedException;
import org.nakedobjects.viewer.skylark.CompositeViewSpecification;
import org.nakedobjects.viewer.skylark.Content;
import org.nakedobjects.viewer.skylark.Location;
import org.nakedobjects.viewer.skylark.ObjectContent;
import org.nakedobjects.viewer.skylark.Size;
import org.nakedobjects.viewer.skylark.Skylark;
import org.nakedobjects.viewer.skylark.View;
import org.nakedobjects.viewer.skylark.ViewAxis;
import org.nakedobjects.viewer.skylark.core.AbstractViewBuilder;
import org.nakedobjects.viewer.skylark.util.ContentFactory;
import org.nakedobjects.viewer.skylark.util.ViewFactory;

import java.util.Enumeration;

import org.apache.log4j.Logger;


public class WorkspaceBuilder extends AbstractViewBuilder {
    private static final Logger LOG = Logger.getLogger(WorkspaceBuilder.class);
    private static final int PADDING = 10;
    public static final Location UNPLACED = new Location(-1, -1);

    public void build(View view) {
        NakedObject object = ((ObjectContent) view.getContent()).getObject();

        if (object != null && view.getSubviews().length == 0) {
            NakedObjectField[] flds = object.getSpecification().getVisibleFields(object, ClientSession.getSession());
            ViewFactory viewFactory = Skylark.getInstance().getViewFactory();
            ContentFactory contentFactory = Skylark.getInstance().getContentFactory();
            
            for (int f = 0; f < flds.length; f++) {
                NakedObjectField field = flds[f];
                Naked attribute = object.getField(field);

                if (field.getName().equals("classes") && field.isCollection()) {
                    Enumeration elements = ((InternalCollection) attribute).elements();
                    while (elements.hasMoreElements()) {
                        NakedObject cls = (NakedObject) elements.nextElement();
                        Content content = contentFactory.createRootContent(cls);
                        View classIcon = viewFactory.createIcon(content);
                        //View classIcon = viewFactory.createWorkspaceIcon(cls);
                        classIcon.setLocation(WorkspaceBuilder.UNPLACED);
                        view.addView(classIcon);
                    }

                } else if (field.getName().equals("objects") && field.isCollection()) {
                    Enumeration elements = ((InternalCollection) attribute).elements();
                    while (elements.hasMoreElements()) {
                        NakedObject obj = (NakedObject) elements.nextElement();
                        Content content = contentFactory.createRootContent(obj);
                        View objectIcon = viewFactory.createIcon(content);
                        //View objectIcon = viewFactory.createWorkspaceIcon(obj);
                        view.addView(objectIcon);
                    }
                }
            }
        }
    }

    public boolean canDisplay(Naked object) {
        return object instanceof NakedObject && object != null;
    }

    public Size getRequiredSize(View view) {
        return new Size(500, 500);
    }

    public String getName() {
        return "Simple Workspace";
    }

    public void layout(View view) {
        View views[] = view.getSubviews();
        for (int i = 0; i < views.length; i++) {
            View subview = views[i];
            subview.layout();
        }

        Size size = view.getSize();
        size.contract(view.getPadding());

        LOG.debug("Laying out workspace within " + size);

        int maxHeight = size.getHeight();

        int xClass = PADDING;
        int yClass = PADDING;
        int maxClassWidth = 0;

        int xObject = size.getWidth() - PADDING;
        int yObject = PADDING;

        int xWindow = 150;
        int yWindow = PADDING;

        for (int i = 0; i < views.length; i++) {
            View v = views[i];
            Size componentSize = v.getRequiredSize();
            v.setSize(componentSize);
            if (v.getLocation().equals(UNPLACED)) {
                int height = componentSize.getHeight() + 6;
                if (v.getSpecification().isOpen()) {
                    v.setLocation(new Location(xWindow, yWindow));
                    yWindow += height;

                } else {
                    NakedObject object = ((ObjectContent) v.getContent()).getObject();
                    if (object.getObject() instanceof NakedClass) {
                        if (yClass + height > maxHeight) {
                            yClass = PADDING;
                            xClass += maxClassWidth + PADDING;
                            maxClassWidth = 0;
                            LOG.debug("Creating new column at " + xClass + ", " + yClass);
                        }
                        LOG.debug("Class icon at " + xClass + ", " + yClass);
                        v.setLocation(new Location(xClass, yClass));
                        maxClassWidth = Math.max(maxClassWidth, componentSize.getWidth());
                        yClass += height;

                    } else {
                        v.setLocation(new Location(xObject - componentSize.getWidth(), yObject));
                        yObject += height;

                    }
                }

            }
        }
    }

    public View createCompositeView(Content content, CompositeViewSpecification specification, ViewAxis axis) {
        throw new NotImplementedException();
    }

}

/*
 * Naked Objects - a framework that exposes behaviourally complete business
 * objects directly to the user. Copyright (C) 2000 - 2005 Naked Objects Group
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