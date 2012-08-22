package org.apache.isis.core.metamodel.adapter.oid;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.isis.core.testsupport.value.ValueTypeContractTestAbstract;

public class OidVersionTest_valueSemantics extends ValueTypeContractTestAbstract<OidVersion> {

    @Override
    protected List<OidVersion> getObjectsWithSameValue() {
        return Arrays.asList(
                    OidVersion.create(123L, null, (Long)null), 
                    OidVersion.create(123L, "jimmy", (Long)null), 
                    OidVersion.create(123L, null, new Date().getTime())
                ); 
    }

    @Override
    protected List<OidVersion> getObjectsWithDifferentValue() {
        return Arrays.asList(
                    OidVersion.create(124L, null, (Long)null), 
                    OidVersion.create(125L, null, (Long)null) 
                );
    }

}
