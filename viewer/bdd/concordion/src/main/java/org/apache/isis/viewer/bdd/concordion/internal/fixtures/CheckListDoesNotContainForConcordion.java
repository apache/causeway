package org.apache.isis.viewer.bdd.concordion.internal.fixtures;

import org.apache.isis.viewer.bdd.common.AliasRegistry;
import org.apache.isis.viewer.bdd.common.fixtures.CheckListDoesNotContainPeer;

public class CheckListDoesNotContainForConcordion extends AbstractFixture<CheckListDoesNotContainPeer> {

    public CheckListDoesNotContainForConcordion(final AliasRegistry aliasRegistry,
        final String listAlias) {
        super(new CheckListDoesNotContainPeer(aliasRegistry, listAlias));
    }

    public String execute(String alias) {
        if (!getPeer().isValidAlias(alias)) {
            return "unknown alias '" + alias + "'";
        }
        boolean doesNotContain = getPeer().execute(alias);
        return doesNotContain?"ok":"does contain";
    }

}
