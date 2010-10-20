package org.apache.isis.extensions.bdd.common.fixtures.perform.checkthat;

import org.apache.isis.extensions.bdd.common.CellBinding;
import org.apache.isis.extensions.bdd.common.StoryBoundValueException;
import org.apache.isis.extensions.bdd.common.fixtures.perform.PerformContext;
import org.apache.isis.metamodel.adapter.ObjectAdapter;

public class Usable extends ThatSubcommandAbstract {

	public Usable() {
		super("is usable", "is enabled", "is not disabled");
	}

	public ObjectAdapter that(final PerformContext performContext)
			throws StoryBoundValueException {

		if (!performContext.usableMemberConsent().isAllowed()) {
			CellBinding onMemberBinding = performContext.getPeer()
					.getOnMemberBinding();
			throw StoryBoundValueException.current(onMemberBinding, "(disabled)");
		}

		return null;
	}

}
