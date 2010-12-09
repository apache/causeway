package org.apache.isis.viewer.bdd.concordion.internal.fixtures;

import org.apache.isis.viewer.bdd.common.AliasRegistry;
import org.apache.isis.viewer.bdd.common.fixtures.CheckListSizePeer;

public class CheckListSizeForConcordion extends AbstractFixture<CheckListSizePeer> {

    public CheckListSizeForConcordion(final AliasRegistry aliasRegistry,
        final String listAlias) {
        super(new CheckListSizePeer(aliasRegistry, listAlias));
    }

    public String execute(int size) {
        boolean isEmpty = getPeer().execute(size);
        return isEmpty?"ok":"contains " + getPeer().getSize() + " objects";
    }

}
