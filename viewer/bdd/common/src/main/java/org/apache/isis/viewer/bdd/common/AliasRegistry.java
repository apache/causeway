package org.apache.isis.viewer.bdd.common;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;

public interface AliasRegistry {

    public ObjectAdapter getAliased(final String alias);

    public String getAlias(final ObjectAdapter adapter);

    /**
     * Holds a new {@link NakedObject adapter}, automatically assigning it a new
     * heldAs alias.
     */
    public String aliasPrefixedAs(final String prefix, final ObjectAdapter adapter);

    /**
     * Holds a new {@link NakedObject adapter}.
     */
    public void aliasAs(final String alias, final ObjectAdapter adapter);

}
