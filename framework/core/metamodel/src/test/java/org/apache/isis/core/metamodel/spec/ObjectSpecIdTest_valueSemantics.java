package org.apache.isis.core.metamodel.spec;

import java.util.Arrays;
import java.util.List;

import org.apache.isis.core.testsupport.value.ValueTypeContractTestAbstract;

public class ObjectSpecIdTest_valueSemantics extends ValueTypeContractTestAbstract<ObjectSpecId> {

    @Override
    protected List<ObjectSpecId> getObjectsWithSameValue() {
        return Arrays.asList(new ObjectSpecId("CUS"), new ObjectSpecId("CUS"), new ObjectSpecId("CUS"));
    }

    @Override
    protected List<ObjectSpecId> getObjectsWithDifferentValue() {
        return Arrays.asList(new ObjectSpecId("bUS"), new ObjectSpecId("CUt"));
    }

}
