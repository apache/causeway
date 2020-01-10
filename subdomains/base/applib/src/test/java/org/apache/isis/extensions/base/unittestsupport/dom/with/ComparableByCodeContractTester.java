package org.apache.isis.extensions.base.unittestsupport.dom.with;

import org.apache.isis.subdomains.base.applib.with.WithCodeComparable;

/**
 * @deprecated - use superclass
 */
@Deprecated
public class ComparableByCodeContractTester<T extends WithCodeComparable<T>> extends
        org.apache.isis.subdomains.base.applib.with.ComparableByCodeContractTester<T> {

    public ComparableByCodeContractTester(Class<T> cls) {
        super(cls);
    }

}
