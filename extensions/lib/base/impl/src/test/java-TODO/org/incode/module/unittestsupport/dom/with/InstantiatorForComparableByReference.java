package org.incode.module.unittestsupport.dom.with;

import org.incode.module.base.dom.with.WithReferenceComparable;

/**
 *
 * @deprecated - use superclass
 */
@Deprecated
public class InstantiatorForComparableByReference extends
        org.incode.module.base.dom.with.InstantiatorForComparableByReference {

    public InstantiatorForComparableByReference(Class<? extends WithReferenceComparable<?>> cls) {
        super(cls);
    }

}