package org.incode.module.unittestsupport.dom.titled;

import org.incode.module.base.dom.TitledEnum;

/**
 * @deprecated - use superclass
 * @param <T>
 */
@Deprecated
public class TitledEnumContractTester<T extends TitledEnum> extends
        org.incode.module.base.dom.titled.TitledEnumContractTester {

    /**
     * @param enumType
     */
    public TitledEnumContractTester(Class<? extends Enum<?>> enumType) {
        super(enumType);
    }

}
