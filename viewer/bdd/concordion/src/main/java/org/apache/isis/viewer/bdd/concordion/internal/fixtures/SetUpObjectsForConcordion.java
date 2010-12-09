package org.apache.isis.viewer.bdd.concordion.internal.fixtures;

import org.apache.isis.viewer.bdd.common.AliasRegistry;
import org.apache.isis.viewer.bdd.common.CellBinding;
import org.apache.isis.viewer.bdd.common.CellBindingDefault;
import org.apache.isis.viewer.bdd.common.IsisViewerConstants;
import org.apache.isis.viewer.bdd.common.StoryBoundValueException;
import org.apache.isis.viewer.bdd.common.fixtures.SetUpObjectsPeer;

public class SetUpObjectsForConcordion extends AbstractFixture<SetUpObjectsPeer> {

    public SetUpObjectsForConcordion(final AliasRegistry aliasesRegistry, final String className,
        final SetUpObjectsPeer.Mode mode) {
        this(aliasesRegistry, className, mode, CellBindingDefault
            .builder(IsisViewerConstants.ALIAS_RESULT_NAME, IsisViewerConstants.ALIAS_RESULT_HEAD_SET).autoCreate().build());
    }

    private SetUpObjectsForConcordion(final AliasRegistry storyFixture, final String className,
        final SetUpObjectsPeer.Mode mode, final CellBinding aliasBinding) {
        super(new SetUpObjectsPeer(storyFixture, className, mode, aliasBinding));
    }

    public String executeHeader(String alias, String... propertyNames) {

        // create bindings (there's only one)
        getPeer().getAliasBinding().setHeadColumn(0);

        // define properties and the alias column
        int colNum = 0;
        getPeer().definePropertyOrAlias(alias, colNum++);

        for (String propertyName : propertyNames) {
            getPeer().definePropertyOrAlias(propertyName, colNum++);
        }

        return ""; // ok
    }

    public String executeRow(String alias, String... propertyValues) {

        // set property values and the alis
        getPeer().addPropertyValueOrAlias(alias);
        for (String propertyValue : propertyValues) {
            getPeer().addPropertyValueOrAlias(propertyValue);
        }

        // create the object
        try {
            getPeer().createObject();
            return "ok";
        } catch (StoryBoundValueException ex) {
            return ex.toString();
        }

    }

}
