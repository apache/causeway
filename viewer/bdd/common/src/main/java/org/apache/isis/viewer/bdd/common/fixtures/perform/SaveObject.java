package org.apache.isis.viewer.bdd.common.fixtures.perform;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContext;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.PersistenceSession;
import org.apache.isis.viewer.bdd.common.CellBinding;
import org.apache.isis.viewer.bdd.common.ScenarioBoundValueException;

public class SaveObject extends PerformAbstractTypeParams {

    private ObjectAdapter result;

    public SaveObject(final Perform.Mode mode) {
        super("save", Type.OBJECT, NumParameters.ZERO, mode);
    }

    @Override
    public void doHandle(final PerformContext performContext) throws ScenarioBoundValueException {

        final ObjectAdapter onAdapter = performContext.getOnAdapter();

        final Consent valid = onAdapter.getSpecification().isValid(onAdapter);

        CellBinding performBinding = performContext.getPeer().getPerformBinding();
        if (valid.isVetoed()) {
            throw ScenarioBoundValueException.current(performBinding, valid.getReason());
        }

        if (onAdapter.isPersistent()) {
            throw ScenarioBoundValueException.current(performBinding, "(already persistent)");
        }

        // persist

        // xactn mgmt now performed by PersistenceSession#makePersistent
        // getOwner().getTransactionManager().startTransaction();
        getPersistenceSession().makePersistent(onAdapter);
        // getOwner().getTransactionManager().endTransaction();

        // all OK.
    }

    protected PersistenceSession getPersistenceSession() {
        return IsisContext.getPersistenceSession();
    }

    @Override
    public ObjectAdapter getResult() {
        return result;
    }

}
