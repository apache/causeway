package org.apache.isis.extensions.base.unittestsupport.dom.with;

import org.apache.isis.subdomains.base.applib.with.WithNameComparable;

/**
 *
 * @deprecated - use superclass
 */
@Deprecated
public class ComparableByNameContractTester<T extends WithNameComparable<T>> extends
        org.apache.isis.subdomains.base.applib.with.ComparableByNameContractTester<T> {

    public ComparableByNameContractTester(Class<T> cls) {
        super(cls);
    }

}
