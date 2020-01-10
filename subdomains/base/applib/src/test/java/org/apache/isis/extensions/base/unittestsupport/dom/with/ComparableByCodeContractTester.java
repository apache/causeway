package org.apache.isis.extensions.base.unittestsupport.dom.with;

import org.apache.isis.extensions.base.dom.with.WithCodeComparable;

/**
 * @deprecated - use superclass
 */
@Deprecated
public class ComparableByCodeContractTester<T extends WithCodeComparable<T>> extends
        org.apache.isis.extensions.base.dom.with.ComparableByCodeContractTester<T> {

    public ComparableByCodeContractTester(Class<T> cls) {
        super(cls);
    }

}
