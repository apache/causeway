package org.apache.isis.viewer.bdd.concordion.internal.fixtures;

import org.apache.isis.viewer.bdd.common.AliasRegistry;
import org.apache.isis.viewer.bdd.common.CellBinding;
import org.apache.isis.viewer.bdd.common.CellBindingDefault;
import org.apache.isis.viewer.bdd.common.IsisViewerConstants;
import org.apache.isis.viewer.bdd.common.StoryBoundValueException;
import org.apache.isis.viewer.bdd.common.StoryCellDefault;
import org.apache.isis.viewer.bdd.common.StoryValueException;
import org.apache.isis.viewer.bdd.common.fixtures.AliasItemsInListPeer;

public class AliasItemsInListForConcordion extends AbstractFixture<AliasItemsInListPeer> {

    public AliasItemsInListForConcordion(final AliasRegistry aliasRegistry, final String listAlias) {
        this(aliasRegistry, listAlias, 
            CellBindingDefault.builder(IsisViewerConstants.TITLE_NAME,
            IsisViewerConstants.TITLE_HEAD).build(), CellBindingDefault
            .builder(IsisViewerConstants.TYPE_NAME, IsisViewerConstants.TYPE_HEAD).optional().build(),
            CellBindingDefault
                .builder(IsisViewerConstants.ALIAS_RESULT_NAME, IsisViewerConstants.ALIAS_RESULT_HEAD_SET).autoCreate()
                .build());
    }

    private AliasItemsInListForConcordion(final AliasRegistry aliasRegistry, final String listAlias,
        final CellBinding titleBinding, final CellBinding typeBinding, final CellBinding aliasBinding) {
        super(new AliasItemsInListPeer(aliasRegistry, listAlias, titleBinding, typeBinding, aliasBinding));
    }

    public String execute(String aliasAs, String title, String type) {
        String header = executeHeader(aliasAs, title, type);
        if (header != null) {
            return header;
        }

        String row = executeRow(aliasAs, title, type);
        if (row != null) {
            return row;
        }

        return "ok"; // ok
    }

    private String executeHeader(String alias, String title, String type) {
        try {
            getPeer().assertIsList();
        } catch (StoryValueException e) {
            return e.getMessage();
        }

        // create bindings
        getPeer().getTitleBinding().setHeadColumn(0);
        getPeer().getAliasBinding().setHeadColumn(1);

        if (type != null) {
            getPeer().getTypeBinding().setHeadColumn(2, new StoryCellDefault(type));
        }

        return null;
    }

    private String executeRow(String aliasAs, String title, String type) {
        if (!getPeer().isList()) {
            return null; // skip
        }

        captureCurrent(aliasAs, title, type);

        try {
            getPeer().findAndAlias();
        } catch (StoryBoundValueException e) {
            return e.getMessage();
        }

        return null;
    }

    private void captureCurrent(String aliasAs, String title, String type) {
        getPeer().getAliasBinding().captureCurrent(new StoryCellDefault(aliasAs));
        getPeer().getTitleBinding().captureCurrent(new StoryCellDefault(title));
        if (type != null) {
            getPeer().getTitleBinding().captureCurrent(new StoryCellDefault(type));
        }
    }

}
