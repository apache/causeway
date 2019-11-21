package org.incode.module.unittestsupport.dom.with;

import org.incode.module.base.dom.with.WithReferenceComparable;

/**
 *
 * @deprecated - use superclass
 */
@Deprecated
public class ComparableByReferenceContractTester<T extends WithReferenceComparable<T>> extends
        org.incode.module.base.dom.with.ComparableByReferenceContractTester<T> {

    public ComparableByReferenceContractTester(Class<T> cls) {
        super(cls);
    }

}
