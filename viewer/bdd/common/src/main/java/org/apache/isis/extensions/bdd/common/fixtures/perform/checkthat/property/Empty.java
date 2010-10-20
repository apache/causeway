package org.apache.isis.extensions.bdd.common.fixtures.perform.checkthat.property;

import org.apache.isis.extensions.bdd.common.CellBinding;
import org.apache.isis.extensions.bdd.common.StoryBoundValueException;
import org.apache.isis.extensions.bdd.common.fixtures.perform.PerformContext;
import org.apache.isis.extensions.bdd.common.fixtures.perform.checkthat.ThatSubcommandAbstract;
import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.spec.feature.OneToOneAssociation;

public class Empty extends ThatSubcommandAbstract {

	public Empty() {
		super("is empty");
	}

	public ObjectAdapter that(final PerformContext performContext)
			throws StoryBoundValueException {

		final OneToOneAssociation otoa = (OneToOneAssociation) performContext
				.getNakedObjectMember();

		// get
		final ObjectAdapter resultAdapter = otoa.get(performContext
				.getOnAdapter());

		if (resultAdapter != null) {
			String actualStr = performContext.getPeer().getAliasRegistry()
					.getAlias(resultAdapter);
			if (actualStr == null) {
				actualStr = resultAdapter.titleString();
			}
			CellBinding thatItBinding = performContext.getPeer()
					.getThatItBinding();
			throw StoryBoundValueException.current(thatItBinding, actualStr);
		}

		return null;
	}

}
