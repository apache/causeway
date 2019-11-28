package org.apache.isis.extensions.base.unittestsupport.dom.with;

import org.apache.isis.extensions.base.dom.with.WithDescriptionComparable;

/**
 *
 * @deprecated - use superclass
 */
@Deprecated
public class ComparableByDescriptionContractTester<T extends WithDescriptionComparable<T>> extends
        org.apache.isis.extensions.base.dom.with.ComparableByDescriptionContractTester<T> {

    public ComparableByDescriptionContractTester(Class<T> cls) {
        super(cls);
    }

}
