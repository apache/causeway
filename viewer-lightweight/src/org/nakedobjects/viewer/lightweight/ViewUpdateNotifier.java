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

import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectManager;
import org.nakedobjects.object.NakedObjectRuntimeException;
import org.nakedobjects.object.UpdateNotifier;
import org.nakedobjects.utility.DebugInfo;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.log4j.Logger;


public class ViewUpdateNotifier implements UpdateNotifier, DebugInfo {
    private static final Logger LOG = Logger.getLogger(ViewUpdateNotifier.class);
    private Hashtable views = new Hashtable();

    public String getDebugData() {
        StringBuffer buf = new StringBuffer();
        Enumeration f = views.keys();

        while (f.hasMoreElements()) {
            Object oid = f.nextElement();

            Vector viewsToNotify = (Vector) views.get(oid);
            Enumeration e = viewsToNotify.elements();
            buf.append("Views for " + oid + " \n");

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

    public void add(ObjectView view) {
        NakedObject object = view.getObject();

        if (object != null) {
            Vector viewsToNotify;

            if (views.containsKey(object)) {
                viewsToNotify = (Vector) views.get(object);
            } else {
                viewsToNotify = new Vector();
                views.put(object, viewsToNotify);
            }

            viewsToNotify.addElement(view);
            LOG.debug("Added " + view + " to observers for " + object);
        }
    }

    // TODO change to broadcastObjectSet, broadcastObjectClear, and broadcastObjectChangeValue
    public void broadcastObjectChanged(NakedObject object, NakedObjectManager objectStore) {
        Vector viewsToNotify = (Vector) views.get(object);

        if (viewsToNotify == null  || viewsToNotify.size() == 0) {
            LOG.warn("No views to update for " + object);
        } else {
            for (int i = 0; i < viewsToNotify.size(); i++) {
                ObjectView view = (ObjectView) viewsToNotify.elementAt(i);
                LOG.info("object change notifications to: " + view.getName() + view.getId() +
                    " for " + object);
                view.objectUpdate(object);
            }
        }
    }

    public void remove(ObjectView view) {
        NakedObject object = view.getObject();

        if (views.containsKey(object)) {
            Vector viewsToNotify;
            viewsToNotify = (Vector) views.get(object);
            viewsToNotify.removeElement(view);
            LOG.debug("Removed " + view + " from observers for " + object);

            if (viewsToNotify.size() == 0) {
                views.remove(object);
                LOG.debug("Removed observer list for " + object);
            }
        } else {
            throw new NakedObjectRuntimeException("Tried to remove a non-existant view " + view +
                " from observers for " + object);
        }
    }

    public void shutdown() {
    }
}
