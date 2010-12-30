package org.apache.isis.viewer.bdd.common.fixtures.perform;

import java.util.List;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.consent2.Consent;
import org.apache.isis.core.metamodel.consent2.InteractionInvocationMethod;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionRemoveFromFacet;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.viewer.bdd.common.CellBinding;
import org.apache.isis.viewer.bdd.common.ScenarioBoundValueException;
import org.apache.isis.viewer.bdd.common.ScenarioCell;

public class RemoveFromCollection extends PerformAbstractTypeParams {

	private ObjectAdapter result;

	public RemoveFromCollection(final Perform.Mode mode) {
		super("remove from collection", Type.COLLECTION, NumParameters.ONE,
				mode);
	}

	@Override
	public void doHandle(final PerformContext performContext)
			throws ScenarioBoundValueException {

		final ObjectAdapter onAdapter = performContext.getOnAdapter();
		final ObjectMember nakedObjectMember = performContext
				.getObjectMember();
		final CellBinding onMemberBinding = performContext.getPeer()
				.getOnMemberBinding();
		final ScenarioCell onMemberCell = onMemberBinding.getCurrentCell();

		final List<ScenarioCell> argumentCells = performContext.getArgumentCells();

		final OneToManyAssociation otma = (OneToManyAssociation) nakedObjectMember;

		// safe since guaranteed by superclass
		CellBinding arg0Binding = performContext.getPeer().getArg0Binding();
		final ScenarioCell arg0Cell = argumentCells.get(0);
		final String toRemove = arg0Cell.getText();

		final CollectionRemoveFromFacet removeFromFacet = nakedObjectMember
				.getFacet(CollectionRemoveFromFacet.class);
		if (removeFromFacet == null) {
			throw ScenarioBoundValueException.current(onMemberBinding,
					"(cannot remove from collection)");
		}

		final ObjectAdapter toRemoveAdapter = performContext.getPeer()
				.getAliasRegistry().getAliased(toRemove);
		if (toRemoveAdapter == null) {
			throw ScenarioBoundValueException.current(arg0Binding, "(unknown alias)");
		}

		// validate argument
		otma
				.createValidateAddInteractionContext(getSession(),
						InteractionInvocationMethod.BY_USER, onAdapter,
						toRemoveAdapter);
		final Consent validToRemove = otma.isValidToRemove(onAdapter,
				toRemoveAdapter);
		if (validToRemove.isVetoed()) {
			throw ScenarioBoundValueException.current(onMemberBinding, validToRemove
					.getReason());
		}

		// remove
		removeFromFacet.remove(onAdapter, toRemoveAdapter);

	}

	public ObjectAdapter getResult() {
		return result;
	}

}
