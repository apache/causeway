package org.apache.isis.extensions.base.unittestsupport.dom.with;

import org.apache.isis.subdomains.base.applib.with.WithCodeComparable;

/**
 *
 * @deprecated - use superclass
 */
@Deprecated
public class InstantiatorForComparableByCode extends org.apache.isis.subdomains.base.applib.with.InstantiatorForComparableByCode {

    public InstantiatorForComparableByCode(Class<? extends WithCodeComparable<?>> cls) {
        super(cls);
    }

}