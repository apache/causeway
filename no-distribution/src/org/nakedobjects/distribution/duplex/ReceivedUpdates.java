package org.nakedobjects.distribution.duplex;

import org.nakedobjects.distribution.ObjectUpdateMessage;

import java.util.Vector;


final class ReceivedUpdates {
    private Vector updates = new Vector();
	private boolean running = true;

	synchronized ObjectUpdateMessage getNextUpdate() {
        while (updates.size() == 0  && running) {
            try {
                wait();
            } catch (InterruptedException ignore) {
            }
        }

        if(running) {
        	ObjectUpdateMessage update = (ObjectUpdateMessage) updates.elementAt(0);
        	updates.removeElementAt(0);
        	return update;
        } else {
        	return null;
        }
	}

	   synchronized void addUpdate(ObjectUpdateMessage update) {
        updates.addElement(update);
        notify();
    }

	public synchronized void shutdown() {
		running = false;
		notify();
	}
}

/*
Naked Objects - a framework that exposes behaviourally complete
business objects directly to the user.
Copyright (C) 2000 - 2005  Naked Objects Group Ltd

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
