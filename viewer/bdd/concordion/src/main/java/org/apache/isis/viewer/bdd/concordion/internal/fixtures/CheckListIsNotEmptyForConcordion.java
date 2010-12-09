package org.apache.isis.viewer.bdd.concordion.internal.fixtures;

import org.apache.isis.viewer.bdd.common.AliasRegistry;
import org.apache.isis.viewer.bdd.common.fixtures.CheckListIsNotEmptyPeer;

public class CheckListIsNotEmptyForConcordion extends AbstractFixture<CheckListIsNotEmptyPeer> {

    public CheckListIsNotEmptyForConcordion(final AliasRegistry aliasRegistry,
        final String listAlias) {
        super(new CheckListIsNotEmptyPeer(aliasRegistry, listAlias));
    }

    public String execute() {
        boolean isNotEmpty = getPeer().execute();
        return isNotEmpty?"ok":"empty";
    }

}
