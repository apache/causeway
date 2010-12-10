package org.apache.isis.viewer.bdd.common.fixtures.perform.checkthat.object;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.viewer.bdd.common.CellBinding;
import org.apache.isis.viewer.bdd.common.ScenarioBoundValueException;
import org.apache.isis.viewer.bdd.common.fixtures.perform.PerformContext;
import org.apache.isis.viewer.bdd.common.fixtures.perform.checkthat.ThatSubcommandAbstract;

public class Valid extends ThatSubcommandAbstract {

    public Valid() {
        super("is valid", "valid");
    }

    public ObjectAdapter that(final PerformContext performContext) throws ScenarioBoundValueException {

        if (!performContext.validObjectConsent().isAllowed()) {
        	CellBinding thatItBinding = performContext.getPeer().getThatItBinding();
        	throw ScenarioBoundValueException.current(thatItBinding, "(not valid)");
        }

        return null;
    }

}
