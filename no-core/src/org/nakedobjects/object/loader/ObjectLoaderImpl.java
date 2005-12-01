package org.nakedobjects.object.loader;

import org.nakedobjects.object.AdapterFactory;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedCollection;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectLoader;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.NakedReference;
import org.nakedobjects.object.NakedValue;
import org.nakedobjects.object.ObjectFactory;
import org.nakedobjects.object.Oid;
import org.nakedobjects.object.ResolveState;
import org.nakedobjects.object.defaults.PojoAdapter;
import org.nakedobjects.object.value.adapter.BooleanAdapter;
import org.nakedobjects.object.value.adapter.ByteAdapter;
import org.nakedobjects.object.value.adapter.CharAdapter;
import org.nakedobjects.object.value.adapter.DateAdapter;
import org.nakedobjects.object.value.adapter.DoubleAdapter;
import org.nakedobjects.object.value.adapter.FloatAdapter;
import org.nakedobjects.object.value.adapter.IntAdapter;
import org.nakedobjects.object.value.adapter.LongAdapter;
import org.nakedobjects.object.value.adapter.ShortAdapter;
import org.nakedobjects.object.value.adapter.StringAdapter;
import org.nakedobjects.utility.Assert;
import org.nakedobjects.utility.DebugString;

import java.util.Date;
import java.util.Enumeration;

import org.apache.log4j.Logger;


public class ObjectLoaderImpl implements NakedObjectLoader {
    private static final Logger LOG = Logger.getLogger(ObjectLoaderImpl.class);
    private ObjectFactory objectFactory;
    private PojoAdapterMap pojoAdapterMap;
    private IdentityAdapterMap identityAdapterMap;
    private AdapterFactory adapterFactory;

    private void addIdentityMapping(Oid oid, NakedReference adapter) {
        LOG.debug("adding identity " + oid + " for " + adapter);
        identityAdapterMap.put(oid, adapter);
    }

    public NakedObject createAdapterForTransient(final Object object) {
        LOG.debug("creating adapter (transient) for " + object);
        NakedObject adapter = createObjectAdapter(object);
        Assert.assertEquals(adapter, pojoAdapterMap.getPojo(object));
        ((PojoAdapter) adapter).changeState(ResolveState.TRANSIENT);
        return adapter;
    }

    /**
     * Creates adapter for Java primitives, or else delegates to the AdapterFactory class.
     */
    public NakedValue createAdapterForValue(final Object value) {
        Assert.assertFalse("can't create an adapter for a NOF adapter", value instanceof Naked);
        Assert.assertFalse("can't create an adapter for a NO Specification", value instanceof NakedObjectSpecification);
        //LOG.debug("creating adapter (value) for " + value);

        NakedValue adapter;
        if (value instanceof String) {
            adapter = new StringAdapter((String) value);

        } else if (value instanceof Date) {
            adapter = new DateAdapter((Date) value);
        
        } else if (value instanceof Boolean) {
            adapter = new BooleanAdapter((Boolean) value);
        
        } else if (value instanceof Character) {
            adapter = new CharAdapter((Character) value);
        
        } else if (value instanceof Byte) {
            adapter = new ByteAdapter((Byte) value);
        
        } else if (value instanceof Short) {
            adapter = new ShortAdapter((Short) value);
        
        } else if (value instanceof Integer) {
            adapter = new IntAdapter((Integer) value);
        
        } else if (value instanceof Long) {
            adapter = new LongAdapter((Long) value);
        
        } else if (value instanceof Float) {
            adapter = new FloatAdapter((Float) value);
        
        } else if (value instanceof Double) {
            adapter = new DoubleAdapter((Double) value);
        
        } else {
            adapter = adapterFactory.createValueAdapter(value);
        }

        return adapter;
    }

    // TODO remove? replace? used by JavaAction.execute()
    public NakedCollection createAdapterForCollection(final Object collection, NakedObjectSpecification specification) {
        Assert.assertFalse("Can't create an adapter for a NOF adapter", collection instanceof Naked);
        LOG.debug("creating adapter (collection) for " + collection);

        NakedCollection adapter;
        adapter = adapterFactory.createCollectionAdapter(collection, specification);
        if(adapter != null) {
            pojoAdapterMap.add(collection, adapter);
            LOG.debug("created " + adapter + " for " + collection);
            adapter.changeState(ResolveState.TRANSIENT);
    
            Assert.assertNotNull(adapter);
        }
        return adapter;
    }

    private NakedObject createObjectAdapter(final Object object) {
        Assert.assertNotNull(object);
        Assert.assertFalse("POJO Map already contains object", object, pojoAdapterMap.containsPojo(object));
        Assert.assertFalse("Can't create an adapter for a NOF adapter", object instanceof Naked);

        NakedObject nakedObject = new PojoAdapter(object);
        pojoAdapterMap.add(object, nakedObject);
        LOG.debug("created PojoAdapter@" + Integer.toHexString(nakedObject.hashCode()) + " for " + object);
        return nakedObject;
    }

    public NakedObject createTransientInstance(NakedObjectSpecification specification) {
        Assert.assertTrue("must be an object", specification.isObject());
        LOG.debug("creating transient instance of " + specification);
        Object object = objectFactory.createObject(specification);
        NakedObject adapter = createAdapterForTransient(object);
        objectFactory.setUpAsNewLogicalObject(object);
        return adapter;
    }

    public NakedCollection recreateCollection(NakedObjectSpecification specification) {
        Assert.assertFalse("must not be an object", specification.isObject());
        Assert.assertFalse("must not be a value", specification.isValue());
        LOG.debug("recreating collection " + specification);
        Object object = objectFactory.createObject(specification);
        NakedCollection adapter = createAdapterForCollection(object, specification);
        return adapter;        
    }
    
    public NakedObject recreateTransientInstance(NakedObjectSpecification specification) {
        Assert.assertTrue("must be an object", specification.isObject());
        LOG.debug("recreating transient instance of for " + specification);
        Object object = objectFactory.createObject(specification);
        NakedObject adapter = createAdapterForTransient(object);
        return adapter;
    }

    public NakedValue createValueInstance(NakedObjectSpecification specification) {
        Assert.assertTrue("must be a value", specification.isValue());
        //LOG.debug("creating value instance of for " + specification);
        Object object = objectFactory.createValueObject(specification);
        return createAdapterForValue(object);
    }

    public NakedObject getAdapterFor(final Object object) {
        Assert.assertNotNull("can't get an adapter for null", this, object);
        NakedObject adapter = (NakedObject) pojoAdapterMap.getPojo(object);
        return adapter;
    }

    public NakedObject getAdapterFor(final Oid oid) {
        Assert.assertNotNull("OID should not be null", this, oid);
        updateOid(oid);
        NakedObject adapter = (NakedObject) identityAdapterMap.get(oid);
        return adapter;
    }

    public NakedCollection getAdapterForElseCreateAdapterForCollection(
            NakedObject parent,
            String fieldName,
            NakedObjectSpecification specification,
            Object collection) {
        Assert.assertNotNull("can't get an adapter for null", this, collection);
        InternalCollectionKey key = new InternalCollectionKey(parent, fieldName);
        NakedCollection adapter = (NakedCollection) pojoAdapterMap.getPojo(key);

        if (adapter == null) {
            adapter = adapterFactory.createCollectionAdapter(collection, specification);
            pojoAdapterMap.add(key, adapter);
            
            if(parent.getResolveState().isPersistent()) {
	            LOG.debug("creating adapter for persistent collection: " + collection);
	            adapter.changeState(ResolveState.GHOST);
            } else {
	            LOG.debug("creating adapter for transient collection: " + collection);
	            adapter.changeState(ResolveState.TRANSIENT);
            }
        }
        Assert.assertNotNull("should have an adapter for ", collection, adapter);
        return adapter;
    }

    public NakedObject getAdapterForElseCreateAdapterForTransient(final Object object) {
        NakedObject adapter = getAdapterFor(object);
        if (adapter == null) {
            LOG.debug("no existing adapter found; creating a transient adapter for " + object);
            adapter = createAdapterForTransient(object);
        }
        Assert.assertNotNull("should have an adapter for ", object, adapter);
        return adapter;
    }

    public String getDebugData() {
        DebugString debug = new DebugString();
        debug.appendTitle("POJO-Adapter Mappings");
        debug.append(pojoAdapterMap);
        debug.appendln();

        debug.appendTitle("Identity-Adapter Mappings");
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
        return "Object Loader";
    }

    public Enumeration getIdentifiedObjects() {
        return identityAdapterMap.elements();
    }

    public void init() {
        LOG.info("initialising " + this);
        Assert.assertNotNull("needs an object factory", objectFactory);
        Assert.assertNotNull("needs an adapter factory", adapterFactory);
        
        if(identityAdapterMap == null) {
            identityAdapterMap = new IdentityAdapterHashMap();
        }
        if(pojoAdapterMap == null) {
            pojoAdapterMap = new PojoAdapterHashMap();
        }
    }

    public boolean isIdentityKnown(Oid oid) {
        Assert.assertNotNull(oid);
        updateOid(oid);
        return identityAdapterMap.containsKey(oid);
    }

    public void start(NakedReference object, ResolveState state) {
        LOG.debug("start " + object + " as " + state.name());
        object.changeState(state);
    }

    public void end(NakedReference object) {
        ResolveState endState = object.getResolveState().getEndState();
        LOG.debug("end " + object + " as " + endState.name());
        object.changeState(endState);
    }

    public void madePersistent(final NakedReference adapter, final Oid assignedOid) {
        LOG.debug("made persistent " + adapter + " as " + assignedOid);
            Assert.assertTrue("No adapter found in map", pojoAdapterMap.getPojo(adapter.getObject()) != null );
            Assert.assertTrue("Not the same adapter in map", pojoAdapterMap.getPojo(adapter.getObject()) == adapter);
            Assert.assertNull("OID should not already map to a known adapter " + assignedOid, identityAdapterMap.get(assignedOid));
        
        adapter.persistedAs(assignedOid);

        addIdentityMapping(assignedOid, adapter);
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

    public NakedObject recreateAdapterForPersistent(Oid oid, Object object) {
        Assert.assertNotNull("must have an OID", oid);
        if (isIdentityKnown(oid)) {
            return getAdapterFor(oid);
        }

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
    public void set_AdapterFactory(AdapterFactory adapterFactory) {
        this.adapterFactory = adapterFactory;
    }
    
    /**
     * Expose as a .Net property.
     * 
     * @property
     */
    public void set_PojoAdapterMap(PojoAdapterMap pojos) {
        this.pojoAdapterMap = pojos;
    }

    public void setIdentityAdapterMap(IdentityAdapterMap identityAdapterMap) {
        this.identityAdapterMap = identityAdapterMap;
    }

    public void setObjectFactory(ObjectFactory objectFactory) {
        this.objectFactory = objectFactory;
    }

    public void setPojoAdapterMap(PojoAdapterMap pojoAdpaterMap) {
        this.pojoAdapterMap = pojoAdpaterMap;
    }

    public void setAdapterFactory(AdapterFactory adapterFactory) {
        this.adapterFactory = adapterFactory;
    }
    
    public void shutdown() {
        LOG.info("shutting down " + this);
        identityAdapterMap.clear();
        identityAdapterMap = null;
        pojoAdapterMap.shutdown();
        adapterFactory = null;
    }

    public void unloaded(NakedObject object) {
        LOG.debug("unload ignored: " + object);
        
        /*
         * TODO need to unload object that are no longer referenced
         * 
         * If an object is unloaded while its pojo still exist then accessing that pojo via the
         * reflector will create a different PojoAdapter and no OID will exist to identify - hence
         * the adapter will appear as transient and will no longer be usable as a persistent object
         */

        LOG.debug("removed loaded object " + object);
        Oid oid = object.getOid();
        if(oid != null) {
            identityAdapterMap.remove(oid);
        }
        pojoAdapterMap.remove(object);
    }
    
    private void updateOid(Oid oid) {
        if(oid.hasPrevious()) {
            NakedObject object = (NakedObject) identityAdapterMap.get(oid.getPrevious());
            if(object != null) {
                LOG.debug("updating oid " + oid.getPrevious() + " to " + oid);
                identityAdapterMap.remove(oid.getPrevious());
                Oid oidFromObject = object.getOid();
                oidFromObject.copyFrom(oid);
                identityAdapterMap.put(oidFromObject, object);
            }
        }
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
