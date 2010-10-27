package org.apache.isis.viewer.bdd.common.fixtures.perform.checkthat.object;

import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.viewer.bdd.common.CellBinding;
import org.apache.isis.viewer.bdd.common.StoryBoundValueException;
import org.apache.isis.viewer.bdd.common.fixtures.perform.PerformContext;
import org.apache.isis.viewer.bdd.common.fixtures.perform.checkthat.ThatSubcommandAbstract;

public class Valid extends ThatSubcommandAbstract {

    public Valid() {
        super("is valid", "valid");
    }

    public ObjectAdapter that(final PerformContext performContext) throws StoryBoundValueException {

        if (!performContext.validObjectConsent().isAllowed()) {
        	CellBinding thatItBinding = performContext.getPeer().getThatItBinding();
        	throw StoryBoundValueException.current(thatItBinding, "(not valid)");
        }

        return null;
    }

}
