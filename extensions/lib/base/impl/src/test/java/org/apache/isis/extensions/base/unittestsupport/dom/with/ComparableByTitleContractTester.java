package org.apache.isis.extensions.base.unittestsupport.dom.with;

import org.apache.isis.extensions.base.dom.with.WithTitleComparable;

/**
 *
 * @deprecated - use superclass
 */
@Deprecated
public class ComparableByTitleContractTester<T extends WithTitleComparable<T>> extends
        org.apache.isis.extensions.base.dom.with.ComparableByTitleContractTester<T> {

    public ComparableByTitleContractTester(Class<T> cls) {
        super(cls);
    }

}
