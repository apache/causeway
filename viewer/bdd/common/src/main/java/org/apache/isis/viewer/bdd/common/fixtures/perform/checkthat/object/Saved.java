package org.apache.isis.viewer.bdd.common.fixtures.perform.checkthat.object;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.viewer.bdd.common.CellBinding;
import org.apache.isis.viewer.bdd.common.ScenarioBoundValueException;
import org.apache.isis.viewer.bdd.common.fixtures.perform.PerformContext;
import org.apache.isis.viewer.bdd.common.fixtures.perform.checkthat.ThatSubcommandAbstract;

public class Saved extends ThatSubcommandAbstract {

    public Saved() {
        super("is saved", "is persistent", "is persisted", "saved",
                "persistent", "persisted");
    }

    public ObjectAdapter that(final PerformContext performContext) throws ScenarioBoundValueException {

        final ObjectAdapter onAdapter = performContext.getOnAdapter();
        CellBinding thatItBinding = performContext.getPeer().getThatItBinding();

        if (!onAdapter.isPersistent()) {
        	throw ScenarioBoundValueException.current(thatItBinding, "(not saved)");
        }

        return null;
    }

}
