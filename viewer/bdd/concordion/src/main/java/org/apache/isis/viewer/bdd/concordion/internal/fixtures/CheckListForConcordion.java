package org.apache.isis.viewer.bdd.concordion.internal.fixtures;

import org.apache.isis.viewer.bdd.common.AliasRegistry;
import org.apache.isis.viewer.bdd.common.CellBindingDefault;
import org.apache.isis.viewer.bdd.common.ScenarioCell;
import org.apache.isis.viewer.bdd.common.ScenarioCellDefault;
import org.apache.isis.viewer.bdd.common.fixtures.CheckListConstants;
import org.apache.isis.viewer.bdd.common.fixtures.CheckListPeer;
import org.apache.isis.viewer.bdd.common.fixtures.CheckListPeer.CheckMode;

public class CheckListForConcordion extends AbstractFixture<CheckListPeer> {

    public CheckListForConcordion(final AliasRegistry aliasRegistry, final String listAlias) {
        super(new CheckListPeer(aliasRegistry, listAlias, CheckMode.NOT_EXACT, titleBinding()));
    }

    private static CellBindingDefault titleBinding() {
        return CellBindingDefault
            .builder(CheckListConstants.TITLE_NAME, CheckListConstants.TITLE_HEAD_SET).ditto().build();
    }

    public String executeHeader(String title) {
        return setupHeader(title);
    }

    private String setupHeader(String title) {
        int colNum = 0;
        getPeer().getTitleBinding().setHeadColumn(colNum++);
        return ""; // ok
    }

    public String executeRow(String title) {

        setupHeader(title);

        // capture current
        getPeer().getTitleBinding().captureCurrent(new ScenarioCellDefault(title));

        // execute
        return checkExists();
    }

    private String checkExists() {
        if(!getPeer().findAndAddObject()) {
            return getTitle() + " not found";
        }
        return "ok";
    }
    private String getTitle() {
        ScenarioCell currentCell = getPeer().getTitleBinding().getCurrentCell();
        return currentCell!=null?currentCell.getText():"(no title provided)";
    }

}
