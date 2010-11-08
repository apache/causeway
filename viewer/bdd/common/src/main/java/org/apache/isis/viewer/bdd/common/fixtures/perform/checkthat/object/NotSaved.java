package org.apache.isis.viewer.bdd.common.fixtures.perform.checkthat.object;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.viewer.bdd.common.CellBinding;
import org.apache.isis.viewer.bdd.common.StoryBoundValueException;
import org.apache.isis.viewer.bdd.common.fixtures.perform.PerformContext;
import org.apache.isis.viewer.bdd.common.fixtures.perform.checkthat.ThatSubcommandAbstract;

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
