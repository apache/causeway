package org.apache.isis.viewer.bdd.common.fixtures.perform.checkthat;

import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.viewer.bdd.common.CellBinding;
import org.apache.isis.viewer.bdd.common.StoryBoundValueException;
import org.apache.isis.viewer.bdd.common.fixtures.perform.PerformContext;

public class Hidden extends ThatSubcommandAbstract {

	public Hidden() {
		super("is hidden", "is not visible");
	}

	public ObjectAdapter that(final PerformContext performContext)
			throws StoryBoundValueException {

		if (performContext.visibleMemberConsent().isAllowed()) {
			CellBinding onMemberBinding = performContext.getPeer()
					.getOnMemberBinding();
			throw StoryBoundValueException.current(onMemberBinding, "(visible)");
		}

		return null;
	}

}
