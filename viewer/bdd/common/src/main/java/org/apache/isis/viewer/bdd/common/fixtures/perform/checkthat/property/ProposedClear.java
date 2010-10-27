package org.apache.isis.viewer.bdd.common.fixtures.perform.checkthat.property;

import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.consent.Consent;
import org.apache.isis.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.viewer.bdd.common.CellBinding;
import org.apache.isis.viewer.bdd.common.StoryBoundValueException;
import org.apache.isis.viewer.bdd.common.fixtures.perform.PerformContext;
import org.apache.isis.viewer.bdd.common.fixtures.perform.checkthat.AssertsValidity;
import org.apache.isis.viewer.bdd.common.fixtures.perform.checkthat.ThatValidityAbstract;

public class ProposedClear extends ThatValidityAbstract {

    public ProposedClear(final AssertsValidity assertion) {
        super(assertion);
    }

    public ObjectAdapter that(final PerformContext performContext) throws StoryBoundValueException {

        final ObjectAdapter onAdapter = performContext.getOnAdapter();
        final OneToOneAssociation otoa = (OneToOneAssociation) performContext
                .getNakedObjectMember();

        final Consent validityConsent = otoa
                .isAssociationValid(onAdapter, null);

        if (!getAssertion().satisfiedBy(validityConsent)) {
            final CellBinding thatBinding = performContext.getPeer().getThatItBinding();
            throw StoryBoundValueException.current(thatBinding,
                    getAssertion().getReason(validityConsent));
        }

        // can only return null.
        return null;
    }

}
