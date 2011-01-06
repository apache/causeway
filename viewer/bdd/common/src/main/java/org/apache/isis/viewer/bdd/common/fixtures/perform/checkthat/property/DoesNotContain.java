package org.apache.isis.viewer.bdd.common.fixtures.perform.checkthat.property;

import org.apache.isis.core.commons.lang.StringUtils;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.viewer.bdd.common.CellBinding;
import org.apache.isis.viewer.bdd.common.ScenarioBoundValueException;
import org.apache.isis.viewer.bdd.common.ScenarioCell;
import org.apache.isis.viewer.bdd.common.fixtures.perform.PerformContext;
import org.apache.isis.viewer.bdd.common.fixtures.perform.checkthat.ThatSubcommandAbstract;

public class DoesNotContain extends ThatSubcommandAbstract {

    public DoesNotContain() {
        super("does not contain", "is not");
    }

    public ObjectAdapter that(final PerformContext performContext) throws ScenarioBoundValueException {

        final OneToOneAssociation otoa = (OneToOneAssociation) performContext
                .getObjectMember();

        // if we have an expected result
        CellBinding arg0Binding = performContext.getPeer().getArg0Binding();
		final ScenarioCell arg0Cell = arg0Binding.getCurrentCell();
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

        if (!StringUtils.isNullOrEmpty(expected)) {

            // see if expected matches an alias
            final ObjectAdapter expectedAdapter = performContext.getPeer().getAliasRegistry().getAliased(expected);
            if (expectedAdapter != null) {
                // known
                if (resultAdapter != expectedAdapter) {
                    return resultAdapter;
                }
                throw ScenarioBoundValueException.current(arg0Binding, "(does contain)");
            }

            // otherwise, compare title
            if (StringUtils.nullSafeEquals(resultTitle, expected)) {
            	throw ScenarioBoundValueException.current(arg0Binding, "(does contain)");
            }
        }

        return resultAdapter;
    }

}
