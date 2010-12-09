package org.apache.isis.viewer.bdd.concordion.internal.fixtures;

import org.apache.isis.viewer.bdd.common.AliasRegistry;
import org.apache.isis.viewer.bdd.common.fixtures.CheckListContainsPeer;

public class CheckListContainsForConcordion extends AbstractFixture<CheckListContainsPeer> {

    public CheckListContainsForConcordion(final AliasRegistry aliasRegistry,
        final String listAlias) {
        super(new CheckListContainsPeer(aliasRegistry, listAlias));
    }

    public String execute(String alias) {
        if (!getPeer().isValidAlias(alias)) {
            return "unknown alias '" + alias + "'";
        }
        boolean contains = getPeer().execute(alias);
        return contains?"ok":"does not contain '" + alias + "'";
    }

}
