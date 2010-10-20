package org.apache.isis.extensions.bdd.common.fixtures.perform.checkthat.collections;

import org.apache.isis.extensions.bdd.common.CellBinding;
import org.apache.isis.extensions.bdd.common.StoryBoundValueException;
import org.apache.isis.extensions.bdd.common.StoryCell;
import org.apache.isis.extensions.bdd.common.fixtures.perform.PerformContext;
import org.apache.isis.extensions.bdd.common.fixtures.perform.checkthat.AssertsContainment;
import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.spec.feature.ObjectMember;

public class Containment extends ThatAbstract {

	private final AssertsContainment assertion;

	public Containment(final AssertsContainment assertion) {
		super(assertion.getKey());
		this.assertion = assertion;
	}

	@Override
	protected void doThat(final PerformContext performContext,
			final Iterable<ObjectAdapter> collection)
			throws StoryBoundValueException {

		final ObjectMember nakedObjectMember = performContext
				.getNakedObjectMember();
		final CellBinding thatBinding = performContext.getPeer()
				.getThatItBinding();
		final CellBinding arg0Binding = performContext.getPeer()
				.getArg0Binding();

		if (!arg0Binding.isFound()) {
			throw StoryBoundValueException.current(thatBinding,
					"(requires argument)");
		}

		final StoryCell arg0Cell = arg0Binding.getCurrentCell();

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
			throw StoryBoundValueException.current(arg0Binding, assertion
					.getErrorMsgIfNotSatisfied());
		}

	}

}
