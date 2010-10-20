package org.apache.isis.extensions.bdd.common.fixtures.perform.checkthat.collections;

import org.apache.isis.extensions.bdd.common.CellBinding;
import org.apache.isis.extensions.bdd.common.StoryBoundValueException;
import org.apache.isis.extensions.bdd.common.fixtures.perform.PerformContext;
import org.apache.isis.extensions.bdd.common.fixtures.perform.checkthat.AssertsEmpty;
import org.apache.isis.metamodel.adapter.ObjectAdapter;

public class Emptiness extends ThatAbstract {

	private final AssertsEmpty assertion;

	public Emptiness(final AssertsEmpty assertion) {
		super(assertion.getKey());
		this.assertion = assertion;
	}

	@Override
	protected void doThat(final PerformContext performContext,
			final Iterable<ObjectAdapter> collection)
			throws StoryBoundValueException {

		boolean empty = true;
		for (@SuppressWarnings("unused")
		final ObjectAdapter eachObject : collection) {
			empty = false;
		}

		if (!assertion.isSatisfiedBy(empty)) {
			CellBinding thatItBinding = performContext.getPeer()
					.getThatItBinding();
			throw StoryBoundValueException.current(thatItBinding, assertion
					.getErrorMsgIfNotSatisfied());
		}
	}

}
