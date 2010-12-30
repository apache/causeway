package org.apache.isis.viewer.bdd.common.fixtures.perform;

import java.util.List;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.consent2.Consent;
import org.apache.isis.core.metamodel.facets.properties.modify.PropertySetterFacet;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.viewer.bdd.common.CellBinding;
import org.apache.isis.viewer.bdd.common.ScenarioBoundValueException;
import org.apache.isis.viewer.bdd.common.ScenarioCell;

public class SetProperty extends PerformAbstractTypeParams {

	private ObjectAdapter result;

	public SetProperty(final Perform.Mode mode) {
		super("set property", Type.PROPERTY, NumParameters.ONE, mode);
	}

	@Override
	public void doHandle(final PerformContext performContext)
			throws ScenarioBoundValueException {

		final ObjectAdapter onAdapter = performContext.getOnAdapter();
		final ObjectMember nakedObjectMember = performContext
				.getObjectMember();
		final List<ScenarioCell> argumentCells = performContext.getArgumentCells();

		final OneToOneAssociation otoa = (OneToOneAssociation) nakedObjectMember;

		// set
		final PropertySetterFacet setterFacet = otoa
				.getFacet(PropertySetterFacet.class);
		if (setterFacet == null) {
			CellBinding onMemberBinding = performContext.getPeer()
					.getOnMemberBinding();
			throw ScenarioBoundValueException.current(onMemberBinding, 
					"(cannot set)");
		}

		// safe to obtain since guaranteed by superclass
		CellBinding arg0Binding = performContext.getPeer().getArg0Binding();
		final ScenarioCell arg0Cell = argumentCells.get(0);

		// obtain existing as 'context' (used if this is a parsed @Value)
		final ObjectAdapter contextAdapter = otoa.get(onAdapter);

		// validate parameter
		final ObjectAdapter toSetAdapter = performContext.getPeer().getAdapter(
				contextAdapter, otoa.getSpecification(), arg0Binding, arg0Cell);
		final Consent validConsent = otoa.isAssociationValid(onAdapter,
				toSetAdapter);
		if (validConsent.isVetoed()) {
			throw ScenarioBoundValueException.current(arg0Binding, validConsent
					.getReason());
		}

		setterFacet.setProperty(onAdapter, toSetAdapter);

	}

	public ObjectAdapter getResult() {
		return result;
	}

}
