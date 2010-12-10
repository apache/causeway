package org.apache.isis.viewer.bdd.concordion.internal.fixtures;

import org.apache.isis.viewer.bdd.common.AliasRegistry;
import org.apache.isis.viewer.bdd.common.fixtures.CheckCollectionContentsPeer;

public class CheckCollectionContentsForConcordion extends AbstractFixture<CheckCollectionContentsPeer> {

    public CheckCollectionContentsForConcordion(final AliasRegistry aliasRegistry,
        final String listAlias) {
        super(new CheckCollectionContentsPeer(aliasRegistry, listAlias));
    }

    public String contains(String alias) {
        if (!getPeer().isValidAlias(alias)) {
            return "unknown alias '" + alias + "'";
        }
        boolean contains = getPeer().contains(alias);
        return contains?"ok":"does not contain '" + alias + "'";
    }

    public String doesNotContain(String alias) {
        if (!getPeer().isValidAlias(alias)) {
            return "unknown alias '" + alias + "'";
        }
        boolean doesNotContain = getPeer().doesNotContain(alias);
        return doesNotContain?"ok":"does contain";
    }


    public String isEmpty() {
        boolean isEmpty = getPeer().isEmpty();
        return isEmpty?"ok":"not empty";
    }

    
    public String isNotEmpty() {
        boolean isNotEmpty = getPeer().isNotEmpty();
        return isNotEmpty?"ok":"empty";
    }


    public String assertSize(int size) {
        boolean hasSize = getPeer().assertSize(size);
        return hasSize?"ok":"contains " + getPeer().getSize() + " objects";
    }

}

