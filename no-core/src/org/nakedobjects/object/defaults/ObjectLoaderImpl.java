package org.nakedobjects.object.defaults;

import org.nakedobjects.NakedObjects;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedCollection;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectLoader;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.NakedValue;
import org.nakedobjects.object.ObjectFactory;
import org.nakedobjects.object.ReflectorFactory;
import org.nakedobjects.object.ResolveState;
import org.nakedobjects.object.persistence.Oid;
import org.nakedobjects.object.reflect.PojoAdapter;
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


public class ObjectLoaderImpl implements NakedObjectLoader {
    private static final Logger LOG = Logger.getLogger(ObjectLoaderImpl.class);
    // TODO follow same pattern as PojoAdapterHash - delegate to hash class
    protected Hashtable identityMap = new Hashtable();
    protected ObjectFactory objectFactory;
    private PojoAdapterHash pojoMap = new PojoAdapterHashImpl();
    private ReflectorFactory reflectorFactory;

    
    public NakedObject createAdapterForTransient(final Object object) {
        NakedObject adapter = createObjectAdapter(object);
        Assert.assertTrue(pojoMap.getPojo(object) == adapter);
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

    public NakedCollection createCollectionAdapter(final Object collection) {
        Assert.assertFalse("Can't create an adapter for a NOF adapter", collection instanceof Naked);

        NakedCollection adapter;
        adapter = reflectorFactory.createCollectionAdapter(collection);

        Assert.assertNotNull(adapter);

        return adapter;
    }

    public NakedObject createInstance(NakedObjectSpecification specification) {
        Object object = objectFactory.createNewLogicalObject(specification);
        return createAdapterForTransient(object);
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

    public NakedObject createTransientInstance(NakedObjectSpecification specification) {
        return createInstance(specification);
    }

    public NakedValue createValueInstance(NakedObjectSpecification specification) {
        Object object = objectFactory.createValueObject(specification);
        return createAdapterForValue(object);
    }
    
    public NakedObject getAdapterOrCreateTransientFor(final Object object) {
        NakedObject adapter = getAdapterFor(object);
        if (adapter == null) {
            LOG.debug("No existing adapter found for " + object + "; creating a new transient one");
            adapter = NakedObjects.getObjectLoader().createAdapterForTransient(object);
        }
        Assert.assertNotNull("should have an adapter for ", object, adapter);
        return adapter;
    }

    public NakedObject getAdapterFor(final Object object) {
        Assert.assertNotNull("object is null", this, object);
        NakedObject adapter = (NakedObject) pojoMap.getPojo(object);
        return adapter;
    }

    public NakedObject getAdapterFor(final Oid oid) {
        Assert.assertNotNull("OID should not be null", this, oid);
        NakedObject adapter = (NakedObject) identityMap.get(oid);
        return adapter;
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

    public Enumeration getIdentifiedObjects() {
        return identityMap.elements();
    }

    public NakedObject getObject(Oid oid, NakedObjectSpecification hint) {
        return null;
    }

    public boolean isIdentityKnown(Oid oid) {
        Assert.assertNotNull(oid);
        return identityMap.containsKey(oid);
    }

    public void loaded(NakedObject object, ResolveState state) {
        ((PojoAdapter) object).changeState(state);
    }

    public void loading(NakedObject object, ResolveState state) {
        ((PojoAdapter) object).changeState(state);
    }

    public void makePersistent(final NakedObject adapter, final Oid oid) {
        Assert.assertTrue("Adapter should be in map", pojoMap.getPojo(adapter.getObject()) == adapter);
        Assert.assertNull("OID should not already map to an adapter", identityMap.get(oid));

        ((PojoAdapter) adapter).persistedAs(oid);

        identityMap.put(oid, adapter);
    }

    /**
     * Recreates an adapter for a persistent business object that is being
     * loaded into the system. If an adapter already exists for the specified
     * OID then that adapter is returned. Otherwise a new instance of the
     * specified business object is created and an adapter is created for it.
     * The adapter will then be in the state UNRESOLVED.
     */
    public NakedObject recreateAdapter(Oid oid, NakedObjectSpecification specification) {
        if (isIdentityKnown(oid)) {
            return getAdapterFor(oid);
        }

        LOG.debug("recreating object " + specification.getFullName() + "/" + oid);
        Object object = objectFactory.recreateObject(specification);

        Assert.assertNotNull(oid);
        Assert.assertFalse("Identity Map already contains object for OID " + oid, identityMap.containsKey(oid));

        PojoAdapter adapter = (PojoAdapter) createObjectAdapter(object);

        LOG.debug("adding adapter for " + oid + " - " + adapter);
        identityMap.put(oid, adapter);

        Assert.assertTrue(pojoMap.getPojo(object) == adapter);
        Assert.assertTrue(identityMap.get(oid) == adapter);

        adapter.recreate(oid);
        return adapter;
    }

    public Naked recreateExistingInstance(NakedObjectSpecification specification) {
        return null;
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
    public void set_ObjectFactory(ObjectFactory objectFactory) {
        this.objectFactory = objectFactory;
    }

    /**
     * Expose as a .Net property.
     * 
     * @property
     */
    public void set_PojoAdapterHash(PojoAdapterHash pojos) {
        this.pojoMap = pojos;
    }

    public void setObjectFactory(ObjectFactory objectFactory) {
        this.objectFactory = objectFactory;
    }

    public void setPojoAdapterHash(PojoAdapterHash pojos) {
        this.pojoMap = pojos;
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

    public void init() {
        reflectorFactory = NakedObjects.getReflectorFactory();
    }

    public boolean needsLoading(NakedObject object) {
        return ((PojoAdapter) object).needsLoading();
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