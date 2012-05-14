package org.apache.isis.core.metamodel.adapter.oid;

import java.util.Arrays;
import java.util.List;

import org.apache.isis.core.metamodel.adapter.oid.Oid.State;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.core.testsupport.value.ValueTypeContractTestAbstract;

public class RootOidDefaultTest_valueSemantics_whenPersistent extends ValueTypeContractTestAbstract<RootOidDefault> {

    @Override
    protected List<RootOidDefault> getObjectsWithSameValue() {
        return Arrays.asList(
                new RootOidDefault(ObjectSpecId.of("CUS"), "123", State.PERSISTENT), 
                new RootOidDefault(ObjectSpecId.of("CUS"), "123", State.PERSISTENT), 
                new RootOidDefault(ObjectSpecId.of("CUS"), "123", State.PERSISTENT));
    }

    @Override
    protected List<RootOidDefault> getObjectsWithDifferentValue() {
        return Arrays.asList(
                new RootOidDefault(ObjectSpecId.of("CUS"), "123", State.TRANSIENT), 
                new RootOidDefault(ObjectSpecId.of("CUS"), "124", State.PERSISTENT), 
                new RootOidDefault(ObjectSpecId.of("CUX"), "123", State.PERSISTENT));
    }

}
