/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */


package org.apache.isis.runtime.objectstore.inmemory;

import java.text.MessageFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.apache.isis.commons.debug.Debug;
import org.apache.isis.commons.debug.DebugString;
import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.adapter.ResolveState;
import org.apache.isis.metamodel.adapter.oid.Oid;
import org.apache.isis.metamodel.adapter.version.Version;
import org.apache.isis.metamodel.facets.collections.modify.CollectionFacet;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.metamodel.util.CollectionFacetUtils;
import org.apache.isis.runtime.context.IsisContext;
import org.apache.isis.runtime.objectstore.inmemory.internal.ObjectStoreInstances;
import org.apache.isis.runtime.objectstore.inmemory.internal.ObjectStorePersistedObjects;
import org.apache.isis.runtime.objectstore.inmemory.internal.ObjectStorePersistedObjectsDefault;
import org.apache.isis.runtime.objectstore.inmemory.internal.commands.InMemoryCreateObjectCommand;
import org.apache.isis.runtime.objectstore.inmemory.internal.commands.InMemoryDestroyObjectCommand;
import org.apache.isis.runtime.objectstore.inmemory.internal.commands.InMemorySaveObjectCommand;
import org.apache.isis.runtime.persistence.ObjectNotFoundException;
import org.apache.isis.runtime.persistence.PersistenceSession;
import org.apache.isis.runtime.persistence.PersistenceSessionFactory;
import org.apache.isis.runtime.persistence.PersistenceSessionHydrator;
import org.apache.isis.runtime.persistence.PersistorUtil;
import org.apache.isis.runtime.persistence.UnsupportedFindException;
import org.apache.isis.runtime.persistence.adaptermanager.AdapterManager;
import org.apache.isis.runtime.persistence.objectstore.ObjectStore;
import org.apache.isis.runtime.persistence.objectstore.transaction.CreateObjectCommand;
import org.apache.isis.runtime.persistence.objectstore.transaction.DestroyObjectCommand;
import org.apache.isis.runtime.persistence.objectstore.transaction.PersistenceCommand;
import org.apache.isis.runtime.persistence.objectstore.transaction.SaveObjectCommand;
import org.apache.isis.runtime.persistence.query.PersistenceQuery;
import org.apache.isis.runtime.persistence.query.PersistenceQueryBuiltIn;
import org.apache.isis.runtime.transaction.ObjectPersistenceException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.apache.isis.commons.ensure.Ensure.ensureThatState;


public class InMemoryObjectStore implements ObjectStore {

    private final static Logger LOG = Logger.getLogger(InMemoryObjectStore.class);

    protected ObjectStorePersistedObjects persistedObjects;

    public InMemoryObjectStore() {
        LOG.info("creating memory object store");
    }

    // ///////////////////////////////////////////////////////
    // Name
    // ///////////////////////////////////////////////////////

    public String name() {
        return "In-Memory Object Store";
    }

    // ///////////////////////////////////////////////////////
    // open, close, shutdown
    // ///////////////////////////////////////////////////////

    public void open() {
        // TODO: all a bit hacky, but is to keep tests running.  Should really sort out using mocks.
        InMemoryPersistenceSessionFactory inMemoryPersistenceSessionFactory = getInMemoryPersistenceSessionFactory();
        persistedObjects = inMemoryPersistenceSessionFactory == null ? null : inMemoryPersistenceSessionFactory.getPersistedObjects();
        if (persistedObjects == null) {
        	if (inMemoryPersistenceSessionFactory != null) {
        		persistedObjects = inMemoryPersistenceSessionFactory.createPersistedObjects();
        	} else {
        		persistedObjects = new ObjectStorePersistedObjectsDefault();
        	}
        } else {
            recreateAdapters();
        }
    }

    private void recreateAdapters() {
        for(ObjectSpecification noSpec: persistedObjects.specifications()) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("recreating adapters for: " + noSpec.getFullName());
			}
        	recreateAdapters(persistedObjects.instancesFor(noSpec));
        }
    }

	private void recreateAdapters(ObjectStoreInstances objectStoreInstances) {
		for (Oid oid : objectStoreInstances.getOids()) {

		    // it's important not to "touch" the pojo, not even in log messages.  That's because 
		    // the toString() will cause bytecode enhancement to try to resolve references.

			if (LOG.isDebugEnabled()) {
				LOG.debug("recreating adapter: oid=" + oid);
			}
		    Object pojo = objectStoreInstances.getPojo(oid);

		    ObjectAdapter existingAdapterLookedUpByPojo = getAdapterManager().getAdapterFor(pojo);
		    if (existingAdapterLookedUpByPojo != null) {
		    	// this could happen if we rehydrate a persisted object that depends on another persisted object
		    	// not yet rehydrated.
		    	getAdapterManager().removeAdapter(existingAdapterLookedUpByPojo);
		    }

		    ObjectAdapter existingAdapterLookedUpByOid = getAdapterManager().getAdapterFor(oid);
		    ensureThatState(existingAdapterLookedUpByOid, is(nullValue()), "Already have mapping for " + oid);

		    ObjectAdapter recreatedAdapter = getHydrator().recreateAdapter(oid, pojo);
		    
		    Version version = objectStoreInstances.getVersion(oid);
		    recreatedAdapter.setOptimisticLock(version);
		}
	}

	public void close() {
        final InMemoryPersistenceSessionFactory inMemoryPersistenceSessionFactory = getInMemoryPersistenceSessionFactory();
        // TODO: this is hacky, only here to keep tests running.  Should sort out using mocks
        if (inMemoryPersistenceSessionFactory != null) {
        	inMemoryPersistenceSessionFactory.attach(getPersistenceSession(), persistedObjects);
        	persistedObjects = null;
        } 
    }


    // ///////////////////////////////////////////////////////
    // fixtures
    // ///////////////////////////////////////////////////////

    /**
     * No permanent persistence, so must always install fixtures.
     */
    public boolean isFixturesInstalled() {
        return false;
    }

    // ///////////////////////////////////////////////////////
    // reset
    // ///////////////////////////////////////////////////////

    public void reset() {}

    // ///////////////////////////////////////////////////////
    // Transaction management
    // ///////////////////////////////////////////////////////

    public void startTransaction() {}

    public void endTransaction() {}

    public void abortTransaction() {}

    // ///////////////////////////////////////////////////////
    // Command Creation
    // ///////////////////////////////////////////////////////

    public CreateObjectCommand createCreateObjectCommand(final ObjectAdapter object) {
        return new InMemoryCreateObjectCommand(object, persistedObjects);
    }

    public SaveObjectCommand createSaveObjectCommand(final ObjectAdapter object) {
        return new InMemorySaveObjectCommand(object, persistedObjects);
    }

    public DestroyObjectCommand createDestroyObjectCommand(final ObjectAdapter object) {
        return new InMemoryDestroyObjectCommand(object, persistedObjects);
    }

    // ///////////////////////////////////////////////////////
    // Command Execution
    // ///////////////////////////////////////////////////////

    public void execute(final List<PersistenceCommand> commands) throws ObjectPersistenceException {
        if (LOG.isInfoEnabled()) {
            LOG.info("execute commands");
        }
        for (PersistenceCommand command : commands) {
            command.execute(null);
        }
        LOG.info("end execution");
    }

    // ///////////////////////////////////////////////////////
    // getObject, resolveField, resolveImmediately
    // ///////////////////////////////////////////////////////

    public ObjectAdapter getObject(final Oid oid, final ObjectSpecification hint) throws ObjectNotFoundException,
            ObjectPersistenceException {
        LOG.debug("getObject " + oid);
        final ObjectStoreInstances ins = instancesFor(hint);
        final ObjectAdapter object = ins.retrieveObject(oid);
        if (object == null) {
            throw new ObjectNotFoundException(oid);
        } else {
            setupReferencedObjects(object);
            return object;
        }
    }

    public void resolveImmediately(final ObjectAdapter adapter) throws ObjectPersistenceException {

        // this is a nasty hack, but even though this method is called by 
        // PersistenceSessionObjectStore#resolveImmediately which has a check,
    	// seem to be hitting a race condition with another thread that is resolving the object
    	// before I get here.
    	// as belt-n-braces, have also made PSOS#resolveImmediately synchronize on
    	// the object being resolved.
        if (adapter.getResolveState().canChangeTo(ResolveState.RESOLVING)) {
            LOG.debug("resolve " + adapter);
            setupReferencedObjects(adapter);
            
        	PersistorUtil.start(adapter, ResolveState.RESOLVING);
        	PersistorUtil.end(adapter); // moves to RESOLVED
        } else {
        	LOG.warn("resolveImmediately ignored, " +
        			 "adapter's current state is: " + adapter.getResolveState() + 
        			 " ; oid: " + adapter.getOid());
        }
    }

    public void resolveField(final ObjectAdapter object, final ObjectAssociation field) throws ObjectPersistenceException {
        final ObjectAdapter reference = field.get(object);
        PersistorUtil.start(reference, ResolveState.RESOLVING);
        PersistorUtil.end(reference);
    }

    private void setupReferencedObjects(final ObjectAdapter object) {
        setupReferencedObjects(object, new Vector());
    }

    private void setupReferencedObjects(final ObjectAdapter adapter, final Vector all) {
    	// TODO: is this code needed, then?  Looks like it isn't...
        if (true) {
            return;
        }

        if (adapter == null || all.contains(adapter)) {
            return;
        }
        all.addElement(adapter);
        PersistorUtil.start(adapter, ResolveState.RESOLVING);

        final ObjectAssociation[] fields = adapter.getSpecification().getAssociations();
        for (int i = 0; i < fields.length; i++) {
            final ObjectAssociation field = fields[i];
            if (field.isOneToManyAssociation()) {
                final ObjectAdapter col = field.get(adapter);
                final CollectionFacet facet = CollectionFacetUtils.getCollectionFacetFromSpec(col);
                for (final Iterator<ObjectAdapter> e = facet.iterator(col); e.hasNext();) {
                    final ObjectAdapter element = e.next();
                    setupReferencedObjects(element, all);
                }
            } else if (field.isOneToOneAssociation()) {
                final ObjectAdapter fieldContent = field.get(adapter);
                setupReferencedObjects(fieldContent, all);
            }
        }

        PersistorUtil.end(adapter);

    }

    // ///////////////////////////////////////////////////////
    // getInstances, hasInstances
    // ///////////////////////////////////////////////////////

    public ObjectAdapter[] getInstances(final PersistenceQuery persistenceQuery) 
    throws ObjectPersistenceException,
            UnsupportedFindException {

    	if (!(persistenceQuery instanceof PersistenceQueryBuiltIn)) {
    		throw new IllegalArgumentException(MessageFormat.format(
							"Provided PersistenceQuery not supported; was {0}; " +
							"the in-memory object store only supports {1}",
							persistenceQuery.getClass().getName(), 
							PersistenceQueryBuiltIn.class.getName()));
    	}
		PersistenceQueryBuiltIn builtIn = (PersistenceQueryBuiltIn) persistenceQuery;
    	
    	final Vector<ObjectAdapter> instances = new Vector<ObjectAdapter>();
        final ObjectSpecification spec = persistenceQuery.getSpecification();
        findInstances(spec, builtIn, instances);
        return toInstancesArray(instances);
    }

    public boolean hasInstances(final ObjectSpecification spec) {
        if (instancesFor(spec).hasInstances()) {
            return true;
        }
        
        // includeSubclasses
        final ObjectSpecification[] subclasses = spec.subclasses();
        for (int i = 0; i < subclasses.length; i++) {
            if (hasInstances(subclasses[i])) {
                return true;
            }
        }
        
        return false;
    }

    private void findInstances(
            final ObjectSpecification spec,
            final PersistenceQueryBuiltIn persistenceQuery,
            final Vector<ObjectAdapter> foundInstances) {
    	
        instancesFor(spec).findInstancesAndAdd(persistenceQuery, foundInstances);

        // include subclasses
        final ObjectSpecification[] subclasses = spec.subclasses();
        for (int i = 0; i < subclasses.length; i++) {
            findInstances(subclasses[i], persistenceQuery, foundInstances);
        }
        
    }

    private ObjectAdapter[] toInstancesArray(final Vector<ObjectAdapter> instances) {
        final ObjectAdapter[] ins = new ObjectAdapter[instances.size()];
        for (int i = 0; i < ins.length; i++) {
            final ObjectAdapter object = instances.elementAt(i);
            setupReferencedObjects(object);
            if (object.getResolveState().canChangeTo(ResolveState.RESOLVING)) {
                PersistorUtil.start(object, ResolveState.RESOLVING);
                PersistorUtil.end(object);
            }
            ins[i] = object;
        }
        return ins;
    }

    // ///////////////////////////////////////////////////////
    // Services
    // ///////////////////////////////////////////////////////


    public Oid getOidForService(final String name) {
        return persistedObjects.getService(name);
    }

    public void registerService(final String name, final Oid oid) {
    	persistedObjects.registerService(name, oid);
    }

    private ObjectStoreInstances instancesFor(final ObjectSpecification spec) {
    	return persistedObjects.instancesFor(spec);
    }

    // ///////////////////////////////////////////////////////
    // Debugging
    // ///////////////////////////////////////////////////////

    public String debugTitle() {
        return name();
    }

    public void debugData(final DebugString debug) {
        debug.appendTitle("Domain Objects");
        for(final ObjectSpecification spec: persistedObjects.specifications()) {
            debug.appendln(spec.getFullName());
            final ObjectStoreInstances instances = instancesFor(spec);
            instances.debugData(debug);
        }
        debug.unindent();
        debug.appendln();
    }

    private String debugCollectionGraph(final ObjectAdapter collection, final int level, final Vector recursiveElements) {
        final StringBuffer s = new StringBuffer();

        if (recursiveElements.contains(collection)) {
            s.append("*\n");
        } else {
            recursiveElements.addElement(collection);

            final CollectionFacet facet = CollectionFacetUtils.getCollectionFacetFromSpec(collection);
            final Iterator<ObjectAdapter> e = facet.iterator(collection);

            while (e.hasNext()) {
                indent(s, level);

                ObjectAdapter element;
                try {
                    element = e.next();
                } catch (final ClassCastException ex) {
                    LOG.error(ex);
                    return s.toString();
                }

                s.append(element);
                s.append(debugGraph(element, level + 1, recursiveElements));
            }
        }

        return s.toString();
    }

    private String debugGraph(final ObjectAdapter object, final int level, final Vector recursiveElements) {
        if (level > 3) {
            return "...\n"; // only go 3 levels?
        }

        Vector elements;
        if (recursiveElements == null) {
            elements = new Vector(25, 10);
        } else {
            elements = recursiveElements;
        }

        if (object.getSpecification().isCollection()) {
            return "\n" + debugCollectionGraph(object, level, elements);
        } else {
            return "\n" + debugObjectGraph(object, level, elements);
        }
    }

    private String debugObjectGraph(final ObjectAdapter object, final int level, final Vector recursiveElements) {
        final StringBuffer s = new StringBuffer();

        recursiveElements.addElement(object);

        // work through all its fields
        ObjectAssociation[] fields;

        fields = object.getSpecification().getAssociations();

        for (int i = 0; i < fields.length; i++) {
            final ObjectAssociation field = fields[i];
            final Object obj = field.get(object);

            final String id = field.getId();
            indent(s, level);

            if (field.isOneToManyAssociation()) {
                s.append(id + ": \n" + debugCollectionGraph((ObjectAdapter) obj, level + 1, recursiveElements));
            } else {
                if (recursiveElements.contains(obj)) {
                    s.append(id + ": " + obj + "*\n");
                } else {
                    s.append(id + ": " + obj);
                    s.append(debugGraph((ObjectAdapter) obj, level + 1, recursiveElements));
                }
            }
        }

        return s.toString();
    }

    private void indent(final StringBuffer s, final int level) {
        for (int indent = 0; indent < level; indent++) {
            s.append(Debug.indentString(4) + "|");
        }

        s.append(Debug.indentString(4) + "+--");
    }

    

    
    // ///////////////////////////////////////////////////////
    // Dependencies (from context)
    // ///////////////////////////////////////////////////////

    /**
     * Must use {@link IsisContext context}, because although this object is recreated with each
     * {@link PersistenceSession session}, the persisted objects that get
     * {@link #attachPersistedObjects(ObjectStorePersistedObjects) attached} to it span multiple
     * sessions.
     * 
     * <p>
     * The alternative design would be to laboriously inject the session into not only
     * this object but also the {@link ObjectStoreInstances} that do the work.
     */
    protected PersistenceSession getPersistenceSession() {
        return IsisContext.getPersistenceSession();
    }

    /**
     * Must use {@link IsisContext context}, because although this object is recreated with each
     * {@link PersistenceSession session}, the persisted objects that get
     * {@link #attachPersistedObjects(ObjectStorePersistedObjects) attached} to it span multiple
     * sessions.
     * 
     * <p>
     * The alternative design would be to laboriously inject the session into not only
     * this object but also the {@link ObjectStoreInstances} that do the work.
     */
    protected AdapterManager getAdapterManager() {
        return getPersistenceSession().getAdapterManager();
    }

    /**
     * Must use {@link IsisContext context}, because although this object is recreated with each
     * {@link PersistenceSession session}, the persisted objects that get
     * {@link #attachPersistedObjects(ObjectStorePersistedObjects) attached} to it span multiple
     * sessions.
     * 
     * <p>
     * The alternative design would be to laboriously inject the session into not only
     * this object but also the {@link ObjectStoreInstances} that do the work.
     */
    protected PersistenceSessionHydrator getHydrator() {
        return getPersistenceSession();
    }

    
	/**
	 * Downcasts the {@link PersistenceSessionFactory} to {@link InMemoryPersistenceSessionFactory}.
	 */
	protected InMemoryPersistenceSessionFactory getInMemoryPersistenceSessionFactory() {
		PersistenceSessionFactory persistenceSessionFactory = getPersistenceSession().getPersistenceSessionFactory();

        if (!(persistenceSessionFactory instanceof InMemoryPersistenceSessionFactory)) {
        	// for testing support
            return null;
        }
        return (InMemoryPersistenceSessionFactory) persistenceSessionFactory;
	}


}
