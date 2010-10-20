package org.apache.isis.extensions.bdd.common.fixtures.perform.checkthat;

import org.apache.isis.extensions.bdd.common.CellBinding;
import org.apache.isis.extensions.bdd.common.StoryBoundValueException;
import org.apache.isis.extensions.bdd.common.fixtures.perform.PerformContext;
import org.apache.isis.metamodel.adapter.ObjectAdapter;

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
