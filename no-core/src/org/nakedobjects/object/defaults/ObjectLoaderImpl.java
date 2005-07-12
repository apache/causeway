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

import org.apache.log4j.Logger;


public class ObjectLoaderImpl implements NakedObjectLoader {
    private static final Logger LOG = Logger.getLogger(ObjectLoaderImpl.class);
    private ObjectFactory objectFactory;
    private PojoAdapterHash pojoAdapterMap;
    private IdentityAdapterMap identityAdapterMap;
    private ReflectorFactory reflectorFactory;

    private void addIdentityMapping(Oid oid, PojoAdapter adapter) {
        LOG.debug("adding identity " + oid + " for " + adapter);
        identityAdapterMap.put(oid, adapter);
    }

    public NakedObject createAdapterForTransient(final Object object) {
        LOG.debug("creating adapter (transient) for " + object );
        NakedObject adapter = createObjectAdapter(object);
        Assert.assertEquals(adapter, pojoAdapterMap.getPojo(object));
        ((PojoAdapter) adapter).changeState(ResolveState.TRANSIENT);
        return adapter;
    }

    public NakedValue createAdapterForValue(final Object value) {
        Assert.assertFalse("Can't create an adapter for a NOF adapter", value instanceof Naked);
        LOG.debug("creating adapter (value) for " + value);

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
        LOG.debug("creating adapter (collection) for " + collection);

        NakedCollection adapter;
        adapter = reflectorFactory.createCollectionAdapter(collection);

        Assert.assertNotNull(adapter);

        return adapter;
    }

    private NakedObject createObjectAdapter(final Object object) {
        Assert.assertNotNull(object);
        Assert.assertFalse("POJO Map already contains object", pojoAdapterMap.containsPojo(object));
        Assert.assertFalse("Can't create an adapter for a NOF adapter", object instanceof Naked);

        NakedObject nakedObject = new PojoAdapter(object);
        pojoAdapterMap.add(object, nakedObject);
        LOG.debug("created PojoAdapter@" + Integer.toHexString(nakedObject.hashCode()) + " for " + object);
        return nakedObject;
    }

    public NakedObject createTransientInstance(NakedObjectSpecification specification) {
        Assert.assertTrue("must be an object", specification.isObject());
        LOG.debug("creating transient instance of for " + specification);
        Object object = objectFactory.createObject(specification);
        NakedObject adapter = createAdapterForTransient(object);
        objectFactory.setUpAsNewLogicalObject(object);
        return adapter;
    }

    public NakedValue createValueInstance(NakedObjectSpecification specification) {
        Assert.assertTrue("must be a value", specification.isValue());
        LOG.debug("creating value instance of for " + specification);
        Object object = objectFactory.createValueObject(specification);
        return createAdapterForValue(object);
    }

    public NakedObject getAdapterFor(final Object object) {
        Assert.assertNotNull("object is null", this, object);
        LOG.debug("get adapter for " + object);
        NakedObject adapter = (NakedObject) pojoAdapterMap.getPojo(object);
        return adapter;
    }

    public NakedObject getAdapterFor(final Oid oid) {
        Assert.assertNotNull("OID should not be null", this, oid);
        LOG.debug("get adapter for " + oid);
        NakedObject adapter = (NakedObject) identityAdapterMap.get(oid);
        return adapter;
    }

    public NakedObject getAdapterForElseCreateAdapterForTransient(final Object object) {
        NakedObject adapter = getAdapterFor(object);
        if (adapter == null) {
            LOG.debug("No existing adapter found for " + object + "; creating a new transient one");
            adapter = NakedObjects.getObjectLoader().createAdapterForTransient(object);
        }
        Assert.assertNotNull("should have an adapter for ", object, adapter);
        return adapter;
    }

    public String getDebugData() {
        DebugString debug = new DebugString();
        debug.append(pojoAdapterMap);
        debug.appendln();

        debug.appendTitle("Loaded objects");
        Enumeration e = identityAdapterMap.keys();
        int count = 0;
        while (e.hasMoreElements()) {
            Oid oid = (Oid) e.nextElement();
            NakedObject object = (NakedObject) identityAdapterMap.get(oid);
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
        return identityAdapterMap.elements();
    }

    public void init() {
        Assert.assertNotNull("needs an identity-adapter map", identityAdapterMap);
        Assert.assertNotNull("needs a pojo-adapter map", pojoAdapterMap);
        Assert.assertNotNull("needs an object factory", objectFactory);
        reflectorFactory = NakedObjects.getReflectorFactory();
    }

    public boolean isIdentityKnown(Oid oid) {
        Assert.assertNotNull(oid);
        return identityAdapterMap.containsKey(oid);
    }
    
    public void serializing(NakedObject object) {
        ((PojoAdapter) object).changeState(ResolveState.SERIALIZING);
    }

    public void loaded(NakedObject object, ResolveState state) {
        LOG.debug("loaded " + object + " as " + state.name());
        PojoAdapter pojoAdapter = ((PojoAdapter) object);
        Assert.assertTrue("invalid state for changing to LOADED", pojoAdapter.getResolveState().isResolving());
        pojoAdapter.changeState(state);
    }

    public void loading(NakedObject object, ResolveState state) {
        LOG.debug("loading " + object + " as " + state.name());
        PojoAdapter pojoAdapter = ((PojoAdapter) object);
        Assert.assertTrue("invalid state for changing to " + state, pojoAdapter, pojoAdapter.getResolveState().isResolvable(state));
        pojoAdapter.changeState(state);
    }

    public void madePersistent(final NakedObject adapter, final Oid assignedOid) {
        Assert.assertTrue("Adapter should be in map", pojoAdapterMap.getPojo(adapter.getObject()) == adapter);
        Assert.assertNull("OID should not already map to a known adapter", identityAdapterMap.get(assignedOid));
        LOG.debug("made persistent " + adapter+ " as " + assignedOid);

        ((PojoAdapter) adapter).persistedAs(assignedOid);

        addIdentityMapping(assignedOid, (PojoAdapter) adapter);
    }

    public NakedObject recreateAdapterForPersistent(Oid oid, NakedObjectSpecification specification) {
        Assert.assertNotNull("must have an OID", oid);
        Assert.assertTrue("must be an object", specification.isObject());
        if (isIdentityKnown(oid)) {
            return getAdapterFor(oid);
        }

        LOG.debug("recreating object " + specification.getFullName() + "/" + oid);
        Object object = objectFactory.createObject(specification);

        Assert.assertNotNull(oid);
        Assert.assertFalse("Identity Map already contains object for OID " + oid, identityAdapterMap.containsKey(oid));

        PojoAdapter adapter = (PojoAdapter) createObjectAdapter(object);

        addIdentityMapping(oid, adapter);

        Assert.assertTrue(pojoAdapterMap.getPojo(object) == adapter);
        Assert.assertTrue(identityAdapterMap.get(oid) == adapter);

        adapter.recreatedAs(oid);
        return adapter;
    }

    public void reset() {
        identityAdapterMap.clear();
        pojoAdapterMap.reset();
    }

    /**
     * Expose as a .Net property.
     * 
     * @property
     */
    public void set_IdentityAdapterMap(IdentityAdapterMap identityAdapterMap) {
        this.identityAdapterMap = identityAdapterMap;
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
    public void set_ReflectorFactory(ReflectorFactory reflectorFactory) {
        this.reflectorFactory = reflectorFactory;
    }

    /**
     * Expose as a .Net property.
     * 
     * @property
     */
    public void set_PojoAdapterMap(PojoAdapterHash pojos) {
        this.pojoAdapterMap = pojos;
    }

    public void setIdentityAdapterMap(IdentityAdapterMap identityAdapterMap) {
        this.identityAdapterMap = identityAdapterMap;
    }

    public void setObjectFactory(ObjectFactory objectFactory) {
        this.objectFactory = objectFactory;
    }

    public void setPojoAdapterMap(PojoAdapterHash pojoAdpaterMap) {
        this.pojoAdapterMap = pojoAdpaterMap;
    }

    public void shutdown() {
        identityAdapterMap.clear();
        identityAdapterMap = null;
        pojoAdapterMap.shutdown();
        reflectorFactory = null;
    }

    public void unloaded(NakedObject object) {
        Assert.assertTrue("cannot unload object as it is not loaded", object, identityAdapterMap.contains(object));
        LOG.debug("removed loaded object " + object);
        identityAdapterMap.remove(object.getOid());
        pojoAdapterMap.remove(object);
    }
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business objects directly to the user. Copyright (C) 2000 -
 * 2005 Naked Objects Group Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address of Naked Objects Group is Kingsway House, 123
 * Goldworth Road, Woking GU21 1NR, UK).
 */