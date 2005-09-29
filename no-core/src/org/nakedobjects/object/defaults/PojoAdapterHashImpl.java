package org.nakedobjects.object.defaults;

import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedReference;
import org.nakedobjects.utility.DebugString;

import java.util.Enumeration;
import java.util.Hashtable;

import org.apache.log4j.Logger;


public class PojoAdapterHashImpl implements PojoAdapterHash {
    private static final Logger LOG = Logger.getLogger(PojoAdapterHashImpl.class);
    protected Hashtable pojos = new Hashtable();

    public void add(Object pojo, Naked adapter) {
        LOG.debug("add " + pojo + " as " + adapter);
        pojos.put(pojo, adapter);
    }

    public boolean containsPojo(Object pojo) {
        return pojos.containsKey(pojo);
    }

    protected void finalize() throws Throwable {
        super.finalize();
        LOG.info("finalizing hash of pojos");
    }

    public String getDebugData() {
        DebugString debug = new DebugString();
        Enumeration e = pojos.keys();
        int count = 0;
        while (e.hasMoreElements()) {
            Object pojo = (Object) e.nextElement();
            NakedReference object = (NakedReference) pojos.get(pojo);
            debug.append(count++, 5);
            debug.append(" '");
            debug.append(pojo.toString(), 25);
            debug.append("'    ");
            debug.appendln(object.toString());
        }
        return debug.toString();
    }

    public String getDebugTitle() {
        return "POJO Adapter Hashtable";
    }

    public Naked getPojo(Object pojo) {
        return (Naked) pojos.get(pojo);
    }

    public void reset() {
        LOG.debug("reset");
        pojos.clear();
    }

    public void shutdown() {
        LOG.debug("shutdown");
        pojos.clear();
    }

    public void remove(NakedObject object) {
        LOG.debug("remove " + object);
        pojos.remove(object.getObject());
    }
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business objects directly to the user.
 * Copyright (C) 2000 - 2005 Naked Objects Group Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation; either version 2 of the License, or (at your
 * option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to
 * the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address of Naked Objects Group is
 * Kingsway House, 123 Goldworth Road, Woking GU21 1NR, UK).
 */