package org.apache.isis.extensions.bdd.common.fixtures.perform.checkthat.property;

import org.apache.isis.extensions.bdd.common.fixtures.perform.PerformContext;
import org.apache.isis.extensions.bdd.common.fixtures.perform.checkthat.AssertsValidity;
import org.apache.isis.extensions.bdd.common.fixtures.perform.checkthat.ProposedArgumentValidityAbstract;
import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.consent.Consent;
import org.apache.isis.metamodel.spec.feature.OneToOneAssociation;

public class ProposedSet extends ProposedArgumentValidityAbstract {

    public ProposedSet(final AssertsValidity assertion) {
        super(assertion);
    }

    @Override
    protected Consent determineConsent(final PerformContext performContext,
            final ObjectAdapter toValidateAdapter) {

        final ObjectAdapter onAdapter = performContext.getOnAdapter();
        final OneToOneAssociation otoa = (OneToOneAssociation) performContext
                .getNakedObjectMember();

        return otoa.isAssociationValid(onAdapter, toValidateAdapter);
    }

}
