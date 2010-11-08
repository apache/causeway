package org.apache.isis.viewer.bdd.common.fixtures.perform.checkthat.object;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.viewer.bdd.common.CellBinding;
import org.apache.isis.viewer.bdd.common.StoryBoundValueException;
import org.apache.isis.viewer.bdd.common.fixtures.perform.PerformContext;
import org.apache.isis.viewer.bdd.common.fixtures.perform.checkthat.ThatSubcommandAbstract;

public class NotValid extends ThatSubcommandAbstract {

    public NotValid() {
        super("is not valid", "is invalid", "not valid", "invalid");
    }

    public ObjectAdapter that(final PerformContext performContext) throws StoryBoundValueException {

        CellBinding thatItBinding = performContext.getPeer().getThatItBinding();

        if (performContext.validObjectConsent().isAllowed()) {
        	throw StoryBoundValueException.current(thatItBinding, "(valid)");
        }

        return null;
    }

}
