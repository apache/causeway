package org.apache.isis.extensions.base.unittestsupport.dom.with;

import org.apache.isis.subdomains.base.applib.with.WithDescriptionComparable;

/**
 *
 * @deprecated - use superclass
 */
@Deprecated
public class InstantiatorForComparableByDescription extends
        org.apache.isis.subdomains.base.applib.with.InstantiatorForComparableByDescription {

    public InstantiatorForComparableByDescription(Class<? extends WithDescriptionComparable<?>> cls) {
        super(cls);
    }

}