package org.apache.isis.extensions.base.unittestsupport.dom.with;

import org.apache.isis.subdomains.base.applib.with.WithTitleComparable;

/**
 *
 * @deprecated - use superclass
 */
@Deprecated
public class ComparableByTitleContractTester<T extends WithTitleComparable<T>> extends
        org.apache.isis.subdomains.base.applib.with.ComparableByTitleContractTester<T> {

    public ComparableByTitleContractTester(Class<T> cls) {
        super(cls);
    }

}
