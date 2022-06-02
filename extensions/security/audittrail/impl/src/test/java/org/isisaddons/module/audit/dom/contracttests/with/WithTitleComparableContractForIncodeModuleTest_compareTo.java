package org.isisaddons.module.audit.dom.contracttests.with;

import com.google.common.collect.ImmutableMap;

import org.incode.module.base.dom.with.ComparableByTitleContractTestAbstract_compareTo;
import org.incode.module.base.dom.with.WithTitleComparable;

/**
 * Automatically tests all domain objects implementing {@link WithTitleComparable}.
 */
public class WithTitleComparableContractForIncodeModuleTest_compareTo extends
        ComparableByTitleContractTestAbstract_compareTo {

    public WithTitleComparableContractForIncodeModuleTest_compareTo() {
        super("org.isisaddons.module.audit", ImmutableMap.<Class<?>,Class<?>>of());
    }

}
