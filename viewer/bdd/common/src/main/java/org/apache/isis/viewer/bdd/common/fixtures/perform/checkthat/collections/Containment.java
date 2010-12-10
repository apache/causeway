package org.apache.isis.viewer.bdd.common.fixtures.perform.checkthat.collections;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;
import org.apache.isis.viewer.bdd.common.CellBinding;
import org.apache.isis.viewer.bdd.common.ScenarioBoundValueException;
import org.apache.isis.viewer.bdd.common.ScenarioCell;
import org.apache.isis.viewer.bdd.common.fixtures.perform.PerformContext;
import org.apache.isis.viewer.bdd.common.fixtures.perform.checkthat.AssertsContainment;

public class Containment extends ThatAbstract {

	private final AssertsContainment assertion;

	public Containment(final AssertsContainment assertion) {
		super(assertion.getKey());
		this.assertion = assertion;
	}

	@Override
	protected void doThat(final PerformContext performContext,
			final Iterable<ObjectAdapter> collection)
			throws ScenarioBoundValueException {

		final ObjectMember nakedObjectMember = performContext
				.getObjectMember();
		final CellBinding thatBinding = performContext.getPeer()
				.getThatItBinding();
		final CellBinding arg0Binding = performContext.getPeer()
				.getArg0Binding();

		if (!arg0Binding.isFound()) {
			throw ScenarioBoundValueException.current(thatBinding,
					"(requires argument)");
		}

		final ScenarioCell arg0Cell = arg0Binding.getCurrentCell();

		final ObjectAdapter containedAdapter = performContext.getPeer()
				.getAdapter(null, nakedObjectMember.getSpecification(),
						arg0Binding, arg0Cell);

		boolean contains = false;
		for (final ObjectAdapter eachAdapter : collection) {
			if (containedAdapter == eachAdapter) {
				contains = true;
				break;
			}
		}

		if (!assertion.isSatisfiedBy(contains)) {
			throw ScenarioBoundValueException.current(arg0Binding, assertion
					.getErrorMsgIfNotSatisfied());
		}

	}

}
