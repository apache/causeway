package org.nakedobjects.object.reflect;

import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedCollection;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedValue;
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
import org.nakedobjects.utility.UnexpectedCallException;

import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;

import org.apache.log4j.Logger;


public class PojoAdapterFactoryImpl implements PojoAdapterFactory {
    private static final Logger LOG = Logger.getLogger(PojoAdapterFactoryImpl.class);
    // TODO follow same pattern as PojoAdapterHash - delegate to hash class
    protected Hashtable identityMap = new Hashtable();
    private PojoAdapterHash pojoMap = new PojoAdapterHashImpl();
    private ReflectorFactory reflectorFactory;
    
    
    public NakedObject createAdapterForTransient(final Object object) {
        NakedObject adapter = createObjectAdapter(object);
       
        Assert.assertTrue(pojoMap.getPojo(object) == adapter);
        
        return adapter;
    }
    
    public NakedObject createAdapterForPersistent(final Object object, final Oid oid) {
        Assert.assertNotNull(oid);
        Assert.assertFalse("Identity Map already contains object for OID " + oid, identityMap.containsKey(oid));
        
        NakedObject adapter = createObjectAdapter(object);
    
        LOG.debug("adding adapter for " + oid + " - " + adapter);
        identityMap.put(oid, adapter);

        Assert.assertTrue(pojoMap.getPojo(object) == adapter);
        Assert.assertTrue(identityMap.get(oid) == adapter);
 
        return adapter;
    }
    
    public NakedCollection createCollectionAdapter(final Object collection) {
        Assert.assertFalse("Can't create an adapter for a NOF adapter", collection instanceof Naked);
        
        NakedCollection adapter;
        adapter = reflectorFactory.createCollectionAdapter(collection);
        
        Assert.assertNotNull(adapter);
    
        return adapter;
    }
    
    public NakedValue createAdapterForValue(final Object value) {
        Assert.assertFalse("Can't create an adapter for a NOF adapter", value instanceof Naked);
        
        NakedValue adapter;
        if (value instanceof String) {
            adapter = new StringAdapter((String) value);
        } else if (value instanceof Date) {
            adapter = new DateAdapter((Date) value);
        } else if (value instanceof Float) {
            adapter = new FloatAdapter();
        } else if (value instanceof Double) {
            adapter = new DoubleAdapter();
        } else if (value instanceof Boolean) {
            adapter = new BooleanAdapter();
        } else if (value instanceof Byte) {
            adapter = new ByteAdapter();
        } else if (value instanceof Short) {
            adapter = new ShortAdapter();
        } else if (value instanceof Integer) {
            adapter = new IntAdapter();
        } else if (value instanceof Long) {
            adapter = new LongAdapter();
        } else {
            adapter = reflectorFactory.createValueAdapter(value);
        }
        
   //     Assert.assertNotNull(value.toString(), adapter);
    
        return adapter;
    }
    
    public void makePersistent(final NakedObject adapter, final Oid oid) {
        Assert.assertTrue("Adapter should be in map", pojoMap.getPojo(adapter.getObject()) == adapter);
        Assert.assertNull("OID should not map to an adapter", identityMap.get(oid));

        ((PojoAdapter) adapter).persistedAs(oid);

        identityMap.put(oid, adapter);
    }
    
    public NakedObject getAdapterFor(final Oid oid) {
        Assert.assertNotNull("OID should not be null", this, oid);
        NakedObject adapter = (NakedObject) identityMap.get(oid);
        return adapter;
    }
 
    public NakedObject getAdapterFor(final Object object) {
        Assert.assertNotNull("object is null", this, object);
        NakedObject adapter = (NakedObject) pojoMap.getPojo(object);
        if(adapter == null) {
            LOG.debug("No existing adapter found for " + object + "; creating a new transient one");
            adapter = createAdapterForTransient(object);
        }
        Assert.assertNotNull("should find an adapter for ", object, adapter);
        return adapter;
    }
 
    
    
    
    public Naked createAdapter(final Object pojo) {
        throw new UnexpectedCallException();
        /*
        if (pojo == null) {
            return null;
        }
        Naked nakedObject;
        if (pojoMap.containsPojo(pojo)) {
            nakedObject = pojoMap.getPojo(pojo);
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
                pojoMap.add(pojo, nakedObject);
   //             NakedObjects.getObjectManager().resolveImmediately((NakedObject) nakedObject);
                LOG.debug("created PojoAdapter@" + Integer.toHexString(nakedObject.hashCode()) + " for " + pojo);                
            }
        }
        return nakedObject;
        */
    }

    private NakedObject createObjectAdapter(final Object object) {
        Assert.assertNotNull(object);
        Assert.assertFalse("POJO Map already contains object", pojoMap.containsPojo(object));
        Assert.assertFalse("Can't create an adapter for a NOF adapter", object instanceof Naked);

        NakedObject nakedObject = new PojoAdapter(object);
        pojoMap.add(object, nakedObject);
        LOG.debug("created PojoAdapter@" + Integer.toHexString(nakedObject.hashCode()) + " for " + object);                
        return nakedObject;
    }

    public NakedObject createNOAdapter(final Object pojo) {
        return (NakedObject) createAdapter(pojo);
    }

    public String getDebugData() {
        DebugString debug = new DebugString();
        debug.append(pojoMap);
        debug.appendln();

        debug.appendTitle("Loaded objects");
        Enumeration e = identityMap.keys();
        int count = 0;
        while (e.hasMoreElements()) {
            Oid oid = (Oid) e.nextElement();
            NakedObject object = (NakedObject) identityMap.get(oid);
            debug.append(count++, 5);
            debug.append(" ");
            debug.append(oid.toString(), 8);
            debug.append("    ");
            debug.appendln(object.toString());
        }
        debug.appendln();
        return debug.toString();
    }

    public String getDebugTitle() {
        return "Loaded objects and POJOs";
    }

     public NakedObject getLoadedObject(Oid oid) {
        throw new UnexpectedCallException();
    }

    protected Enumeration getIdentifiedObjects() {
        return identityMap.elements();
    }

    public boolean isIdentityKnown(Oid oid) {
        Assert.assertNotNull(oid);
        return identityMap.containsKey(oid);
    }
    
    public boolean isLoaded(Oid oid) {
        throw new UnexpectedCallException();
/*        if (oid == null) {
            throw new IllegalArgumentException("OID is null");
        }
        return identityMap.containsKey(oid);
        */
    }

    public void loaded(NakedObject object) throws ResolveException {
        throw new UnexpectedCallException();
        /*
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
        */
    }

    public void reset() {
        identityMap = new Hashtable();
        pojoMap.reset();
    }

    /**
     * Expose as a .Net property.
     * 
     * @property
     */
    public void set_PojoAdapterHash(PojoAdapterHash pojos) {
        this.pojoMap = pojos;
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
        this.pojoMap = pojos;
    }

    public void setReflectorFactory(ReflectorFactory reflectorFactory) {
        this.reflectorFactory = reflectorFactory;
    }

    public void shutdown() {
        identityMap.clear();
        identityMap = null;
        pojoMap.shutdown();
        reflectorFactory = null;
    }

    public void unloaded(NakedObject object) {
        Assert.assertTrue("cannot unload object as it is not loaded", object, identityMap.contains(object));
        LOG.debug("removed loaded object " + object);
        identityMap.remove(object.getOid());
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