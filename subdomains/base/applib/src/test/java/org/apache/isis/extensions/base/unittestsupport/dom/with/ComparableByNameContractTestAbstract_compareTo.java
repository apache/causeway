package org.apache.isis.extensions.base.unittestsupport.dom.with;

import com.google.common.collect.ImmutableMap;

import org.apache.isis.subdomains.base.applib.with.WithNameComparable;

/**
 * Automatically tests all domain objects implementing {@link WithNameComparable}.
 * 
 * <p>
 * Any that cannot be instantiated are skipped; manually test using
 * {@link ComparableByNameContractTester}.
 *
 * @deprecated - use superclass
 */
@Deprecated
public abstract class ComparableByNameContractTestAbstract_compareTo extends
        org.apache.isis.subdomains.base.applib.with.ComparableByNameContractTestAbstract_compareTo {

    protected ComparableByNameContractTestAbstract_compareTo(
            final String packagePrefix,
            final ImmutableMap<Class<?>, Class<?>> noninstantiableSubstitutes) {
        super(packagePrefix, noninstantiableSubstitutes);
    }

}
