package org.apache.isis.viewer.bdd.common.fixtures;

import org.apache.isis.viewer.bdd.common.AliasRegistry;

public class CheckListIsNotEmptyPeer  extends AbstractListFixturePeer {

    public CheckListIsNotEmptyPeer(final AliasRegistry aliasesRegistry,
            final String listAlias) {
        super(aliasesRegistry, listAlias);
    }


    /**
     * Returns <tt>true</tt> if is not empty.
     * 
     * @return <tt>false</tt> if the alias is invalid or does not represent a list
     */
    public boolean execute() {
        if(!isValidListAlias()) {
            return false;
        }

        return collectionAdapters().size() != 0;
    }

}
