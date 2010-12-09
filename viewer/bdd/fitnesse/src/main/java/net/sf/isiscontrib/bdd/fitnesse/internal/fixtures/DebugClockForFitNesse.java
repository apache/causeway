package net.sf.isiscontrib.bdd.fitnesse.internal.fixtures;

import net.sf.isiscontrib.bdd.fitnesse.internal.AbstractFixture;
import net.sf.isiscontrib.bdd.fitnesse.internal.CellBindingForFitNesse;

import org.apache.isis.viewer.bdd.common.AliasRegistry;
import org.apache.isis.viewer.bdd.common.CellBinding;
import org.apache.isis.viewer.bdd.common.IsisViewerConstants;
import org.apache.isis.viewer.bdd.common.StoryCell;
import org.apache.isis.viewer.bdd.common.fixtures.DebugClockPeer;

import fit.Fixture;
import fit.Parse;

public class DebugClockForFitNesse extends AbstractFixture<DebugClockPeer> {

    public DebugClockForFitNesse(final AliasRegistry aliasesRegistry) {
        this(aliasesRegistry, CellBindingForFitNesse.builder(IsisViewerConstants.VALUE_NAME, IsisViewerConstants.VALUE_HEAD).build());
    }

    private DebugClockForFitNesse(final AliasRegistry aliasesRegistry, final CellBinding valueBinding) {
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
