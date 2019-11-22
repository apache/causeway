package org.incode.module.unittestsupport.dom.with;

import com.google.common.collect.ImmutableMap;

import org.incode.module.base.dom.with.WithDescriptionComparable;

/**
 * Automatically tests all domain objects implementing {@link WithDescriptionComparable}.
 * 
 * <p>
 * Any that cannot be instantiated are skipped; manually test using
 * {@link ComparableByDescriptionContractTester}.
 *
 * @deprecated - use superclass
 */
@Deprecated
public abstract class ComparableByDescriptionContractTestAbstract_compareTo extends
        org.incode.module.base.dom.with.ComparableByDescriptionContractTestAbstract_compareTo {

    protected ComparableByDescriptionContractTestAbstract_compareTo(
            final String packagePrefix,
            final ImmutableMap<Class<?>, Class<?>> noninstantiableSubstitutes) {
        super(packagePrefix, noninstantiableSubstitutes);
    }

}
