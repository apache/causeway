package org.apache.isis.extensions.bdd.common.fixtures.perform;

import org.apache.isis.extensions.bdd.common.CellBinding;
import org.apache.isis.extensions.bdd.common.StoryBoundValueException;
import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.consent.Consent;

public class SaveObject extends PerformAbstractTypeParams {

	private ObjectAdapter result;

	public SaveObject(final Perform.Mode mode) {
		super("save", Type.OBJECT, NumParameters.ZERO, mode);
	}

	@Override
	public void doHandle(final PerformContext performContext)
			throws StoryBoundValueException {

		final ObjectAdapter onAdapter = performContext.getOnAdapter();

		final Consent valid = onAdapter.getSpecification().isValid(onAdapter);

		CellBinding performBinding = performContext.getPeer()
				.getPerformBinding();
		if (valid.isVetoed()) {
			throw StoryBoundValueException.current(performBinding,
					valid.getReason());
		}

		if (onAdapter.isPersistent()) {
			throw StoryBoundValueException.current(performBinding, 
					"(already persistent)");
		}

		// persist

		// xactn mgmt now performed by PersistenceSession#makePersistent
		// getOwner().getTransactionManager().startTransaction();
		performContext.getPeer().makePersistent(onAdapter);
		// getOwner().getTransactionManager().endTransaction();

		// all OK.
	}

	public ObjectAdapter getResult() {
		return result;
	}

}
