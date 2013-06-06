package org.apache.isis.core.unittestsupport.bidir;

import com.google.common.collect.ImmutableMap;

public class BidirectionalRelationshipContractTestAll extends BidirectionalRelationshipContractTestAbstract {

    public BidirectionalRelationshipContractTestAll() {
        super("org.apache.isis.core.unittestsupport.bidir", 
                ImmutableMap.<Class<?>,Instantiator>of(
                    // no instantiator need be registered for ParentDomainObject.class; 
                    // will default to using new InstantiatorSimple(AgreementForTesting.class),
                    ChildDomainObject.class, new InstantiatorForChildDomainObject(),
                    PeerDomainObject.class, new InstantiatorSimple(PeerDomainObjectForTesting.class)
                ));
        withLoggingTo(System.out);
    }

}
