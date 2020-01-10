package org.apache.isis.extensions.base.unittestsupport.dom.with;

import org.apache.isis.extensions.base.dom.with.WithCodeComparable;

/**
 *
 * @deprecated - use superclass
 */
@Deprecated
public class InstantiatorForComparableByCode extends org.apache.isis.extensions.base.dom.with.InstantiatorForComparableByCode {

    public InstantiatorForComparableByCode(Class<? extends WithCodeComparable<?>> cls) {
        super(cls);
    }

}