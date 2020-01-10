package org.apache.isis.extensions.base.unittestsupport.dom.with;

import org.apache.isis.subdomains.base.applib.with.WithDescriptionComparable;

/**
 *
 * @deprecated - use superclass
 */
@Deprecated
public class ComparableByDescriptionContractTester<T extends WithDescriptionComparable<T>> extends
        org.apache.isis.subdomains.base.applib.with.ComparableByDescriptionContractTester<T> {

    public ComparableByDescriptionContractTester(Class<T> cls) {
        super(cls);
    }

}
