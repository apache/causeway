package org.apache.isis.extensions.bdd.common.fixtures.perform.checkthat.object;

import org.apache.isis.extensions.bdd.common.CellBinding;
import org.apache.isis.extensions.bdd.common.StoryBoundValueException;
import org.apache.isis.extensions.bdd.common.fixtures.perform.PerformContext;
import org.apache.isis.extensions.bdd.common.fixtures.perform.checkthat.ThatSubcommandAbstract;
import org.apache.isis.metamodel.adapter.ObjectAdapter;

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
