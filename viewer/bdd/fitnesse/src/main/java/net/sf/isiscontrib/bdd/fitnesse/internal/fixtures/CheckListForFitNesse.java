package net.sf.isiscontrib.bdd.fitnesse.internal.fixtures;

import java.util.List;

import net.sf.isiscontrib.bdd.fitnesse.internal.AbstractFixture;
import net.sf.isiscontrib.bdd.fitnesse.internal.CellBindingForFitNesse;
import net.sf.isiscontrib.bdd.fitnesse.internal.util.FitnesseUtil;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.viewer.bdd.common.AliasRegistry;
import org.apache.isis.viewer.bdd.common.CellBinding;
import org.apache.isis.viewer.bdd.common.Constants;
import org.apache.isis.viewer.bdd.common.StoryValueException;
import org.apache.isis.viewer.bdd.common.fixtures.CheckListPeer;
import org.apache.isis.viewer.bdd.common.fixtures.CheckListPeer.CheckMode;

import fit.Fixture;
import fit.Parse;

public class CheckListForFitNesse extends AbstractFixture<CheckListPeer> {

    public CheckListForFitNesse(final AliasRegistry aliasesRegistry, final String listAlias, final CheckMode checkMode) {
        this(aliasesRegistry, listAlias, checkMode, CellBindingForFitNesse.builder(Constants.TITLE_NAME,
            Constants.TITLE_HEAD).build(), CellBindingForFitNesse.builder(Constants.TYPE_NAME, Constants.TYPE_HEAD)
            .optional().build());
    }

    protected CheckListForFitNesse(final AliasRegistry aliasesRegistry, final String listAlias,
        final CheckMode checkMode, final CellBinding titleBinding, final CellBinding typeBinding) {
        super(new CheckListPeer(aliasesRegistry, listAlias, checkMode, titleBinding, typeBinding));
    }

    @Override
    public void doTable(final Parse table) {
        final Parse listAliasCell = table.parts.parts.more;

        try {
            getPeer().assertIsList();
        } catch (StoryValueException e) {
            FitnesseUtil.exception(this, listAliasCell, e.getMessage());
            return;
        }

        super.doTable(table);

        if (getPeer().isCheckModeExact()) {
            reportSurplus(table);
        }
    }

    private void reportSurplus(final Parse table) {

        final List<ObjectAdapter> notFoundAdapters = getPeer().getNotFoundAdapters();
        if (notFoundAdapters.size() == 0) {
            return;
        }

        final int colSpan = 3; // TODO: shouldn't hardcode this
        Parse lastRow = table.parts.last();
        for (final ObjectAdapter notFoundAdapter : notFoundAdapters) {
            final Parse titleCell = new Parse("td", "", null, null);
            titleCell.addToTag(" colspan=\"" + colSpan + "\"");
            titleCell.addToBody(notFoundAdapter.titleString() + "<hr");
            wrong(titleCell);
            titleCell.addToBody(Fixture.label("(expected)"));

            final Parse row = new Parse("tr", null, titleCell, null);

            lastRow.more = row;
            lastRow = row;
        }
    }

    @Override
    public void doRow(final Parse row) {

        if (!getPeer().isList()) {
            return; // skip
        }

        doCells(row.parts);

        try {
            if (!getPeer().findAndAddObject()) {
                FitnesseUtil.exception(this, getPeer().getTitleBinding().getCurrentCell(), "(not found)");
            }
        } catch (final Exception e) {
            reportError(row, e);
        }
    }

}
