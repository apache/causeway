package org.apache.isis.viewer.bdd.common.fixtures.perform.checkthat.action;

import java.util.List;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;
import org.apache.isis.viewer.bdd.common.CellBinding;
import org.apache.isis.viewer.bdd.common.StoryBoundValueException;
import org.apache.isis.viewer.bdd.common.StoryCell;
import org.apache.isis.viewer.bdd.common.fixtures.perform.PerformContext;
import org.apache.isis.viewer.bdd.common.fixtures.perform.checkthat.ThatSubcommandAbstract;

public class ArgumentSetValid extends ThatSubcommandAbstract {

    public ArgumentSetValid() {
        super("is valid for", "is valid", "valid");
    }

    // TODO: a lot of duplication with InvokeAction; simplify somehow?
    public ObjectAdapter that(final PerformContext performContext) throws StoryBoundValueException {

        final ObjectAdapter onAdapter = performContext.getOnAdapter();
        final ObjectMember nakedObjectMember = performContext
                .getObjectMember();
        final CellBinding onMemberBinding = performContext
        .getPeer().getOnMemberBinding();
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
        final ObjectAdapter[] proposedArguments = performContext.getPeer().getAdapters(
        		onAdapter, nakedObjectAction, onMemberBinding, argumentCells);

        // validate arguments
        final Consent argSetValid = nakedObjectAction
                .isProposedArgumentSetValid(onAdapter, proposedArguments);
        if (argSetValid.isVetoed()) {
        	throw StoryBoundValueException.current(onMemberBinding, argSetValid.getReason());
        }

        // execute
        return null;
    }

}
