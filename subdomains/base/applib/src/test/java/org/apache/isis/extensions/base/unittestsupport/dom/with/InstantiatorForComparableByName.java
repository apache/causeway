package org.apache.isis.extensions.base.unittestsupport.dom.with;

import org.apache.isis.subdomains.base.applib.with.WithNameComparable;

/**
 *
 * @deprecated - use superclass
 */
@Deprecated
public class InstantiatorForComparableByName extends org.apache.isis.subdomains.base.applib.with.InstantiatorForComparableByName {

    public InstantiatorForComparableByName(Class<? extends WithNameComparable<?>> cls) {
        super(cls);
    }

}