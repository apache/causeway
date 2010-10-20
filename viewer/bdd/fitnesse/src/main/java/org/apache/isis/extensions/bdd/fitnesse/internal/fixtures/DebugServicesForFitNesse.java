package org.apache.isis.extensions.bdd.fitnesse.internal.fixtures;

import org.apache.isis.extensions.bdd.common.AliasRegistry;
import org.apache.isis.extensions.bdd.common.fixtures.DebugServicesPeer;
import org.apache.isis.extensions.bdd.fitnesse.internal.AbstractFixture;

import fit.Parse;

public class DebugServicesForFitNesse extends AbstractFixture<DebugServicesPeer> {

    public DebugServicesForFitNesse(final AliasRegistry aliasesRegistry) {
        super(new DebugServicesPeer(aliasesRegistry));
    }

    @Override
    public void doTable(final Parse table) {
        super.doTable(table);
        final Parse last = table.parts.last();
        final String debugLine = debugServices();
        Parse row = last;
        final Parse errorCell = makeMessageCell(debugLine);
        insertRowAfter(row, new Parse("tr", null, errorCell, null));
    }

    private String debugServices() {
    	return getPeer().debugServices();
    }

}
