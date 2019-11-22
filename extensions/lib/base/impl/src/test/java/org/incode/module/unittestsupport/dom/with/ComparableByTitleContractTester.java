package org.incode.module.unittestsupport.dom.with;

import org.incode.module.base.dom.with.WithTitleComparable;

/**
 *
 * @deprecated - use superclass
 */
@Deprecated
public class ComparableByTitleContractTester<T extends WithTitleComparable<T>> extends
        org.incode.module.base.dom.with.ComparableByTitleContractTester<T> {

    public ComparableByTitleContractTester(Class<T> cls) {
        super(cls);
    }

}
