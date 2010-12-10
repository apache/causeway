package org.apache.isis.viewer.bdd.common.fixtures;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.viewer.bdd.common.AliasRegistry;

public class CheckCollectionContentsPeer  extends AbstractListFixturePeer {

    public CheckCollectionContentsPeer(final AliasRegistry aliasesRegistry,
            final String listAlias) {
        super(aliasesRegistry, listAlias);
    }


    /**
     * Returns <tt>true</tt> if collection contains specified alias.
     * 
     * <p>
     * If either the list alias is invalid, or the provided alias is 
     * {@link #isValidAlias(String) invalid}, will return <tt>false</tt>.
     */
    public boolean contains(String alias) {
        if(!isValidListAlias()) {
            return false;
        }

        ObjectAdapter adapter = getAliasRegistry().getAliased(alias);
        if(adapter == null) {
            return false;
        }
        return collectionAdapters().contains(adapter);
    }
    

    /**
     * Returns <tt>true</tt> if collection does not contain specified alias.
     * 
     * <p>
     * If either the list alias is invalid, or the provided alias is 
     * {@link #isValidAlias(String) invalid}, will return <tt>false</tt>.
     */
    public boolean doesNotContain(String alias) {
        if(!isValidListAlias()) {
            return false;
        }
        ObjectAdapter adapter = getAliasRegistry().getAliased(alias);
        if(adapter == null) {
            return false;
        }
        return !collectionAdapters().contains(adapter);
    }

    /**
     * Returns <tt>true</tt> if is empty.
     * 
     * @return <tt>false</tt> if the alias is invalid or does not represent a list
     */
    public boolean isEmpty() {
        if(!isValidListAlias()) {
            return false;
        }
        return collectionAdapters().size() == 0;
    }

    
    /**
     * Returns <tt>true</tt> if is not empty.
     * 
     * @return <tt>false</tt> if the alias is invalid or does not represent a list
     */
    public boolean isNotEmpty() {
        if(!isValidListAlias()) {
            return false;
        }

        return collectionAdapters().size() != 0;
    }


    
    /**
     * Returns <tt>true</tt> if collection has specified size.
     * 
     * @return <tt>false</tt> if the alias is invalid or does not represent a list
     */
    public boolean assertSize(int size) {
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
