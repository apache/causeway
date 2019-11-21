package org.incode.module.unittestsupport.dom.with;

import org.incode.module.base.dom.with.WithDescriptionComparable;

/**
 *
 * @deprecated - use superclass
 */
@Deprecated
public class ComparableByDescriptionContractTester<T extends WithDescriptionComparable<T>> extends
        org.incode.module.base.dom.with.ComparableByDescriptionContractTester<T> {

    public ComparableByDescriptionContractTester(Class<T> cls) {
        super(cls);
    }

}
