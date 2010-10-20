package org.apache.isis.extensions.bdd.common.fixtures.perform.checkthat.object;

import org.apache.isis.extensions.bdd.common.CellBinding;
import org.apache.isis.extensions.bdd.common.StoryBoundValueException;
import org.apache.isis.extensions.bdd.common.fixtures.perform.PerformContext;
import org.apache.isis.extensions.bdd.common.fixtures.perform.checkthat.ThatSubcommandAbstract;
import org.apache.isis.metamodel.adapter.ObjectAdapter;

public class NotSaved extends ThatSubcommandAbstract {

    public NotSaved() {
        super("is not saved", "is not persistent", "is not persisted",
                "not saved", "not persistent", "not persisted");
    }

    public ObjectAdapter that(final PerformContext performContext) throws StoryBoundValueException {

        final ObjectAdapter onAdapter = performContext.getOnAdapter();
        CellBinding thatItBinding = performContext.getPeer().getThatItBinding();

        if (onAdapter.isPersistent()) {
        	throw StoryBoundValueException.current(thatItBinding, "(saved)");
        }

        return null;
    }

}
