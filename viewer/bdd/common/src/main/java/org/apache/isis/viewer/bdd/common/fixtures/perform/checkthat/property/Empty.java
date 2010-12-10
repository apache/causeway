package org.apache.isis.viewer.bdd.common.fixtures.perform.checkthat.property;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.viewer.bdd.common.CellBinding;
import org.apache.isis.viewer.bdd.common.ScenarioBoundValueException;
import org.apache.isis.viewer.bdd.common.fixtures.perform.PerformContext;
import org.apache.isis.viewer.bdd.common.fixtures.perform.checkthat.ThatSubcommandAbstract;

public class Empty extends ThatSubcommandAbstract {

	public Empty() {
		super("is empty");
	}

	public ObjectAdapter that(final PerformContext performContext)
			throws ScenarioBoundValueException {

		final OneToOneAssociation otoa = (OneToOneAssociation) performContext
				.getObjectMember();

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
			throw ScenarioBoundValueException.current(thatItBinding, actualStr);
		}

		return null;
	}

}
