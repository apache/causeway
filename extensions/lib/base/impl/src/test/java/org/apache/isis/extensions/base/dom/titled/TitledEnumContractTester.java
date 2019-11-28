package org.apache.isis.extensions.base.dom.titled;

import org.hamcrest.CoreMatchers;

import org.apache.isis.applib.util.Enums;

import org.apache.isis.extensions.base.dom.TitledEnum;

import static org.junit.Assert.assertThat;

public class TitledEnumContractTester {
    protected Enum<?>[] enumValues;
    protected Class<? extends Enum<?>> cls;

    public TitledEnumContractTester(Class<? extends Enum<?>> enumType) {
        this.cls = enumType;
        this.enumValues = enumType.getEnumConstants();
    }

    public void test() {
        System.out.println("TitledEnumContractTester: " + cls.getName());

        for (Enum<?> enumValue: enumValues) {
            final TitledEnum titled = (TitledEnum) enumValue;
            final String enumName = enumValue.name();
            assertThat(enumValue.getClass().getName()+"#"+enumName, titled.title(), CoreMatchers.is(enumTitle(enumName)));
        }
    }

    private <T> String enumTitle(final String enumName) {
        return Enums.getFriendlyNameOf(enumName);
    }
}
