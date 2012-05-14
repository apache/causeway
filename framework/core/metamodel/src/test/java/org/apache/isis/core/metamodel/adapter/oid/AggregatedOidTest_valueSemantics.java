package org.apache.isis.core.metamodel.adapter.oid;

import java.util.Arrays;
import java.util.List;

import org.apache.isis.core.metamodel.adapter.oid.Oid.State;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.core.testsupport.value.ValueTypeContractTestAbstract;

public class AggregatedOidTest_valueSemantics extends ValueTypeContractTestAbstract<AggregatedOid> {

    private final RootOidDefault parent = new RootOidDefault(ObjectSpecId.of("CUS"), "123", State.PERSISTENT);
    private final RootOidDefault otherParent = new RootOidDefault(ObjectSpecId.of("CUS"), "124", State.PERSISTENT);
    private final AggregatedOid yetAnotherParent = new AggregatedOid(ObjectSpecId.of("NME"), parent, "789");
    
    @Override
    protected List<AggregatedOid> getObjectsWithSameValue() {
        return Arrays.asList(
                new AggregatedOid(ObjectSpecId.of("NME"), parent, "456"), 
                new AggregatedOid(ObjectSpecId.of("NME"), parent, "456"), 
                new AggregatedOid(ObjectSpecId.of("NME"), parent, "456") 
                );
    }

    @Override
    protected List<AggregatedOid> getObjectsWithDifferentValue() {
        return Arrays.asList(
                new AggregatedOid(ObjectSpecId.of("NME"), otherParent, "456"), 
                new AggregatedOid(ObjectSpecId.of("NMX"), parent, "456"), 
                new AggregatedOid(ObjectSpecId.of("NME"), parent, "457"), 
                new AggregatedOid(ObjectSpecId.of("NME"), yetAnotherParent, "456"));
    }

}
