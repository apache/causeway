package org.nakedobjects.viewer.lightweight.view;

import org.nakedobjects.object.NakedClass;
import org.nakedobjects.object.NakedClassManager;
import org.nakedobjects.object.NakedError;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectManager;
import org.nakedobjects.object.NotPersistableException;
import org.nakedobjects.object.collection.InternalCollection;
import org.nakedobjects.object.collection.TypedCollection;
import org.nakedobjects.object.control.Permission;
import org.nakedobjects.object.reflect.Field;
import org.nakedobjects.object.reflect.OneToManyAssociation;
import org.nakedobjects.security.SecurityContext;
import org.nakedobjects.security.Session;
import org.nakedobjects.viewer.lightweight.ClassView;
import org.nakedobjects.viewer.lightweight.InternalView;
import org.nakedobjects.viewer.lightweight.ObjectDrag;
import org.nakedobjects.viewer.lightweight.ObjectView;
import org.nakedobjects.viewer.lightweight.RootView;
import org.nakedobjects.viewer.lightweight.Style;
import org.nakedobjects.viewer.lightweight.util.ViewFactory;


public class InternalList extends StandardList implements InternalView, ClassView {

    public InternalList() {
        setBorder(new OpenFieldBorder());
    }

    protected Permission canAdd(NakedObject source) {
        SecurityContext context = Session.getSession().getSecurityContext();
        InternalCollection collection = (InternalCollection) getObject();
        NakedObject parent = collection.forParent();
        return ((OneToManyAssociation) getFieldOf()).getAbout(context, parent, source, true).canUse();
    }

    public void dropObject(ObjectDrag drag) {
        NakedObject source = drag.getSourceObject();
        if (canAdd(source).isAllowed()) {

            InternalCollection target = ((InternalCollection) getObject());

            if (source instanceof NakedClass && target.getType() != NakedClass.class) {
                source = ((NakedClass) source).acquireInstance();

                try {
                    NakedObjectManager.getInstance().makePersistent(source);
                } catch (NotPersistableException e) {
                    source = new NakedError("Failed to create object", e);

                    RootView view = ViewFactory.getViewFactory().createRootView(source);
                    view.setLocation(drag.getRelativeLocation());
                    getWorkspace().addRootView(view);

                    return;
                }

                source.created();
            }

            if (canAdd(source).isAllowed()) {
                Field fieldOf = getFieldOf();
                ((OneToManyAssociation) fieldOf).setAssociation(((ObjectView) getParent()).getObject(), source);
                invalidateLayout();
                validateLayout();
            }
        }
    }

    public NakedClass forNakedClass() {
        String name = ((TypedCollection) getObject()).getType().getName();
        return NakedClassManager.getInstance().getNakedClass(name);
    }

    public String getName() {
        return "InternalList";
    }

    protected Style.Text getTitleTextStyle() {
        return Style.LABEL;
    }

    protected String title() {
        return name();
    }

    protected boolean isModifiableCollection() {
        return false;
        /*
         * OneToManyAssociation assoc = (OneToManyAssociation)getFieldOf(); InternalCollection
         * collection = (InternalCollection) getObject(); NakedObject parent =
         * collection.forParent(); return assoc.getAbout(parent).canUse().isAllowed();
         */}

}

/*
 * Naked Objects - a framework that exposes behaviourally complete business objects directly to the
 * user. Copyright (C) 2000 - 2003 Naked Objects Group Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 * 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address of Naked Objects
 * Group is Kingsway House, 123 Goldworth Road, Woking GU21 1NR, UK).
 */

