package org.apache.isis.viewer.bdd.common.fixtures.perform.checkthat;

import org.apache.isis.core.commons.lang.StringUtils;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;
import org.apache.isis.viewer.bdd.common.CellBinding;
import org.apache.isis.viewer.bdd.common.ScenarioBoundValueException;
import org.apache.isis.viewer.bdd.common.ScenarioCell;
import org.apache.isis.viewer.bdd.common.fixtures.perform.PerformContext;

public abstract class ProposedArgumentValidityAbstract extends
		ThatValidityAbstract {

	public ProposedArgumentValidityAbstract(final AssertsValidity assertion) {
		super(assertion);
	}

	public ObjectAdapter that(final PerformContext performContext)
			throws ScenarioBoundValueException {

		final ObjectMember nakedObjectMember = performContext
				.getObjectMember();
		final CellBinding thatBinding = performContext.getPeer()
				.getThatItBinding();
		final CellBinding arg0Binding = performContext.getPeer()
				.getArg0Binding();

		// check we have an argument to validate (if one is required)
		if (!arg0Binding.isFound()) {
			throw ScenarioBoundValueException.current(thatBinding,
					"(requires argument)");
		}

		final ScenarioCell arg0Cell = arg0Binding.getCurrentCell();
		final String toValidate = arg0Cell.getText();
		if (StringUtils.isNullOrEmpty(toValidate)) {
			throw ScenarioBoundValueException.current(arg0Binding, "(required)");
		}

		final ObjectAdapter toValidateAdapter = performContext.getPeer()
				.getAdapter(null, nakedObjectMember.getSpecification(),
						arg0Binding, arg0Cell);
		final Consent validityConsent = determineConsent(performContext,
				toValidateAdapter);
		if (!getAssertion().satisfiedBy(validityConsent)) {
			throw ScenarioBoundValueException.current(getAssertion().colorBinding(
					arg0Binding, thatBinding), getAssertion().getReason(
					validityConsent));
		}

		return toValidateAdapter;
	}

	protected abstract Consent determineConsent(
			final PerformContext performContext, ObjectAdapter toValidateAdapter);

}
