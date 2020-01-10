package org.apache.isis.subdomains.base.applib.with;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableMap;

import org.junit.Test;
import org.reflections.Reflections;


public abstract class ComparableByDescriptionContractTestAbstract_compareTo {
    protected final String packagePrefix;
    protected Map<Class<?>, Class<?>> noninstantiableSubstitutes;

    public ComparableByDescriptionContractTestAbstract_compareTo(
            String packagePrefix, ImmutableMap<Class<?>, Class<?>> noninstantiableSubstitutes) {
        this.packagePrefix = packagePrefix;
        this.noninstantiableSubstitutes = noninstantiableSubstitutes;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void searchAndTest() {
        Reflections reflections = new Reflections(packagePrefix);

        Set<Class<? extends WithDescriptionComparable>> subtypes =
                reflections.getSubTypesOf(WithDescriptionComparable.class);
        for (Class<? extends WithDescriptionComparable> subtype : subtypes) {
            if(subtype.isInterface() || subtype.isAnonymousClass() || subtype.isLocalClass() || subtype.isMemberClass()) {
                // skip (probably a testing class)
                continue;
            }
            subtype = instantiable(subtype);
            test(subtype);
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private Class<? extends WithDescriptionComparable> instantiable(Class<? extends WithDescriptionComparable> cls) {
        final Class<?> substitute = noninstantiableSubstitutes.get(cls);
        return (Class<? extends WithDescriptionComparable>) (substitute!=null?substitute:cls);
    }

    private <T extends WithDescriptionComparable<T>> void test(Class<T> cls) {
        new ComparableByDescriptionContractTester<>(cls).test();
    }
}
