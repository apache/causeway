package org.apache.isis.extensions.base.unittestsupport.dom.with;

/**
 *
 * @deprecated - use superclass
 */
@Deprecated
public abstract class WithFieldUniqueContractTestAllAbstract<T> extends
        org.apache.isis.subdomains.base.applib.with.WithFieldUniqueContractTestAllAbstract<T> {

    public WithFieldUniqueContractTestAllAbstract(
            final String prefix,
            final String fieldName,
            final Class<T> interfaceType) {
        super(prefix, fieldName, interfaceType);
    }

}
