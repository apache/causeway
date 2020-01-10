package org.apache.isis.subdomains.base.applib;

/**
 * @deprecated - use superclass
 * @param <T>
 */
@Deprecated
public class TitledEnumContractTester<T extends TitledEnum> extends
        org.apache.isis.subdomains.base.applib.titled.TitledEnumContractTester {

    /**
     * @param enumType
     */
    public TitledEnumContractTester(Class<? extends Enum<?>> enumType) {
        super(enumType);
    }

}
