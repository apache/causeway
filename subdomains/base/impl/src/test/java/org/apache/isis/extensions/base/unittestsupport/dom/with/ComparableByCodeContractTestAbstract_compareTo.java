package org.apache.isis.extensions.base.unittestsupport.dom.with;

import com.google.common.collect.ImmutableMap;

import org.apache.isis.extensions.base.dom.with.WithCodeComparable;

/**
 * Automatically tests all domain objects implementing {@link WithCodeComparable}.
 * 
 * <p>
 * Any that cannot be instantiated are skipped; manually test using
 * {@link ComparableByCodeContractTester}.
 *
 * @deprecated - use superclass
 */
@Deprecated
public abstract class ComparableByCodeContractTestAbstract_compareTo extends
        org.apache.isis.extensions.base.dom.with.ComparableByCodeContractTestAbstract_compareTo {

    protected ComparableByCodeContractTestAbstract_compareTo(
            final String packagePrefix,
            final ImmutableMap<Class<?>, Class<?>> noninstantiableSubstitutes) {
        super(packagePrefix, noninstantiableSubstitutes);
    }

}
