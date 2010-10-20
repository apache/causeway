package org.apache.isis.extensions.bdd.fitnesse.internal.fixtures;

import org.apache.isis.extensions.bdd.common.AliasRegistry;
import org.apache.isis.extensions.bdd.common.CellBinding;
import org.apache.isis.extensions.bdd.common.Constants;
import org.apache.isis.extensions.bdd.common.StoryCell;
import org.apache.isis.extensions.bdd.common.fixtures.DebugClockPeer;
import org.apache.isis.extensions.bdd.fitnesse.internal.AbstractFixture;
import org.apache.isis.extensions.bdd.fitnesse.internal.CellBindingForFitNesse;

import fit.Fixture;
import fit.Parse;

public class DebugClockForFitNesse extends AbstractFixture<DebugClockPeer> {

    public DebugClockForFitNesse(final AliasRegistry aliasesRegistry) {
        this(aliasesRegistry, CellBindingForFitNesse.builder(Constants.VALUE_NAME,
                Constants.VALUE_HEAD).build());
    }

    private DebugClockForFitNesse(final AliasRegistry aliasesRegistry,
            final CellBinding valueBinding) {
        super(new DebugClockPeer(aliasesRegistry, valueBinding));
    }

    @Override
    public void doRow(final Parse row) {
        super.doRow(row);
        readClock();
    }

    public void readClock() {
        final String formattedClockTime = getFormattedClockTime();
        getCurrentCell().setText(Fixture.gray(formattedClockTime));
    }

	public String getFormattedClockTime() {
		return getPeer().getFormattedClockTime();
	}

	private StoryCell getCurrentCell() {
		return getPeer().getCurrentCell();
	}

}
