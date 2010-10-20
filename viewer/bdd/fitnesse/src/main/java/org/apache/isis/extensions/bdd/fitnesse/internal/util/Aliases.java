package org.apache.isis.extensions.bdd.fitnesse.internal.util;

import org.apache.isis.extensions.bdd.common.CellBinding;
import org.apache.isis.extensions.bdd.common.Constants;
import org.apache.isis.extensions.bdd.fitnesse.internal.fixtures.perform.StoryCellForFitNesse;

import fit.Fixture;
import fit.Parse;

public final class Aliases {

    private Aliases() {}

    public static CellBinding findOrCreateAlias(final Parse heads,
            final CellBinding aliasInfo) {
        Parse eachHead = heads;
        for (int i = 0; eachHead != null; i++, eachHead = eachHead.more) {
            final String headText = eachHead.body;

            if (aliasInfo.matches(headText)) {
                aliasInfo.foundHeadColumn(i, new StoryCellForFitNesse(eachHead));
                return aliasInfo;
            }
        }

        // Append an alias cell to header
        if (!aliasInfo.isFound()) {
            final int size = heads.size();
            final Parse aliasCell = new Parse("td", Fixture
                    .gray(Constants.ALIAS_RESULT_HEAD), null, null);
            heads.last().more = aliasCell;
            aliasInfo.create(size, new StoryCellForFitNesse(aliasCell));
        }
        return aliasInfo;
    }

    public static boolean hasAliasText(final String headText) {
        return Constants.ALIAS_RESULT_HEAD_ALT1.equals(headText)
                || Constants.ALIAS_RESULT_HEAD.equals(headText);
    }

}
