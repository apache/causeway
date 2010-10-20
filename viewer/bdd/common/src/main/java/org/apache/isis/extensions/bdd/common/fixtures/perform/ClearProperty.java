package org.apache.isis.extensions.bdd.common.fixtures.perform;

import org.apache.isis.extensions.bdd.common.CellBinding;
import org.apache.isis.extensions.bdd.common.StoryBoundValueException;
import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.consent.Consent;
import org.apache.isis.metamodel.facets.properties.modify.PropertyClearFacet;
import org.apache.isis.metamodel.spec.feature.ObjectMember;
import org.apache.isis.metamodel.spec.feature.OneToOneAssociation;

public class ClearProperty extends PerformAbstractTypeParams {

	private ObjectAdapter result;

	public ClearProperty(final Perform.Mode mode) {
		super("clear property", Type.PROPERTY, NumParameters.ZERO, mode);
	}

	@Override
	public void doHandle(final PerformContext performContext)
			throws StoryBoundValueException {

		final ObjectAdapter onAdapter = performContext.getOnAdapter();
		final ObjectMember nakedObjectMember = performContext
				.getNakedObjectMember();

		final OneToOneAssociation otoa = (OneToOneAssociation) nakedObjectMember;

		// set
		final PropertyClearFacet clearFacet = otoa
				.getFacet(PropertyClearFacet.class);
		CellBinding thatItBinding = performContext.getPeer().getThatItBinding();
		if (clearFacet == null) {
			throw StoryBoundValueException
					.current(thatItBinding, "(cannot clear)");
		}

		// validate setting to null
		final Consent validConsent = otoa.isAssociationValid(onAdapter, null);
		if (validConsent.isVetoed()) {
			throw StoryBoundValueException.current(thatItBinding, validConsent
					.getReason());
		}

		clearFacet.clearProperty(onAdapter);

	}

	public ObjectAdapter getResult() {
		return result;
	}

}
