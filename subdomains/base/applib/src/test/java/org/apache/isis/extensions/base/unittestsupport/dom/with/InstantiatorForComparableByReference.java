package org.apache.isis.extensions.base.unittestsupport.dom.with;

import org.apache.isis.subdomains.base.applib.with.WithReferenceComparable;

/**
 *
 * @deprecated - use superclass
 */
@Deprecated
public class InstantiatorForComparableByReference extends
        org.apache.isis.subdomains.base.applib.with.InstantiatorForComparableByReference {

    public InstantiatorForComparableByReference(Class<? extends WithReferenceComparable<?>> cls) {
        super(cls);
    }

}