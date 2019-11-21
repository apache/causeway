package org.incode.module.unittestsupport.dom.with;

import org.incode.module.base.dom.with.WithNameComparable;

/**
 *
 * @deprecated - use superclass
 */
@Deprecated
public class ComparableByNameContractTester<T extends WithNameComparable<T>> extends
        org.incode.module.base.dom.with.ComparableByNameContractTester<T> {

    public ComparableByNameContractTester(Class<T> cls) {
        super(cls);
    }

}
