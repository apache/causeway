package org.apache.isis.subdomains.base.applib.with;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableMap;

import org.junit.Test;
import org.reflections.Reflections;


public abstract class ComparableByCodeContractTestAbstract_compareTo {
    protected final String packagePrefix;
    protected Map<Class<?>, Class<?>> noninstantiableSubstitutes;

    public ComparableByCodeContractTestAbstract_compareTo(
            String packagePrefix, ImmutableMap<Class<?>, Class<?>> noninstantiableSubstitutes) {
        this.packagePrefix = packagePrefix;
        this.noninstantiableSubstitutes = noninstantiableSubstitutes;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void searchAndTest() {
        Reflections reflections = new Reflections(packagePrefix);

        Set<Class<? extends WithCodeComparable>> subtypes =
                reflections.getSubTypesOf(WithCodeComparable.class);
        for (Class<? extends WithCodeComparable> subtype : subtypes) {
            if(subtype.isInterface() || subtype.isAnonymousClass() || subtype.isLocalClass() || subtype.isMemberClass()) {
                // skip (probably a testing class)
                continue;
            }
            subtype = instantiable(subtype);
            test(subtype);
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private Class<? extends WithCodeComparable> instantiable(Class<? extends WithCodeComparable> cls) {
        final Class<?> substitute = noninstantiableSubstitutes.get(cls);
        return (Class<? extends WithCodeComparable>) (substitute!=null?substitute:cls);
    }

    private <T extends WithCodeComparable<T>> void test(Class<T> cls) {
        new ComparableByCodeContractTester<>(cls).test();
    }
}
