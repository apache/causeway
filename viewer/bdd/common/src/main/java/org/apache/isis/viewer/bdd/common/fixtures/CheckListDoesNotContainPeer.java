package org.apache.isis.viewer.bdd.common.fixtures;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.viewer.bdd.common.AliasRegistry;

public class CheckListDoesNotContainPeer  extends AbstractListFixturePeer {

    public CheckListDoesNotContainPeer(final AliasRegistry aliasesRegistry,
            final String listAlias) {
        super(aliasesRegistry, listAlias);
    }


    /**
     * Returns <tt>true</tt> if collection does not contain specified alias.
     * 
     * <p>
     * If either the list alias is invalid, or the provided alias is 
     * {@link #isValidAlias(String) invalid}, will return <tt>false</tt>.
     */
    public boolean execute(String alias) {
        if(!isValidListAlias()) {
            return false;
        }
        ObjectAdapter adapter = getAliasRegistry().getAliased(alias);
        if(adapter == null) {
            return false;
        }
        return !collectionAdapters().contains(adapter);
    }
    
}
