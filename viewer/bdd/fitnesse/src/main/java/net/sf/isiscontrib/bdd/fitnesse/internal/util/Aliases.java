package net.sf.isiscontrib.bdd.fitnesse.internal.util;

import net.sf.isiscontrib.bdd.fitnesse.internal.fixtures.perform.ScenarioCellForFitNesse;

import org.apache.isis.viewer.bdd.common.CellBinding;
import org.apache.isis.viewer.bdd.common.IsisViewerConstants;

import fit.Fixture;
import fit.Parse;

public final class Aliases {

    private Aliases() {
    }

    public static CellBinding findOrCreateAlias(final Parse heads, final CellBinding aliasInfo) {
        Parse eachHead = heads;
        for (int i = 0; eachHead != null; i++, eachHead = eachHead.more) {
            final String headText = eachHead.body;

            if (aliasInfo.matches(headText)) {
                aliasInfo.setHeadColumn(i, new ScenarioCellForFitNesse(eachHead));
                return aliasInfo;
            }
        }

        // Append an alias cell to header
        if (!aliasInfo.isFound()) {
            final int size = heads.size();
            final Parse aliasCell = new Parse("td", Fixture.gray(IsisViewerConstants.ALIAS_RESULT_HEAD), null, null);
            heads.last().more = aliasCell;
            aliasInfo.createHeadCell(size, new ScenarioCellForFitNesse(aliasCell));
        }
        return aliasInfo;
    }

    public static boolean hasAliasText(final String headText) {
        return IsisViewerConstants.ALIAS_RESULT_HEAD_ALT1.equals(headText) || IsisViewerConstants.ALIAS_RESULT_HEAD.equals(headText);
    }

}
