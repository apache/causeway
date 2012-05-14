package org.apache.isis.core.metamodel.adapter.oid;

import java.util.Arrays;
import java.util.List;

import org.apache.isis.core.metamodel.adapter.oid.Oid.State;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.core.testsupport.value.ValueTypeContractTestAbstract;

public class CollectionOidTest_valueSemantics extends ValueTypeContractTestAbstract<CollectionOid> {

    private final RootOidDefault parent = new RootOidDefault(ObjectSpecId.of("CUS"), "123", State.PERSISTENT);
    private final RootOidDefault otherParent = new RootOidDefault(ObjectSpecId.of("CUS"), "124", State.PERSISTENT);
    private final AggregatedOid yetAnotherParent = new AggregatedOid(ObjectSpecId.of("NME"), parent, "789");
    
    @Override
    protected List<CollectionOid> getObjectsWithSameValue() {
        return Arrays.asList(
                new CollectionOid(parent, "456"), 
                new CollectionOid(parent, "456"), 
                new CollectionOid(parent, "456"));
    }

    @Override
    protected List<CollectionOid> getObjectsWithDifferentValue() {
        return Arrays.asList(
                new CollectionOid(otherParent, "456"), 
                new CollectionOid(parent, "457"), 
                new CollectionOid(yetAnotherParent, "456"));
    }

}
