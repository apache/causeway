package org.apache.isis.viewer.bdd.common.fixtures.perform.checkthat;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.viewer.bdd.common.CellBinding;
import org.apache.isis.viewer.bdd.common.ScenarioBoundValueException;
import org.apache.isis.viewer.bdd.common.fixtures.perform.PerformContext;

public class Hidden extends ThatSubcommandAbstract {

	public Hidden() {
		super("is hidden", "is not visible");
	}

	public ObjectAdapter that(final PerformContext performContext)
			throws ScenarioBoundValueException {

		if (performContext.visibleMemberConsent().isAllowed()) {
			CellBinding onMemberBinding = performContext.getPeer()
					.getOnMemberBinding();
			throw ScenarioBoundValueException.current(onMemberBinding, "(visible)");
		}

		return null;
	}

}
