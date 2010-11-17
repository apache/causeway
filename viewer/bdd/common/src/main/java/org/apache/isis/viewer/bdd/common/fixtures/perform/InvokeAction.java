package org.apache.isis.viewer.bdd.common.fixtures.perform;

import java.util.List;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;
import org.apache.isis.viewer.bdd.common.CellBinding;
import org.apache.isis.viewer.bdd.common.StoryBoundValueException;
import org.apache.isis.viewer.bdd.common.StoryCell;

public class InvokeAction extends PerformAbstractTypeParams {

	private ObjectAdapter result;

	public InvokeAction(final Perform.Mode mode) {
		super("invoke action", Type.ACTION, NumParameters.UNLIMITED, mode);
	}

	@Override
	public void doHandle(final PerformContext performContext)
			throws StoryBoundValueException {

		final ObjectAdapter onAdapter = performContext.getOnAdapter();
		final ObjectMember nakedObjectMember = performContext
				.getObjectMember();
		final CellBinding onMemberBinding = performContext.getPeer()
				.getOnMemberBinding();
		final List<StoryCell> argumentCells = performContext.getArgumentCells();

		final ObjectAction nakedObjectAction = (ObjectAction) nakedObjectMember;
		final int parameterCount = nakedObjectAction.getParameterCount();
		final boolean isContributedOneArgAction = nakedObjectAction
				.isContributed()
				&& parameterCount == 1;

		ObjectAdapter[] proposedArguments;
		if (!isContributedOneArgAction) {

			// lookup arguments
			proposedArguments = performContext.getPeer().getAdapters(onAdapter,
					nakedObjectAction, onMemberBinding, argumentCells);

			// validate arguments
			final Consent argSetValid = nakedObjectAction
					.isProposedArgumentSetValid(onAdapter, proposedArguments);
			if (argSetValid.isVetoed()) {
				throw StoryBoundValueException.current(onMemberBinding, argSetValid
						.getReason());
			}
		} else {
			proposedArguments = new ObjectAdapter[] { onAdapter };
		}

		// execute
		result = nakedObjectAction.execute(onAdapter, proposedArguments);

		// all OK.
	}

	public ObjectAdapter getResult() {
		return result;
	}

}
