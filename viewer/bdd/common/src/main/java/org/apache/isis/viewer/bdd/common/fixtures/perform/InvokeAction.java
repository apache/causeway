package org.apache.isis.viewer.bdd.common.fixtures.perform;

import java.util.List;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.consent2.Consent;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;
import org.apache.isis.viewer.bdd.common.CellBinding;
import org.apache.isis.viewer.bdd.common.ScenarioBoundValueException;
import org.apache.isis.viewer.bdd.common.ScenarioCell;

public class InvokeAction extends PerformAbstractTypeParams {

	private ObjectAdapter result;

	public InvokeAction(final Perform.Mode mode) {
		super("invoke action", Type.ACTION, NumParameters.UNLIMITED, mode);
	}

	@Override
	public void doHandle(final PerformContext performContext)
			throws ScenarioBoundValueException {

	    final ObjectAdapter onAdapter = performContext.getOnAdapter();
		final ObjectMember objectMember = performContext
				.getObjectMember();
		final CellBinding onMemberBinding = performContext.getPeer()
				.getOnMemberBinding();
		final List<ScenarioCell> argumentCells = performContext.getArgumentCells();

		final ObjectAction objectAction = (ObjectAction) objectMember;
		

		final int parameterCount = objectAction.getParameterCount();
		final boolean isContributedOneArgAction = objectAction
				.isContributed()
				&& parameterCount == 1;

		ObjectAdapter[] proposedArguments;
		if (!isContributedOneArgAction) {

			// lookup arguments
			proposedArguments = performContext.getPeer().getAdapters(onAdapter,
					objectAction, onMemberBinding, argumentCells);

			// validate arguments
			final Consent argSetValid = objectAction
					.isProposedArgumentSetValid(onAdapter, proposedArguments);
			if (argSetValid.isVetoed()) {
				throw ScenarioBoundValueException.current(onMemberBinding, argSetValid
						.getReason());
			}
		} else {
			proposedArguments = new ObjectAdapter[] { onAdapter };
		}

		// execute
		result = objectAction.execute(onAdapter, proposedArguments);

		// all OK.
	}

	@Override
    public ObjectAdapter getResult() {
		return result;
	}

}
