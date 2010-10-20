package org.apache.isis.extensions.bdd.fitnesse.internal.fixtures;

import org.apache.isis.extensions.bdd.common.AliasRegistry;
import org.apache.isis.extensions.bdd.common.fixtures.DebugObjectStorePeer;
import org.apache.isis.extensions.bdd.fitnesse.internal.AbstractFixture;

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
