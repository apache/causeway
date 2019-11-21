package org.incode.module.unittestsupport.dom.with;

import com.google.common.collect.ImmutableMap;

import org.incode.module.base.dom.with.WithNameComparable;

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
        org.incode.module.base.dom.with.ComparableByNameContractTestAbstract_compareTo {

    protected ComparableByNameContractTestAbstract_compareTo(
            final String packagePrefix,
            final ImmutableMap<Class<?>, Class<?>> noninstantiableSubstitutes) {
        super(packagePrefix, noninstantiableSubstitutes);
    }

}
