package org.apache.isis.viewer.bdd.common.fixtures.perform.checkthat.collections;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.viewer.bdd.common.CellBinding;
import org.apache.isis.viewer.bdd.common.ScenarioBoundValueException;
import org.apache.isis.viewer.bdd.common.fixtures.perform.PerformContext;
import org.apache.isis.viewer.bdd.common.fixtures.perform.checkthat.AssertsEmpty;

public class Emptiness extends ThatAbstract {

	private final AssertsEmpty assertion;

	public Emptiness(final AssertsEmpty assertion) {
		super(assertion.getKey());
		this.assertion = assertion;
	}

	@Override
	protected void doThat(final PerformContext performContext,
			final Iterable<ObjectAdapter> collection)
			throws ScenarioBoundValueException {

		boolean empty = true;
		for (@SuppressWarnings("unused")
		final ObjectAdapter eachObject : collection) {
			empty = false;
		}

		if (!assertion.isSatisfiedBy(empty)) {
			CellBinding thatItBinding = performContext.getPeer()
					.getThatItBinding();
			throw ScenarioBoundValueException.current(thatItBinding, assertion
					.getErrorMsgIfNotSatisfied());
		}
	}

}
