package org.apache.isis.viewer.bdd.common.story.registries;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.runtime.context.IsisContext;
import org.apache.isis.runtime.persistence.PersistenceSession;
import org.apache.isis.viewer.bdd.common.AliasRegistry;
import org.apache.isis.viewer.bdd.common.StoryValueException;

public class AliasRegistryDefault implements AliasRegistry {

    private final Map<String, ObjectAdapter> adaptersByAlias = new HashMap<String, ObjectAdapter>();
    private final Map<ObjectAdapter, String> aliasesByAdapter = new HashMap<ObjectAdapter, String>();

    /**
     * @see #nextAlias()
     * @see #aliasPrefixedAs(String, NakedObject)
     */
    private final Map<String, int[]> aliasCountsByPrefix = new TreeMap<String, int[]>();

    @Override
    public void aliasAs(final String alias, final ObjectAdapter adapter) {
        adaptersByAlias.put(alias, adapter);
        aliasesByAdapter.put(adapter, alias);
    }

    @Override
    public ObjectAdapter getAliased(final String alias) {
        return adaptersByAlias.get(alias);
    }

    @Override
    public String getAlias(final ObjectAdapter adapter) {
        return aliasesByAdapter.get(adapter);
    }

    @Override
    public String aliasPrefixedAs(final String prefix, final ObjectAdapter adapter) {
        int[] aliasCountForPrefix = aliasCountsByPrefix.get(prefix);
        if (aliasCountForPrefix == null) {
            aliasCountForPrefix = new int[1];
            aliasCountsByPrefix.put(prefix, aliasCountForPrefix);
        }
        final String nextAliasForPrefix = nextAlias(prefix, aliasCountForPrefix);
        adaptersByAlias.put(nextAliasForPrefix, adapter);
        return nextAliasForPrefix;
    }

    private String nextAlias(final String prefix, final int[] heldAsCount) {
        return prefix + "#" + (++heldAsCount[0]);
    }

    @Override
    public Iterator<Entry<String, ObjectAdapter>> iterator() {
        final Set<Entry<String, ObjectAdapter>> entrySet = adaptersByAlias.entrySet();
        return Collections.unmodifiableSet(entrySet).iterator();
    }

    @Override
    public void aliasService(final String aliasAs, final String className) throws StoryValueException {
        final List<ObjectAdapter> serviceAdapters = getPersistenceSession().getServices();
        for (final ObjectAdapter serviceAdapter : serviceAdapters) {
            if (serviceAdapter.getSpecification().getFullName().equals(className)) {
                adaptersByAlias.put(aliasAs, serviceAdapter);
                return;
            }
        }
        throw new StoryValueException("no such service");
    }

    protected PersistenceSession getPersistenceSession() {
        return IsisContext.getPersistenceSession();
    }

}
