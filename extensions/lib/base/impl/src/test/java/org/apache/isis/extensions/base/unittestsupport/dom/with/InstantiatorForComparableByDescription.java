package org.apache.isis.extensions.base.unittestsupport.dom.with;

import org.apache.isis.extensions.base.dom.with.WithDescriptionComparable;

/**
 *
 * @deprecated - use superclass
 */
@Deprecated
public class InstantiatorForComparableByDescription extends
        org.apache.isis.extensions.base.dom.with.InstantiatorForComparableByDescription {

    public InstantiatorForComparableByDescription(Class<? extends WithDescriptionComparable<?>> cls) {
        super(cls);
    }

}