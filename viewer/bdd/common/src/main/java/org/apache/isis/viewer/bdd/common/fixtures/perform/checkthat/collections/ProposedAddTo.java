package org.apache.isis.viewer.bdd.common.fixtures.perform.checkthat.collections;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.viewer.bdd.common.fixtures.perform.PerformContext;
import org.apache.isis.viewer.bdd.common.fixtures.perform.checkthat.AssertsValidity;
import org.apache.isis.viewer.bdd.common.fixtures.perform.checkthat.ProposedArgumentValidityAbstract;

public class ProposedAddTo extends ProposedArgumentValidityAbstract {

    public ProposedAddTo(final AssertsValidity assertion) {
        super(assertion);
    }

    @Override
    protected Consent determineConsent(final PerformContext performContext,
            final ObjectAdapter toValidateAdapter) {

        final ObjectAdapter onAdapter = performContext.getOnAdapter();
        final OneToManyAssociation otma = (OneToManyAssociation) performContext
                .getNakedObjectMember();

        return otma.isValidToAdd(onAdapter, toValidateAdapter);
    }

}
