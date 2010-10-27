package org.apache.isis.viewer.bdd.common.fixtures.perform;

import java.util.List;

import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.consent.Consent;
import org.apache.isis.metamodel.facets.properties.modify.PropertySetterFacet;
import org.apache.isis.metamodel.spec.feature.ObjectMember;
import org.apache.isis.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.viewer.bdd.common.CellBinding;
import org.apache.isis.viewer.bdd.common.StoryBoundValueException;
import org.apache.isis.viewer.bdd.common.StoryCell;

public class SetProperty extends PerformAbstractTypeParams {

	private ObjectAdapter result;

	public SetProperty(final Perform.Mode mode) {
		super("set property", Type.PROPERTY, NumParameters.ONE, mode);
	}

	@Override
	public void doHandle(final PerformContext performContext)
			throws StoryBoundValueException {

		final ObjectAdapter onAdapter = performContext.getOnAdapter();
		final ObjectMember nakedObjectMember = performContext
				.getNakedObjectMember();
		final List<StoryCell> argumentCells = performContext.getArgumentCells();

		final OneToOneAssociation otoa = (OneToOneAssociation) nakedObjectMember;

		// set
		final PropertySetterFacet setterFacet = otoa
				.getFacet(PropertySetterFacet.class);
		if (setterFacet == null) {
			CellBinding onMemberBinding = performContext.getPeer()
					.getOnMemberBinding();
			throw StoryBoundValueException.current(onMemberBinding, 
					"(cannot set)");
		}

		// safe to obtain since guaranteed by superclass
		CellBinding arg0Binding = performContext.getPeer().getArg0Binding();
		final StoryCell arg0Cell = argumentCells.get(0);

		// obtain existing as 'context' (used if this is a parsed @Value)
		final ObjectAdapter contextAdapter = otoa.get(onAdapter);

		// validate parameter
		final ObjectAdapter toSetAdapter = performContext.getPeer().getAdapter(
				contextAdapter, otoa.getSpecification(), arg0Binding, arg0Cell);
		final Consent validConsent = otoa.isAssociationValid(onAdapter,
				toSetAdapter);
		if (validConsent.isVetoed()) {
			throw StoryBoundValueException.current(arg0Binding, validConsent
					.getReason());
		}

		setterFacet.setProperty(onAdapter, toSetAdapter);

	}

	public ObjectAdapter getResult() {
		return result;
	}

}
