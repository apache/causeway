package org.nakedobjects.object;

import org.nakedobjects.utility.DebugInfo;

import java.util.Enumeration;
import java.util.Hashtable;

import org.apache.log4j.Logger;


public abstract class NakedClassManager implements DebugInfo {
    private static NakedClassManager instance;
    private static final Logger LOG = Logger.getLogger(NakedObjectManager.class);

    public static NakedClassManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("No existing class manager");
        }

        return instance;
    }

    private final Hashtable classes = new Hashtable();
    private NakedClassLoader classLoader;

    protected NakedClassManager() {
        if (instance != null) {
            throw new NakedObjectRuntimeException("NakedClassManager already created: " + instance);
        }
        instance = this;

        classLoader = installClassLoader();
        classes.put("org.nakedobjects.object.NakedClass", NakedClass.SELF);
//        NakedClass.SELF.setOid(NakedObjectManager.getInstance().createOid());
    }

    protected NakedClassLoader installClassLoader() {
        return new NakedClassLoader();
    }

    protected abstract boolean accessRemotely();

    protected abstract void createClass(NakedClass nc) throws ObjectStoreException;

    public String getDebugData() {
        StringBuffer data = new StringBuffer();
        data.append("Naked classes\n\n");

        Enumeration e = classes.keys();

        while (e.hasMoreElements()) {
            String name = (String) e.nextElement();
            data.append(name + "; " + classes.get(name) + "\n");
        }

        while (e.hasMoreElements()) {
            data.append(e.nextElement());
            data.append('\n');
        }

        return data.toString();
    }

    public String getDebugTitle() {
        return "Naked Class Manager";
    }

    public NakedClass getNakedClass(String name) {
        if (name == null) {
            throw new NullPointerException();
        }
        if (name.length() == 0) {
            throw new IllegalArgumentException("Class name is empty");
        }

        if (classes.containsKey(name)) {
            Object cls = classes.get(name);
            if (cls instanceof String) {
                throw new NakedObjectRuntimeException("Unwanted recursion - already creating NakedClass for " + name);
            }
            return (NakedClass) cls;

        } else {
            classes.put(name, name); // used to
            // ensure no
            // looping
            // occurs

            try {
                try {
                    NakedClass nc = loadClass(name);
                    LOG.info("loaded class " + name + " from object store");
                    classes.put(name, nc);
                    return nc;
                } catch (ObjectNotFoundException e) {
                    LOG.info("create class " + name + ", and persist in object store");
                    NakedClass nc = new NakedClass();
                    nc.getName().setValue(name);
                    nc.getReflector().setValue(classLoader.findType(name));
                    LOG.debug(name + "/" + nc.getReflector().stringValue());
                    createClass(nc);
                    classes.put(name, nc);
                    return nc;
                }
            } catch (ObjectStoreException e) {
                throw new NakedObjectRuntimeException(e);
            }
        }
    }

    protected abstract NakedClass loadClass(String name) throws ObjectStoreException, ObjectNotFoundException;

    public void reflect(NakedClass cls) {
        classLoader.reflect(cls, accessRemotely());
    }

    public void shutdown() {
        instance = null;
    }

    public void init() {}
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