package org.nakedobjects.object.reflect;

import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectRuntimeException;
import org.nakedobjects.object.ReflectorFactory;
import org.nakedobjects.object.ResolveException;
import org.nakedobjects.object.persistence.Oid;
import org.nakedobjects.object.reflect.valueadapter.BooleanAdapter;
import org.nakedobjects.object.reflect.valueadapter.ByteAdapter;
import org.nakedobjects.object.reflect.valueadapter.DateAdapter;
import org.nakedobjects.object.reflect.valueadapter.DoubleAdapter;
import org.nakedobjects.object.reflect.valueadapter.FloatAdapter;
import org.nakedobjects.object.reflect.valueadapter.IntAdapter;
import org.nakedobjects.object.reflect.valueadapter.LongAdapter;
import org.nakedobjects.object.reflect.valueadapter.ShortAdapter;
import org.nakedobjects.object.reflect.valueadapter.StringAdapter;
import org.nakedobjects.utility.Assert;
import org.nakedobjects.utility.DebugString;

import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;

import org.apache.log4j.Logger;


public class PojoAdapterFactoryImpl implements PojoAdapterFactory {
    private static final Logger LOG = Logger.getLogger(PojoAdapterFactoryImpl.class);
    
    // TODO follow same pattern as PojoAdapterHash - delegate to hash class
    protected Hashtable loaded = new Hashtable();

    private PojoAdapterHash pojos;
    private ReflectorFactory reflectorFactory;

    public Naked createAdapter(final Object pojo) {
        if (pojo == null) {
            return null;
        }
        Naked nakedObject;
        if (pojos.containsPojo(pojo)) {
            nakedObject = pojos.getPojo(pojo);
        } else {
            if (pojo instanceof Naked) {
                throw new NakedObjectRuntimeException("Warning: adapter is wrapping an adapter: " + pojo);
            }

            if (pojo instanceof String) {
                nakedObject = new StringAdapter((String) pojo);
            } else if (pojo instanceof Date) {
                nakedObject = new DateAdapter((Date) pojo);
            } else if (pojo instanceof Float) {
                nakedObject = new FloatAdapter();
            } else if (pojo instanceof Double) {
                nakedObject = new DoubleAdapter();
            } else if (pojo instanceof Boolean) {
                nakedObject = new BooleanAdapter();
            } else if (pojo instanceof Byte) {
                nakedObject = new ByteAdapter();
            } else if (pojo instanceof Short) {
                nakedObject = new ShortAdapter();
            } else if (pojo instanceof Integer) {
                nakedObject = new IntAdapter();
            } else if (pojo instanceof Long) {
                nakedObject = new LongAdapter();
            } else {
                nakedObject = reflectorFactory.createAdapter(pojo);
            }
            if (nakedObject == null) {
                nakedObject = new PojoAdapter(pojo);
                pojos.add(pojo, nakedObject);
   //             NakedObjects.getObjectManager().resolveImmediately((NakedObject) nakedObject);
                LOG.debug("created PojoAdapter@" + Integer.toHexString(nakedObject.hashCode()) + " for " + pojo);                
            }
        }
        return nakedObject;
    }

    public NakedObject createNOAdapter(final Object pojo) {
        return (NakedObject) createAdapter(pojo);
    }

    public String getDebugData() {
        DebugString debug = new DebugString();
        Enumeration e = loaded.keys();
        while (e.hasMoreElements()) {
            Oid oid = (Oid) e.nextElement();
            NakedObject object = (NakedObject) loaded.get(oid);
            debug.append(oid.toString());
            debug.append("    ");
            debug.appendln(object.toString());
        }
        debug.appendln();
        debug.append(pojos.getDebugData());
        return debug.toString();
    }

    public String getDebugTitle() {
        return "Loaded objects and POJOs";
    }

    public NakedObject getLoadedObject(Oid oid) {
        if (oid == null) {
            throw new IllegalArgumentException("OID is null");
        }
        return (NakedObject) loaded.get(oid);
    }

    public Enumeration getLoadedObjects() {
        return loaded.elements();
    }

    public boolean isLoaded(Oid oid) {
        if (oid == null) {
            throw new IllegalArgumentException("OID is null");
        }
        return loaded.containsKey(oid);
    }

    public void loaded(NakedObject object) throws ResolveException {
        Oid oid = object.getOid();
        if (oid == null) {
            throw new IllegalArgumentException("OID is null");
        }
        if (isLoaded(oid)) {
            throw new NakedObjectRuntimeException("cannot add as loaded object; oid " + oid + " already present: "
                    + getLoadedObject(oid));
        }
        if (loaded.contains(object)) {
            throw new NakedObjectRuntimeException(
                    "cannot add as loaded object; object already present, but with a different oid: " + object);
        }
    
        LOG.debug("loaded for OID " + oid + " - " + object);
        loaded.put(oid, object);
    }

    public void reset() {
        loaded = new Hashtable();
        pojos.reset();
    }

    /**
     * Expose as a .Net property.
     * 
     * @property
     */
    public void set_PojoAdapterHash(PojoAdapterHash pojos) {
        this.pojos = pojos;
    }

    /**
     * Expose as a .Net property.
     * 
     * @property
     */
    public void set_ReflectorFactory(ReflectorFactory reflectorFactory) {
        this.reflectorFactory = reflectorFactory;
    }

    public void setPojoAdapterHash(PojoAdapterHash pojos) {
        this.pojos = pojos;
    }

    public void setReflectorFactory(ReflectorFactory reflectorFactory) {
        this.reflectorFactory = reflectorFactory;
    }

    public void shutdown() {
        loaded.clear();
        loaded = null;
        pojos.shutdown();
        reflectorFactory = null;
    }

    public void unloaded(NakedObject object) {
        Assert.assertTrue("cannot unload object as it is not loaded", object, loaded.contains(object));
        LOG.debug("removed loaded object " + object);
        loaded.remove(object.getOid());
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