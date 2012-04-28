package org.apache.isis.core.metamodel.adapter.oid;

import java.util.Arrays;
import java.util.List;

import org.apache.isis.core.metamodel.adapter.oid.Oid.State;
import org.apache.isis.core.testsupport.value.ValueTypeContractTestAbstract;

public class AggregatedOidTest_valueSemantics extends ValueTypeContractTestAbstract<AggregatedOid> {

    private final RootOidDefault parent = new RootOidDefault("CUS", "123", State.PERSISTENT);
    private final RootOidDefault otherParent = new RootOidDefault("CUS", "124", State.PERSISTENT);
    private final AggregatedOid yetAnotherParent = new AggregatedOid(parent, "789");
    
    @Override
    protected List<AggregatedOid> getObjectsWithSameValue() {
        return Arrays.asList(new AggregatedOid(parent, "456"), new AggregatedOid(parent, "456"), new AggregatedOid(parent, "456"));
    }

    @Override
    protected List<AggregatedOid> getObjectsWithDifferentValue() {
        return Arrays.asList(new AggregatedOid(otherParent, "456"), new AggregatedOid(parent, "457"), new AggregatedOid(yetAnotherParent, "456"));
    }

}
