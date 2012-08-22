package org.apache.isis.core.metamodel.adapter.oid;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.isis.core.metamodel.adapter.version.Version;
import org.apache.isis.core.testsupport.value.ValueTypeContractTestAbstract;

public class VersionTest_valueSemantics extends ValueTypeContractTestAbstract<Version> {

    @Override
    protected List<Version> getObjectsWithSameValue() {
        return Arrays.asList(
                    Version.create(123L, null, (Long)null), 
                    Version.create(123L, "jimmy", (Long)null), 
                    Version.create(123L, null, new Date().getTime())
                ); 
    }

    @Override
    protected List<Version> getObjectsWithDifferentValue() {
        return Arrays.asList(
                    Version.create(124L, null, (Long)null), 
                    Version.create(125L, null, (Long)null) 
                );
    }

}
