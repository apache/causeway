package org.nakedobjects.distribution.client;

import org.nakedobjects.distribution.ObjectProxy;
import org.nakedobjects.distribution.Request;
import org.nakedobjects.object.LoadedObjects;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectRuntimeException;
import org.nakedobjects.object.ObjectNotFoundException;

import java.util.Enumeration;
import java.util.Vector;


public abstract class AbstractGetInstances extends Request {
	protected void setInstances(Vector instances) {
		Vector oids = new Vector(instances.size());
		Enumeration e = instances.elements();

		while (e.hasMoreElements()) {
			NakedObject element = (NakedObject) e.nextElement();

			oids.addElement(new ObjectProxy(element));
		}
		response = oids;
	}

	public NakedObject[] getElements(LoadedObjects loadedObjects) {
		sendRequest();
		Vector instances = new Vector(((Vector) response).size());
		Enumeration e = ((Vector) response).elements();

		while (e.hasMoreElements()) {
			ObjectProxy element = (ObjectProxy) e.nextElement();

			try {
				instances.addElement(element.recreateObject(loadedObjects));
			} catch (ObjectNotFoundException ex) {
				throw new NakedObjectRuntimeException(ex);
			}
		}
		return instances;
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