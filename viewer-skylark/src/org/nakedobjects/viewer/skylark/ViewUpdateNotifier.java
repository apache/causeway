package org.nakedobjects.viewer.skylark;

import org.nakedobjects.object.DirtyObjectSet;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectRuntimeException;
import org.nakedobjects.utility.DebugInfo;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.log4j.Logger;


public class ViewUpdateNotifier extends DirtyObjectSet implements DebugInfo {
    private static final Logger LOG = Logger.getLogger(ViewUpdateNotifier.class);
    protected Hashtable views = new Hashtable();
//    private DirtyObjectSet viewChanges = new DirtyObjectSet();

    public ViewUpdateNotifier() {
//        NakedObjects.getObjectManager().addObjectChangedListener(viewChanges);
    }
    
    public void add(View view) {
        Content content = view.getContent();
        if (content instanceof ObjectContent) {
            Naked object = content.getNaked();

            if (object != null) {
                Vector viewsToNotify;

                if (views.containsKey(object)) {
                    viewsToNotify = (Vector) views.get(object);
                } else {
                    viewsToNotify = new Vector();
                    views.put(object, viewsToNotify);
                }

                if (viewsToNotify.contains(view)) {
                    throw new NakedObjectRuntimeException(view + " already being notified");
                }
                viewsToNotify.addElement(view);
                LOG.debug("Added " + view + " to observers for " + object);
            }
        }
    }
/*
    public void objectChanged(NakedObject object) {
        Vector viewsToNotify = (Vector) views.get(object);

        if (viewsToNotify == null || viewsToNotify.size() == 0) {
            LOG.debug("No views to update for " + object);
        } else {
            for (int i = 0; i < viewsToNotify.size(); i++) {
                View view = (View) viewsToNotify.elementAt(i);
                LOG.info("object change notifications to: " + view + " for " + object);
                view.update(object);
            }
        }
    }
*/
    public String getDebugData() {
        StringBuffer buf = new StringBuffer();
        Enumeration f = views.keys();

        while (f.hasMoreElements()) {
            Object object = f.nextElement();

            Vector viewsToNotify = (Vector) views.get(object);
            Enumeration e = viewsToNotify.elements();
            buf.append("Views for " + object + " \n");

            while (e.hasMoreElements()) {
                View view = (View) e.nextElement();
                buf.append("        " + view);
                buf.append("\n");
            }
            buf.append("\n");
        }
        return buf.toString();
    }

    public String getDebugTitle() {
        return "Views for object details (observers)";
    }

    public void remove(View view) {
        Content content = view.getContent();
        if (content instanceof ObjectContent) {
            Naked object = ((ObjectContent) content).getObject();

            if (object != null && views.containsKey(object)) {
                Vector viewsToNotify;
                viewsToNotify = (Vector) views.get(object);
                viewsToNotify.removeElement(view);
                LOG.debug("Removed " + view + " from observers for " + object);

                if (viewsToNotify.size() == 0) {
                    views.remove(object);
                    LOG.debug("Removed observer list for " + object);
                }
            } else {
                throw new NakedObjectRuntimeException("Tried to remove a non-existant view " + view + " from observers for "
                        + object);
            }
        }
    }

    public void shutdown() {
        views.clear();
        //viewChanges.shutdown();
    }

    public void invalidateViewsForChangedObjects() {
       Enumeration objects = dirtyObjects();
        while (objects.hasMoreElements()) {
            NakedObject object = (NakedObject) objects.nextElement();
            LOG.debug("Invalidate views for " + object);
            Object viewsVector = this.views.get(object);
            if(viewsVector == null) {
                continue;
            }
            Enumeration views = ((Vector) viewsVector).elements();
            while (views.hasMoreElements()) {
                View view = (View) views.nextElement();
                LOG.debug("   - " + view);
                view.invalidateContent();
            }
        }
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