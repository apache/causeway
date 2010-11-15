package net.sf.isiscontrib.bdd.fitnesse.internal.fixtures;

import net.sf.isiscontrib.bdd.fitnesse.internal.AbstractFixture;

import org.apache.isis.viewer.bdd.common.AliasRegistry;
import org.apache.isis.viewer.bdd.common.fixtures.DebugObjectStorePeer;

import fit.Parse;

public class DebugObjectStoreForFitNesse extends AbstractFixture<DebugObjectStorePeer> {

    public DebugObjectStoreForFitNesse(final AliasRegistry aliasesRegistry) {
        super(new DebugObjectStorePeer(aliasesRegistry));
    }

    @Override
    public void doTable(final Parse table) {
        super.doTable(table);
        final Parse last = table.parts.last();
        final String debugLine = debugObjectStore();
        Parse row = last;
        final Parse errorCell = makeMessageCell(debugLine);
        insertRowAfter(row, new Parse("tr", null, errorCell, null));
    }

    private String debugObjectStore() {
        return getPeer().debugObjectStore();
    }

}
