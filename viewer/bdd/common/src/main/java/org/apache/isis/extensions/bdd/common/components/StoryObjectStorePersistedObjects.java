package org.apache.isis.extensions.bdd.common.components;

import java.util.HashMap;
import java.util.Map;

import org.apache.isis.commons.exceptions.IsisException;
import org.apache.isis.metamodel.adapter.oid.Oid;
import org.apache.isis.metamodel.facets.object.cached.CachedFacet;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.runtime.objectstore.inmemory.internal.ObjectStoreInstances;
import org.apache.isis.runtime.objectstore.inmemory.internal.ObjectStorePersistedObjects;
import org.apache.isis.runtime.persistence.oidgenerator.simple.SimpleOidGenerator.Memento;

import com.google.common.collect.Iterables;

/**
 * Stores instances in one of two maps, based on whether have their
 * specification has the {@link CachedFacet} (represents cached or reference
 * data) or not (represents operational data).
 * <p>
 * Those that are cached are stored in a <tt>static</tt> map that is never
 * {@link #clear()}ed down. Those that are operational go in a regular instance
 * cache and can be {@link #clear()}ed.
 */
public class StoryObjectStorePersistedObjects implements
        ObjectStorePersistedObjects {

    private static final Map<ObjectSpecification, ObjectStoreInstances> cachedInstancesBySpecMap =
            new HashMap<ObjectSpecification, ObjectStoreInstances>();

    private final Map<ObjectSpecification, ObjectStoreInstances> operationalInstancesBySpecMap;

    private final Map<String, Oid> serviceOidByIdMap;
    private Memento oidGeneratorMemento;

    public StoryObjectStorePersistedObjects() {
        operationalInstancesBySpecMap = new HashMap<ObjectSpecification, ObjectStoreInstances>();
        serviceOidByIdMap = new HashMap<String, Oid>();
    }

    public Memento getOidGeneratorMemento() {
        return oidGeneratorMemento;
    }

    public void saveOidGeneratorMemento(final Memento memento) {
        this.oidGeneratorMemento = memento;
    }

    public Oid getService(final String name) {
        return serviceOidByIdMap.get(name);
    }

    public void registerService(final String name, final Oid oid) {
        final Oid oidLookedUpByName = serviceOidByIdMap.get(name);
        if (oidLookedUpByName != null) {
            if (!oidLookedUpByName.equals(oid)) {
                throw new IsisException(
                        "Already another service registered as name: " + name
                        + " (existing Oid: " + oidLookedUpByName + ", "
                        + "intended: " + oid + ")");
            }
        } else {
            serviceOidByIdMap.put(name, oid);
        }
    }

    public Iterable<ObjectSpecification> specifications() {
        return Iterables.concat(
                StoryObjectStorePersistedObjects.cachedInstancesBySpecMap
                .keySet(), operationalInstancesBySpecMap.keySet());
    }

    public Iterable<ObjectStoreInstances> instances() {
        return Iterables.concat(
                StoryObjectStorePersistedObjects.cachedInstancesBySpecMap
                .values(), operationalInstancesBySpecMap.values());
    }

    public ObjectStoreInstances instancesFor(final ObjectSpecification spec) {
        if (isCached(spec)) {
            return getFromMap(
                    spec,
                    StoryObjectStorePersistedObjects.cachedInstancesBySpecMap);
        } else {
            return getFromMap(spec, operationalInstancesBySpecMap);
        }
    }

    // //////////////////////////////////////////////////////////////////
    // Helpers
    // //////////////////////////////////////////////////////////////////

    private ObjectStoreInstances getFromMap(
            final ObjectSpecification spec,
            final Map<ObjectSpecification, ObjectStoreInstances> map) {
        ObjectStoreInstances ins = map.get(spec);
        if (ins == null) {
            ins = new ObjectStoreInstances(spec);
            map.put(spec, ins);
        }
        return ins;
    }

    private boolean isCached(final ObjectSpecification spec) {
        return spec.containsFacet(CachedFacet.class);
    }

    /**
     * Only clears the operational instances, not the cached instances.
     */
    public void clear() {
        operationalInstancesBySpecMap.clear();
    }

}
