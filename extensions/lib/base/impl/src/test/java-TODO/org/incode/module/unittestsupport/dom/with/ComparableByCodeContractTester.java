package org.incode.module.unittestsupport.dom.with;

import org.incode.module.base.dom.with.WithCodeComparable;

/**
 * @deprecated - use superclass
 */
@Deprecated
public class ComparableByCodeContractTester<T extends WithCodeComparable<T>> extends
        org.incode.module.base.dom.with.ComparableByCodeContractTester<T> {

    public ComparableByCodeContractTester(Class<T> cls) {
        super(cls);
    }

}
