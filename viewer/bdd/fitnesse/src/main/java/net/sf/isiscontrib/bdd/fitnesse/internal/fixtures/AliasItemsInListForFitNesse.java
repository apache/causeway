package net.sf.isiscontrib.bdd.fitnesse.internal.fixtures;

import net.sf.isiscontrib.bdd.fitnesse.internal.AbstractFixture;
import net.sf.isiscontrib.bdd.fitnesse.internal.CellBindingForFitNesse;
import net.sf.isiscontrib.bdd.fitnesse.internal.util.FitnesseUtil;

import org.apache.isis.viewer.bdd.common.AliasRegistry;
import org.apache.isis.viewer.bdd.common.CellBinding;
import org.apache.isis.viewer.bdd.common.IsisViewerConstants;
import org.apache.isis.viewer.bdd.common.ScenarioBoundValueException;
import org.apache.isis.viewer.bdd.common.ScenarioCell;
import org.apache.isis.viewer.bdd.common.ScenarioValueException;
import org.apache.isis.viewer.bdd.common.fixtures.AliasItemsInListPeer;

import fit.Parse;

public class AliasItemsInListForFitNesse extends AbstractFixture<AliasItemsInListPeer> {

    public AliasItemsInListForFitNesse(final AliasRegistry aliasesRegistry, final String listAlias) {
        this(aliasesRegistry, listAlias, CellBindingForFitNesse.builder(IsisViewerConstants.TITLE_NAME, IsisViewerConstants.TITLE_HEAD)
            .build(), CellBindingForFitNesse.builder(IsisViewerConstants.TYPE_NAME, IsisViewerConstants.TYPE_HEAD).optional().build(),
            CellBindingForFitNesse.builder(IsisViewerConstants.ALIAS_RESULT_NAME, IsisViewerConstants.ALIAS_RESULT_HEAD_SET).autoCreate()
                .build());
    }

    private AliasItemsInListForFitNesse(final AliasRegistry aliasRegistry, final String listAlias,
        final CellBinding titleBinding, final CellBinding typeBinding, final CellBinding aliasBinding) {
        super(new AliasItemsInListPeer(aliasRegistry, listAlias, titleBinding, typeBinding, aliasBinding));
    }

    @Override
    public void doTable(final Parse table) {
        final Parse listAliasCell = table.parts.parts.more;

        try {
            getPeer().assertIsList();
        } catch (ScenarioValueException e) {
            FitnesseUtil.exception(this, listAliasCell, e.getMessage());
        }

        super.doTable(table);
    }

    @Override
    public void doRow(final Parse row) {

        if (!getPeer().isList()) {
            return; // skip
        }

        doCells(row.parts);

        try {
            executeRow();
        } catch (final Exception e) {
            reportError(row, e);
        }
    }

    private void executeRow() {
        ScenarioCell currentCell;
        try {
            currentCell = getPeer().findAndAlias();
            right(FitnesseUtil.asParse(currentCell));
        } catch (ScenarioBoundValueException ex) {
            FitnesseUtil.exception(this, ex);
        }
    }

}
