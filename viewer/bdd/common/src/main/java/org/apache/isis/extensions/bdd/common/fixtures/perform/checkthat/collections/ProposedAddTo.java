package org.apache.isis.extensions.bdd.common.fixtures.perform.checkthat.collections;

import org.apache.isis.extensions.bdd.common.fixtures.perform.PerformContext;
import org.apache.isis.extensions.bdd.common.fixtures.perform.checkthat.AssertsValidity;
import org.apache.isis.extensions.bdd.common.fixtures.perform.checkthat.ProposedArgumentValidityAbstract;
import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.consent.Consent;
import org.apache.isis.metamodel.spec.feature.OneToManyAssociation;

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
