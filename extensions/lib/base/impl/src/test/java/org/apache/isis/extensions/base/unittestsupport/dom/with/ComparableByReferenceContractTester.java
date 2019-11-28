package org.apache.isis.extensions.base.unittestsupport.dom.with;

import org.apache.isis.extensions.base.dom.with.WithReferenceComparable;

/**
 *
 * @deprecated - use superclass
 */
@Deprecated
public class ComparableByReferenceContractTester<T extends WithReferenceComparable<T>> extends
        org.apache.isis.extensions.base.dom.with.ComparableByReferenceContractTester<T> {

    public ComparableByReferenceContractTester(Class<T> cls) {
        super(cls);
    }

}
