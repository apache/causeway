package org.apache.isis.viewer.bdd.common.fixtures.perform;

import java.util.List;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.consent.InteractionInvocationMethod;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.metamodel.facets.collections.modify.CollectionRemoveFromFacet;
import org.apache.isis.viewer.bdd.common.CellBinding;
import org.apache.isis.viewer.bdd.common.StoryBoundValueException;
import org.apache.isis.viewer.bdd.common.StoryCell;

public class RemoveFromCollection extends PerformAbstractTypeParams {

	private ObjectAdapter result;

	public RemoveFromCollection(final Perform.Mode mode) {
		super("remove from collection", Type.COLLECTION, NumParameters.ONE,
				mode);
	}

	@Override
	public void doHandle(final PerformContext performContext)
			throws StoryBoundValueException {

		final ObjectAdapter onAdapter = performContext.getOnAdapter();
		final ObjectMember nakedObjectMember = performContext
				.getNakedObjectMember();
		final CellBinding onMemberBinding = performContext.getPeer()
				.getOnMemberBinding();
		final StoryCell onMemberCell = onMemberBinding.getCurrentCell();

		final List<StoryCell> argumentCells = performContext.getArgumentCells();

		final OneToManyAssociation otma = (OneToManyAssociation) nakedObjectMember;

		// safe since guaranteed by superclass
		CellBinding arg0Binding = performContext.getPeer().getArg0Binding();
		final StoryCell arg0Cell = argumentCells.get(0);
		final String toRemove = arg0Cell.getText();

		final CollectionRemoveFromFacet removeFromFacet = nakedObjectMember
				.getFacet(CollectionRemoveFromFacet.class);
		if (removeFromFacet == null) {
			throw StoryBoundValueException.current(onMemberBinding,
					"(cannot remove from collection)");
		}

		final ObjectAdapter toRemoveAdapter = performContext.getPeer()
				.getAliasRegistry().getAliased(toRemove);
		if (toRemoveAdapter == null) {
			throw StoryBoundValueException.current(arg0Binding, "(unknown alias)");
		}

		// validate argument
		otma
				.createValidateAddInteractionContext(getSession(),
						InteractionInvocationMethod.BY_USER, onAdapter,
						toRemoveAdapter);
		final Consent validToRemove = otma.isValidToRemove(onAdapter,
				toRemoveAdapter);
		if (validToRemove.isVetoed()) {
			throw StoryBoundValueException.current(onMemberBinding, validToRemove
					.getReason());
		}

		// remove
		removeFromFacet.remove(onAdapter, toRemoveAdapter);

	}

	public ObjectAdapter getResult() {
		return result;
	}

}
