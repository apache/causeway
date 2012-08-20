package org.apache.isis.core.metamodel.adapter.oid;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.isis.core.testsupport.value.ValueTypeContractTestAbstract;

public class OidVersionTest_valueSemantics extends ValueTypeContractTestAbstract<Oid.Version> {

    @Override
    protected List<Oid.Version> getObjectsWithSameValue() {
        return Arrays.asList(
                    Oid.Version.create(123L, null, null), 
                    Oid.Version.create(123L, "jimmy", null), 
                    Oid.Version.create(123L, null, new Date().getTime())
                ); 
    }

    @Override
    protected List<Oid.Version> getObjectsWithDifferentValue() {
        return Arrays.asList(
                    Oid.Version.create(124L, null, null), 
                    Oid.Version.create(125L, null, null) 
                );
    }

}
