package org.apache.isis.viewer.bdd.common.fixtures.perform.checkthat;

import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.viewer.bdd.common.CellBinding;
import org.apache.isis.viewer.bdd.common.StoryBoundValueException;
import org.apache.isis.viewer.bdd.common.fixtures.perform.PerformContext;

public class Visible extends ThatSubcommandAbstract {

	public Visible() {
		super("is visible", "is not hidden");
	}

	public ObjectAdapter that(final PerformContext performContext)
			throws StoryBoundValueException {

		if (performContext.visibleMemberConsent().isVetoed()) {
			CellBinding onMemberBinding = performContext.getPeer()
					.getOnMemberBinding();
			throw StoryBoundValueException.current(onMemberBinding, "(hidden)");
		}

		return null;
	}

}
