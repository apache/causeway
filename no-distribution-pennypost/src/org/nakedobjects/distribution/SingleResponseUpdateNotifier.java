package org.nakedobjects.distribution;

import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectManager;
import org.nakedobjects.object.UpdateNotifier;

import java.util.Vector;


public class SingleResponseUpdateNotifier implements UpdateNotifier {
    private Vector updates = new Vector();

    public void broadcastObjectChanged(NakedObject object, NakedObjectManager objectManager) {
        System.out.println("UPDATE " + object);
        if (!updates.contains(object)) {
            updates.addElement(object);
        }
    }

    public void shutdown() {}

    public ObjectUpdateMessage[] getUpdates() {
        int max = updates.size();
        ObjectUpdateMessage[] updateMessages = new ObjectUpdateMessage[max];
        for (int i = 0; i < max; i++) {
            NakedObject update = (NakedObject) updates.elementAt(i);
            updateMessages[i] = new ObjectUpdateMessage(update);
        }
        return updateMessages;
    }

    public void clear() {
        updates.removeAllElements();
    }

}

/*
 * Naked Objects - a framework that exposes behaviourally complete business
 * objects directly to the user. Copyright (C) 2000 - 2004 Naked Objects Group
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