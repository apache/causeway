package org.apache.isis.viewer.bdd.common.fixtures.perform.checkthat.action;

import java.util.List;

import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.consent.Consent;
import org.apache.isis.metamodel.spec.feature.ObjectAction;
import org.apache.isis.metamodel.spec.feature.ObjectMember;
import org.apache.isis.viewer.bdd.common.CellBinding;
import org.apache.isis.viewer.bdd.common.StoryBoundValueException;
import org.apache.isis.viewer.bdd.common.StoryCell;
import org.apache.isis.viewer.bdd.common.fixtures.perform.PerformContext;
import org.apache.isis.viewer.bdd.common.fixtures.perform.checkthat.ThatSubcommandAbstract;

public class ArgumentSetNotValid extends ThatSubcommandAbstract {

	public ArgumentSetNotValid() {
		super("is not valid for", "is invalid", "invalid");
	}

	// TODO: a lot of duplication with InvokeAction; simplify somehow?
	public ObjectAdapter that(final PerformContext performContext)
			throws StoryBoundValueException {

		final ObjectAdapter onAdapter = performContext.getOnAdapter();
		final ObjectMember nakedObjectMember = performContext
				.getNakedObjectMember();
		final CellBinding onMemberBinding = performContext.getPeer()
				.getOnMemberBinding();
		final List<StoryCell> argumentCells = performContext.getArgumentCells();

		final ObjectAction nakedObjectAction = (ObjectAction) nakedObjectMember;
		final int parameterCount = nakedObjectAction.getParameterCount();
		final boolean isContributedOneArgAction = nakedObjectAction
				.isContributed()
				&& parameterCount == 1;

		if (isContributedOneArgAction) {
			return null;
		}

		// lookup arguments
		final ObjectAdapter[] proposedArguments = performContext.getPeer()
				.getAdapters(onAdapter, nakedObjectAction, onMemberBinding, argumentCells);

		// validate arguments
		final Consent argSetValid = nakedObjectAction
				.isProposedArgumentSetValid(onAdapter, proposedArguments);
		if (argSetValid.isAllowed()) {
			CellBinding thatItBinding = performContext.getPeer()
					.getThatItBinding();
			throw StoryBoundValueException.current(thatItBinding, "(valid)");
		}

		// execute
		return null;
	}

}
