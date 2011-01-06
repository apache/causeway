package net.sf.isiscontrib.bdd.fitnesse.internal.fixtures;

import net.sf.isiscontrib.bdd.fitnesse.internal.AbstractFixture;
import net.sf.isiscontrib.bdd.fitnesse.internal.CellBindingForFitNesse;
import net.sf.isiscontrib.bdd.fitnesse.internal.util.FitnesseUtil;

import org.apache.isis.core.commons.lang.StringUtils;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.viewer.bdd.common.AliasRegistry;
import org.apache.isis.viewer.bdd.common.CellBinding;
import org.apache.isis.viewer.bdd.common.IsisViewerConstants;
import org.apache.isis.viewer.bdd.common.fixtures.SetUpObjectsPeer;
import org.apache.isis.viewer.bdd.common.fixtures.SetUpObjectsPeer.AssociationVisitor;
import org.apache.isis.viewer.bdd.common.fixtures.SetUpObjectsPeer.PropertyResult;
import org.apache.isis.viewer.bdd.common.fixtures.SetUpObjectsPeer.SetUpObjectResult;

import fit.Fixture;
import fit.Parse;

public class SetUpObjectsForFitNesse extends AbstractFixture<SetUpObjectsPeer> {

    public SetUpObjectsForFitNesse(final AliasRegistry aliasesRegistry, final String className,
        final SetUpObjectsPeer.Mode mode) {
        this(aliasesRegistry, className, mode, CellBindingForFitNesse
            .builder(IsisViewerConstants.ALIAS_RESULT_NAME, IsisViewerConstants.ALIAS_RESULT_HEAD_SET).autoCreate().build());
    }

    private SetUpObjectsForFitNesse(final AliasRegistry aliasesRegistry, final String className,
        final SetUpObjectsPeer.Mode mode, final CellBinding aliasBinding) {
        super(new SetUpObjectsPeer(aliasesRegistry, className, mode, aliasBinding));
    }

    @Override
    public void doTable(final Parse table) {

        final Parse classNameCell = table.parts.parts.more;
        if (getPeer().isSpecOk()) {
            FitnesseUtil.exception(this, classNameCell, "(no such class)");
            return;
        }

        super.doTable(table);

        if (!getPeer().getAliasBinding().isFound()) {
            // no alias column has been provided, so we'll be adding one.

            final Parse blankFinalCell = new Parse("th", "", null, null);
            classNameCell.last().more = blankFinalCell;
        }
    }

    @Override
    protected void doRowsWithBindings(final Parse headRow) {
        Parse eachHead = headRow.parts;
        try {
            eachHead = addProperties(eachHead);
        } catch (final Throwable throwable) {
            exception(eachHead, throwable);
        }
    }

    private Parse addProperties(Parse eachHead) {
        for (int i = 0; eachHead != null; i++, eachHead = eachHead.more) {
            final String headText = eachHead.text();

            PropertyResult status = addProperty(headText, i);
            if (!status.isOk()) {
                if (status == PropertyResult.NO_SUCH_PROPERTY) {
                    FitnesseUtil.exception(this, eachHead, "(no such property)");
                } else if (status == PropertyResult.NOT_A_PROPERTY) {
                    FitnesseUtil.exception(this, eachHead, "(not a property)");
                }
            }
        }
        return eachHead;
    }

    private PropertyResult addProperty(final String heading, int colNum) {
        return getPeer().definePropertyOrAlias(heading, colNum);
    }

    @Override
    public void doRow(final Parse row) {

        // copied down from superclass
        clearCellTextList();

        doCells(row.parts);
        if (!getPeer().getAliasBinding().isFound()) {
            addCellText(""); // placeholder
        }

        try {
            setUpObject(row);
            rightAllKnownAssociationCells(row);
        } catch (final Exception e) {
            wrongAllKnownAssociationCells(row);
            reportError(row, e);
        }
    }

    private void clearCellTextList() {
        getPeer().resetForNextObject();
    }

    private boolean addCellText(String cellText) {
        return getPeer().addPropertyValueOrAlias(cellText);
    }

    @Override
    public void doCell(final Parse cell, final int column) {
        super.doCell(cell, column);
        String cellText = cell.text();
        addCellText(cellText);
    }

    private void setUpObject(final Parse row) throws Exception {

        // spec for adapter should have been initialized in doTable.
        final ObjectAdapter adapter = getPeer().createInstance();
        if (adapter == null) {
            return;
        }

        // TODO: redo using createObject(), catch the StoryFailureException instead
        for (int colNum = 0; colNum < getPeer().getProperties().size(); colNum++) {

            SetUpObjectResult result = getPeer().setUpProperty(adapter, colNum);

            if (!result.isHandled()) {
                final Parse cell = FitnesseUtil.cell(row, colNum);
                if (result.isError()) {
                    FitnesseUtil.exception(this, cell, result.getErrorMessage());
                } else {
                    right(cell);
                }
            }
        }

        getPeer().persistIfNecessary(adapter);

        if (getPeer().getAliasBinding().isFound()) {
            // TODO: review - this block may be redundant since the peer now captures
            // the alias when the text is added (earlier).
            int aliasColumn = getPeer().getAliasBinding().getColumn();
            final Parse aliasCell = FitnesseUtil.cell(row, aliasColumn);
            if (aliasCell != null) {
                String alias = aliasCell.text();
                if (StringUtils.isNullOrEmpty(alias)) {
                    alias = getPeer().aliasFor(adapter);
                    FitnesseUtil.setBody(aliasCell, Fixture.gray(alias));
                } else {
                    getPeer().getAliasRegistry().aliasAs(alias, adapter);
                }
            }
        } else {
            final String alias = getPeer().aliasFor(adapter);
            appendCell(row, Fixture.gray(alias));
        }
    }

    private void rightAllKnownAssociationCells(final Parse row) {
        getPeer().forEachAssociation(new AssociationVisitor() {
            @Override
            public void visit(OneToOneAssociation association, int i) {
                final Parse cell = FitnesseUtil.cell(row, i);
                if (cell != null) {
                    right(cell);
                }
            }
        });
    }

    private void wrongAllKnownAssociationCells(final Parse row) {
        getPeer().forEachAssociation(new AssociationVisitor() {
            @Override
            public void visit(OneToOneAssociation association, int i) {
                final Parse cell = FitnesseUtil.cell(row, i);
                if (cell != null) {
                    wrong(cell);
                }
            }
        });
    }

    private Parse appendCell(final Parse row, final String text) {
        final Parse lastCell = new Parse("td", text, null, null);
        row.parts.last().more = lastCell;
        return lastCell;
    }

}
