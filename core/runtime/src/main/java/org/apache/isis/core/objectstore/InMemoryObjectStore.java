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

package org.apache.isis.core.objectstore;

import java.text.MessageFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.core.commons.debug.DebugUtils;
import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.adapter.oid.TypedOid;
import org.apache.isis.core.metamodel.adapter.version.Version;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionFacet;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionFacetUtils;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.SpecificationLoader;
import org.apache.isis.core.metamodel.spec.feature.Contributed;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.objectstore.commands.InMemoryCreateObjectCommand;
import org.apache.isis.core.objectstore.commands.InMemoryDestroyObjectCommand;
import org.apache.isis.core.objectstore.commands.InMemorySaveObjectCommand;
import org.apache.isis.core.objectstore.internal.ObjectStoreInstances;
import org.apache.isis.core.objectstore.internal.ObjectStorePersistedObjects;
import org.apache.isis.core.runtime.persistence.ObjectNotFoundException;
import org.apache.isis.core.runtime.persistence.ObjectPersistenceException;
import org.apache.isis.core.runtime.persistence.UnsupportedFindException;
import org.apache.isis.core.runtime.persistence.objectstore.transaction.CreateObjectCommand;
import org.apache.isis.core.runtime.persistence.objectstore.transaction.DestroyObjectCommand;
import org.apache.isis.core.runtime.persistence.objectstore.transaction.PersistenceCommand;
import org.apache.isis.core.runtime.persistence.objectstore.transaction.SaveObjectCommand;
import org.apache.isis.core.runtime.persistence.query.PersistenceQueryBuiltIn;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.persistence.ObjectStore;
import org.apache.isis.core.runtime.system.persistence.PersistenceQuery;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;
import org.apache.isis.core.runtime.system.persistence.PersistenceSessionFactory;

public class InMemoryObjectStore implements ObjectStore {

    private final static Logger LOG = LoggerFactory.getLogger(InMemoryObjectStore.class);

    protected ObjectStorePersistedObjects persistedObjects;

    public InMemoryObjectStore() {
        LOG.info("creating memory object store");
    }

    // ///////////////////////////////////////////////////////
    // Name
    // ///////////////////////////////////////////////////////

    @Override
    public String name() {
        return "In-Memory Object Store";
    }

    // ///////////////////////////////////////////////////////
    // open, close, shutdown
    // ///////////////////////////////////////////////////////

    @Override
    public void open() {
        // TODO: all a bit hacky, but is to keep tests running. Should really
        // sort out using mocks.
        final InMemoryPersistenceSessionFactory inMemoryPersistenceSessionFactory = getInMemoryPersistenceSessionFactory();
        persistedObjects = inMemoryPersistenceSessionFactory == null ? null : inMemoryPersistenceSessionFactory.getPersistedObjects();
        if (persistedObjects == null) {
            if (inMemoryPersistenceSessionFactory != null) {
                persistedObjects = inMemoryPersistenceSessionFactory.createPersistedObjects();
            } else {
                persistedObjects = new ObjectStorePersistedObjects();
            }
        } else {
            recreateAdapters();
        }
    }

    protected void recreateAdapters() {
        for (final ObjectSpecId noSpec : persistedObjects.specifications()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("recreating adapters for: " + noSpec.asString());
            }
            recreateAdapters(persistedObjects.instancesFor(noSpec));
        }
    }

    private void recreateAdapters(final ObjectStoreInstances objectStoreInstances) {
        for (final Oid oid : objectStoreInstances.getOids()) {

            // it's important not to "touch" the pojo, not even in log messages.
            // That's because
            // the toString() will cause bytecode enhancement to try to resolve
            // references.

            if (LOG.isDebugEnabled()) {
                LOG.debug("recreating adapter: oid=" + oid);
            }
            final Object pojo = objectStoreInstances.getPojo(oid);

            final ObjectAdapter existingAdapterLookedUpByPojo = getAdapterManager().getAdapterFor(pojo);
            if (existingAdapterLookedUpByPojo != null) {
                // this could happen if we rehydrate a persisted object that
                // depends on another persisted object
                // not yet rehydrated.
                getPersistenceSession().getAdapterManager().removeAdapter(existingAdapterLookedUpByPojo);
            }

            final ObjectAdapter existingAdapterLookedUpByOid = getAdapterManager().getAdapterFor(oid);
            if (existingAdapterLookedUpByOid != null) {
                throw new IsisException("A mapping already exists for " + oid + ": " + existingAdapterLookedUpByOid);
            }

            final ObjectAdapter recreatedAdapter = getPersistenceSession().getAdapterManager().mapRecreatedPojo(oid, pojo);

            final Version version = objectStoreInstances.getVersion(oid);
            recreatedAdapter.setVersion(version);
        }
    }

    @Override
    public void close() {
        final InMemoryPersistenceSessionFactory inMemoryPersistenceSessionFactory = getInMemoryPersistenceSessionFactory();
        // TODO: this is hacky, only here to keep tests running. Should sort out
        // using mocks
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
    @Override
    public boolean isFixturesInstalled() {
        return false;
    }

    // ///////////////////////////////////////////////////////
    // reset
    // ///////////////////////////////////////////////////////

    @Override
    public void reset() {
    }

    // ///////////////////////////////////////////////////////
    // Transaction management
    // ///////////////////////////////////////////////////////

    @Override
    public void startTransaction() {
    }

    @Override
    public void endTransaction() {
    }

    @Override
    public void abortTransaction() {
    }

    // ///////////////////////////////////////////////////////
    // Command Creation
    // ///////////////////////////////////////////////////////

    @Override
    public CreateObjectCommand createCreateObjectCommand(final ObjectAdapter object) {
        if (object.getSpecification().isParented()) {
            return null;
        }
        return new InMemoryCreateObjectCommand(object, persistedObjects);
    }

    @Override
    public SaveObjectCommand createSaveObjectCommand(final ObjectAdapter object) {
        if (object.getSpecification().isParented()) {
            return null;
        }
        return new InMemorySaveObjectCommand(object, persistedObjects);
    }

    @Override
    public DestroyObjectCommand createDestroyObjectCommand(final ObjectAdapter object) {
        return new InMemoryDestroyObjectCommand(object, persistedObjects);
    }

    // ///////////////////////////////////////////////////////
    // Command Execution
    // ///////////////////////////////////////////////////////

    @Override
    public void execute(final List<PersistenceCommand> commands) throws ObjectPersistenceException {
        if (LOG.isInfoEnabled()) {
            LOG.info("execute commands");
        }
        for (final PersistenceCommand command : commands) {
            command.execute(null);
        }
        LOG.info("end execution");
    }

    // ///////////////////////////////////////////////////////
    // getObject, resolveField, resolveImmediately
    // ///////////////////////////////////////////////////////

    @Override
    public ObjectAdapter loadInstanceAndAdapt(final TypedOid oid) throws ObjectNotFoundException, ObjectPersistenceException {
        if(LOG.isDebugEnabled()) {
            LOG.debug("getObject " + oid);
        }
        final ObjectSpecification objectSpec = getSpecificationLookup().lookupBySpecId(oid.getObjectSpecId());
        final ObjectStoreInstances ins = instancesFor(objectSpec.getSpecId());
        final ObjectAdapter adapter = ins.getObjectAndMapIfRequired(oid);
        if (adapter == null) {
            throw new ObjectNotFoundException(oid);
        } 
        return adapter;
    }

    @Override
    public void resolveImmediately(final ObjectAdapter adapter) throws ObjectPersistenceException {

        // these diagnostics are because, even though this method is called by
        // PersistenceSessionObjectStore#resolveImmediately which has a check,
        // seem to be hitting a race condition with another thread that is
        // resolving the object before I get here.
        if (adapter.canTransitionToResolving()) {
            if(LOG.isDebugEnabled()) {
                LOG.debug("resolve " + adapter);
            }
        } else {
            LOG.warn("resolveImmediately ignored, " + "adapter's current state is: " + adapter.getResolveState() + " ; oid: " + adapter.getOid());
        }
        
        adapter.markAsResolvedIfPossible();
    }


    // ///////////////////////////////////////////////////////
    // getInstances, hasInstances
    // ///////////////////////////////////////////////////////

    @Override
    public List<ObjectAdapter> loadInstancesAndAdapt(final PersistenceQuery persistenceQuery) throws ObjectPersistenceException, UnsupportedFindException {

        if (!(persistenceQuery instanceof PersistenceQueryBuiltIn)) {
            throw new IllegalArgumentException(MessageFormat.format("Provided PersistenceQuery not supported; was {0}; " + "the in-memory object store only supports {1}", persistenceQuery.getClass().getName(), PersistenceQueryBuiltIn.class.getName()));
        }
        final PersistenceQueryBuiltIn builtIn = (PersistenceQueryBuiltIn) persistenceQuery;

        final List<ObjectAdapter> instances = Lists.newArrayList();
        final ObjectSpecification spec = persistenceQuery.getSpecification();
        findInstances(spec, builtIn, instances);
        return resolved(instances);
    }

    @Override
    public boolean hasInstances(final ObjectSpecification spec) {
        if (instancesFor(spec.getSpecId()).hasInstances()) {
            return true;
        }

        // includeSubclasses
        final List<ObjectSpecification> subclasses = spec.subclasses();
        for (int i = 0; i < subclasses.size(); i++) {
            if (hasInstances(subclasses.get(i))) {
                return true;
            }
        }

        return false;
    }

    private void findInstances(final ObjectSpecification spec, final PersistenceQueryBuiltIn persistenceQuery, final List<ObjectAdapter> foundInstances) {

        instancesFor(spec.getSpecId()).findInstancesAndAdd(persistenceQuery, foundInstances);

        // include subclasses
        final List<ObjectSpecification> subclasses = spec.subclasses();
        for (int i = 0; i < subclasses.size(); i++) {
            findInstances(subclasses.get(i), persistenceQuery, foundInstances);
        }
    }

    private static List<ObjectAdapter> resolved(final List<ObjectAdapter> instances) {
        for (ObjectAdapter adapter: instances) {
            adapter.markAsResolvedIfPossible();
        }
        return instances;
    }

    // ///////////////////////////////////////////////////////
    // Services
    // ///////////////////////////////////////////////////////

    @Override
    public RootOid getOidForService(ObjectSpecification serviceSpec) {
        return (RootOid) persistedObjects.getService(serviceSpec.getSpecId());
    }

    @Override
    public void registerService(final RootOid rootOid) {
        persistedObjects.registerService(rootOid.getObjectSpecId(), rootOid);
    }

    private ObjectStoreInstances instancesFor(final ObjectSpecId spec) {
        return persistedObjects.instancesFor(spec);
    }

    // ///////////////////////////////////////////////////////
    // Debugging
    // ///////////////////////////////////////////////////////

    @Override
    public String debugTitle() {
        return name();
    }

    @Override
    public void debugData(final DebugBuilder debug) {
        debug.appendTitle("Domain Objects");
        for (final ObjectSpecId specId : persistedObjects.specifications()) {
            debug.appendln(specId.asString());
            final ObjectStoreInstances instances = instancesFor(specId);
            instances.debugData(debug);
        }
        debug.unindent();
        debug.appendln();
    }

    private String debugCollectionGraph(final ObjectAdapter collection, final int level, final Vector<ObjectAdapter> recursiveElements) {
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
                    LOG.error(ex.getMessage(), ex);
                    return s.toString();
                }

                s.append(element);
                s.append(debugGraph(element, level + 1, recursiveElements));
            }
        }

        return s.toString();
    }

    private String debugGraph(final ObjectAdapter object, final int level, final Vector<ObjectAdapter> recursiveElements) {
        if (level > 3) {
            return "...\n"; // only go 3 levels?
        }

        Vector<ObjectAdapter> elements;
        if (recursiveElements == null) {
            elements = new Vector<ObjectAdapter>(25, 10);
        } else {
            elements = recursiveElements;
        }

        if (object.getSpecification().isParentedOrFreeCollection()) {
            return "\n" + debugCollectionGraph(object, level, elements);
        } else {
            return "\n" + debugObjectGraph(object, level, elements);
        }
    }

    private String debugObjectGraph(final ObjectAdapter object, final int level, final Vector<ObjectAdapter> recursiveElements) {
        final StringBuffer s = new StringBuffer();

        recursiveElements.addElement(object);

        // work through all its fields
        final List<ObjectAssociation> fields = object.getSpecification().getAssociations(Contributed.EXCLUDED);

        for (int i = 0; i < fields.size(); i++) {
            final ObjectAssociation field = fields.get(i);
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
            s.append(DebugUtils.indentString(4) + "|");
        }

        s.append(DebugUtils.indentString(4) + "+--");
    }

    // ///////////////////////////////////////////////////////
    // Dependencies (from context)
    // ///////////////////////////////////////////////////////

    /**
     * Must use {@link IsisContext context}, because although this object is
     * recreated with each {@link PersistenceSession session}, the persisted
     * objects that get attached to
     * it span multiple sessions.
     * 
     * <p>
     * The alternative design would be to laboriously inject the session into
     * not only this object but also the {@link ObjectStoreInstances} that do
     * the work.
     */
    protected PersistenceSession getPersistenceSession() {
        return IsisContext.getPersistenceSession();
    }

    /**
     * Must use {@link IsisContext context}, because although this object is
     * recreated with each {@link PersistenceSession session}, the persisted
     * objects that get attached to it span multiple sessions.
     * 
     * <p>
     * The alternative design would be to laboriously inject the session into
     * not only this object but also the {@link ObjectStoreInstances} that do
     * the work.
     */
    protected AdapterManager getAdapterManager() {
        return getPersistenceSession().getAdapterManager();
    }

    protected SpecificationLoader getSpecificationLookup() {
        return IsisContext.getSpecificationLoader();
    }


    /**
     * Downcasts the {@link org.apache.isis.core.runtime.system.persistence.PersistenceSessionFactory} to
     * {@link InMemoryPersistenceSessionFactory}.
     */
    protected InMemoryPersistenceSessionFactory getInMemoryPersistenceSessionFactory() {
        final PersistenceSessionFactory persistenceSessionFactory = getPersistenceSession().getPersistenceSessionFactory();

        if (!(persistenceSessionFactory instanceof InMemoryPersistenceSessionFactory)) {
            // for testing support
            return null;
        }
        return (InMemoryPersistenceSessionFactory) persistenceSessionFactory;
    }

    
}
