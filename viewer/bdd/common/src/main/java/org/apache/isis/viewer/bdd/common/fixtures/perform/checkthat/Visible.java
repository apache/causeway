package org.apache.isis.viewer.bdd.common.fixtures.perform.checkthat;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.viewer.bdd.common.CellBinding;
import org.apache.isis.viewer.bdd.common.ScenarioBoundValueException;
import org.apache.isis.viewer.bdd.common.fixtures.perform.PerformContext;

public class Visible extends ThatSubcommandAbstract {

	public Visible() {
		super("is visible", "is not hidden");
	}

	public ObjectAdapter that(final PerformContext performContext)
			throws ScenarioBoundValueException {

		if (performContext.visibleMemberConsent().isVetoed()) {
			CellBinding onMemberBinding = performContext.getPeer()
					.getOnMemberBinding();
			throw ScenarioBoundValueException.current(onMemberBinding, "(hidden)");
		}

		return null;
	}

}
