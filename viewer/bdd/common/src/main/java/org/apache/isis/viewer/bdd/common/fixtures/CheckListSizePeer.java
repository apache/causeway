package org.apache.isis.viewer.bdd.common.fixtures;

import org.apache.isis.viewer.bdd.common.AliasRegistry;

public class CheckListSizePeer  extends AbstractListFixturePeer {

    public CheckListSizePeer(final AliasRegistry aliasesRegistry,
            final String listAlias) {
        super(aliasesRegistry, listAlias);
    }


    /**
     * Returns <tt>true</tt> if collection has specified size.
     * 
     * @return <tt>false</tt> if the alias is invalid or does not represent a list
     */
    public boolean execute(int size) {
        if(!isValidListAlias()) {
            return false;
        }
        return getSize() == size;
    }


    /**
     * Returns the size of the collection.
     * 
     * @return <tt>-1</tt> if the alias is invalid or does not represent a list.
     */
    public int getSize() {
        if(!isValidListAlias()) {
            return -1;
        }

        return collectionAdapters().size();
    }
   
}
