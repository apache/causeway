package org.apache.isis.viewer.bdd.concordion.internal.fixtures;

import org.apache.isis.viewer.bdd.common.AliasRegistry;
import org.apache.isis.viewer.bdd.common.fixtures.CheckListIsEmptyPeer;

public class CheckListIsEmptyForConcordion extends AbstractFixture<CheckListIsEmptyPeer> {

    public CheckListIsEmptyForConcordion(final AliasRegistry aliasRegistry,
        final String listAlias) {
        super(new CheckListIsEmptyPeer(aliasRegistry, listAlias));
    }

    public String execute() {
        boolean isEmpty = getPeer().execute();
        return isEmpty?"ok":"not empty";
    }

}
