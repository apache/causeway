package org.apache.isis.viewer.bdd.common.fixtures.perform.checkthat.property;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.viewer.bdd.common.CellBinding;
import org.apache.isis.viewer.bdd.common.StoryBoundValueException;
import org.apache.isis.viewer.bdd.common.StoryCell;
import org.apache.isis.viewer.bdd.common.fixtures.perform.PerformContext;
import org.apache.isis.viewer.bdd.common.fixtures.perform.checkthat.ThatSubcommandAbstract;
import org.apache.isis.viewer.bdd.common.util.Strings;

public class DoesNotContain extends ThatSubcommandAbstract {

    public DoesNotContain() {
        super("does not contain", "is not");
    }

    public ObjectAdapter that(final PerformContext performContext) throws StoryBoundValueException {

        final OneToOneAssociation otoa = (OneToOneAssociation) performContext
                .getNakedObjectMember();

        // if we have an expected result
        CellBinding arg0Binding = performContext.getPeer().getArg0Binding();
		final StoryCell arg0Cell = arg0Binding.getCurrentCell();
        final String expected = arg0Cell.getText();

        // get
        final ObjectAdapter resultAdapter = otoa.get(performContext
                .getOnAdapter());

        // see if matches null
        if (resultAdapter == null) {
            // ok
            return null;
        }

        final String resultTitle = resultAdapter.titleString();

        if (!Strings.emptyString(expected)) {

            // see if expected matches an alias
            final ObjectAdapter expectedAdapter = performContext.getPeer().getAliasRegistry().getAliased(expected);
            if (expectedAdapter != null) {
                // known
                if (resultAdapter != expectedAdapter) {
                    return resultAdapter;
                }
                throw StoryBoundValueException.current(arg0Binding, "(does contain)");
            }

            // otherwise, compare title
            if (Strings.nullSafeEquals(resultTitle, expected)) {
            	throw StoryBoundValueException.current(arg0Binding, "(does contain)");
            }
        }

        return resultAdapter;
    }

}
