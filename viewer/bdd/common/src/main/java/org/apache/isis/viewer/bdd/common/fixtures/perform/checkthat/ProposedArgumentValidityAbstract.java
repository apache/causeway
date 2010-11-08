package org.apache.isis.viewer.bdd.common.fixtures.perform.checkthat;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;
import org.apache.isis.viewer.bdd.common.CellBinding;
import org.apache.isis.viewer.bdd.common.StoryBoundValueException;
import org.apache.isis.viewer.bdd.common.StoryCell;
import org.apache.isis.viewer.bdd.common.fixtures.perform.PerformContext;
import org.apache.isis.viewer.bdd.common.util.Strings;

public abstract class ProposedArgumentValidityAbstract extends
		ThatValidityAbstract {

	public ProposedArgumentValidityAbstract(final AssertsValidity assertion) {
		super(assertion);
	}

	public ObjectAdapter that(final PerformContext performContext)
			throws StoryBoundValueException {

		final ObjectMember nakedObjectMember = performContext
				.getNakedObjectMember();
		final CellBinding thatBinding = performContext.getPeer()
				.getThatItBinding();
		final CellBinding arg0Binding = performContext.getPeer()
				.getArg0Binding();

		// check we have an argument to validate (if one is required)
		if (!arg0Binding.isFound()) {
			throw StoryBoundValueException.current(thatBinding,
					"(requires argument)");
		}

		final StoryCell arg0Cell = arg0Binding.getCurrentCell();
		final String toValidate = arg0Cell.getText();
		if (Strings.emptyString(toValidate)) {
			throw StoryBoundValueException.current(arg0Binding, "(required)");
		}

		final ObjectAdapter toValidateAdapter = performContext.getPeer()
				.getAdapter(null, nakedObjectMember.getSpecification(),
						arg0Binding, arg0Cell);
		final Consent validityConsent = determineConsent(performContext,
				toValidateAdapter);
		if (!getAssertion().satisfiedBy(validityConsent)) {
			throw StoryBoundValueException.current(getAssertion().colorBinding(
					arg0Binding, thatBinding), getAssertion().getReason(
					validityConsent));
		}

		return toValidateAdapter;
	}

	protected abstract Consent determineConsent(
			final PerformContext performContext, ObjectAdapter toValidateAdapter);

}
