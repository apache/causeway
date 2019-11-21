package org.incode.module.unittestsupport.dom.with;

/**
 *
 * @deprecated - use superclass
 */
@Deprecated
public abstract class WithFieldUniqueContractTestAllAbstract<T> extends
        org.incode.module.base.dom.with.WithFieldUniqueContractTestAllAbstract<T> {

    public WithFieldUniqueContractTestAllAbstract(
            final String prefix,
            final String fieldName,
            final Class<T> interfaceType) {
        super(prefix, fieldName, interfaceType);
    }

}
