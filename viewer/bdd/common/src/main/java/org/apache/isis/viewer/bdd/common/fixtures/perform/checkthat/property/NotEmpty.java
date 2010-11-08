package org.apache.isis.viewer.bdd.common.fixtures.perform.checkthat.property;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.viewer.bdd.common.CellBinding;
import org.apache.isis.viewer.bdd.common.StoryBoundValueException;
import org.apache.isis.viewer.bdd.common.fixtures.perform.PerformContext;
import org.apache.isis.viewer.bdd.common.fixtures.perform.checkthat.ThatSubcommandAbstract;

public class NotEmpty extends ThatSubcommandAbstract {

	public NotEmpty() {
		super("is not empty");
	}

	public ObjectAdapter that(final PerformContext performContext)
			throws StoryBoundValueException {

		final OneToOneAssociation otoa = (OneToOneAssociation) performContext
				.getNakedObjectMember();

		// get
		final ObjectAdapter resultAdapter = otoa.get(performContext
				.getOnAdapter());

		if (resultAdapter == null) {
			CellBinding thatItBinding = performContext.getPeer()
					.getThatItBinding();
			throw StoryBoundValueException.current(thatItBinding, 
					"(empty)");
		}

		return null;
	}

}
