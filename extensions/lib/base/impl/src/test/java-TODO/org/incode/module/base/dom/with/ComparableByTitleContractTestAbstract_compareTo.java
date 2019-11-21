package org.incode.module.base.dom.with;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableMap;

import org.junit.Test;
import org.reflections.Reflections;

import org.incode.module.unittestsupport.dom.with.ComparableByTitleContractTester;

public class ComparableByTitleContractTestAbstract_compareTo {
    protected final String packagePrefix;
    protected Map<Class<?>, Class<?>> noninstantiableSubstitutes;

    public ComparableByTitleContractTestAbstract_compareTo(
            String packagePrefix, ImmutableMap<Class<?>, Class<?>> noninstantiableSubstitutes) {
        this.packagePrefix = packagePrefix;
        this.noninstantiableSubstitutes = noninstantiableSubstitutes;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void searchAndTest() {
        Reflections reflections = new Reflections(packagePrefix);

        Set<Class<? extends WithTitleComparable>> subtypes =
                reflections.getSubTypesOf(WithTitleComparable.class);
        for (Class<? extends WithTitleComparable> subtype : subtypes) {
            if(subtype.isInterface() || subtype.isAnonymousClass() || subtype.isLocalClass() || subtype.isMemberClass()) {
                // skip (probably a testing class)
                continue;
            }
            subtype = instantiable(subtype);
            test(subtype);
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private Class<? extends WithTitleComparable> instantiable(Class<? extends WithTitleComparable> cls) {
        final Class<?> substitute = noninstantiableSubstitutes.get(cls);
        return (Class<? extends WithTitleComparable>) (substitute!=null?substitute:cls);
    }

    private <T extends WithTitleComparable<T>> void test(Class<T> cls) {
        new ComparableByTitleContractTester<>(cls).test();
    }
}
