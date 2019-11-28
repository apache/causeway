package org.apache.isis.extensions.base.dom.with;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableMap;

import org.junit.Test;
import org.reflections.Reflections;

import org.apache.isis.extensions.base.unittestsupport.dom.with.ComparableByReferenceContractTester;

public abstract class ComparableByReferenceContractTestAbstract_compareTo {
    protected final String packagePrefix;
    protected Map<Class<?>, Class<?>> noninstantiableSubstitutes;

    public ComparableByReferenceContractTestAbstract_compareTo(
            String packagePrefix, ImmutableMap<Class<?>, Class<?>> noninstantiableSubstitutes) {
        this.packagePrefix = packagePrefix;
        this.noninstantiableSubstitutes = noninstantiableSubstitutes;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void searchAndTest() {
        Reflections reflections = new Reflections(packagePrefix);

        Set<Class<? extends WithReferenceComparable>> subtypes =
                reflections.getSubTypesOf(WithReferenceComparable.class);
        for (Class<? extends WithReferenceComparable> subtype : subtypes) {
            if(subtype.isInterface() || subtype.isAnonymousClass() || subtype.isLocalClass() || subtype.isMemberClass()) {
                // skip (probably a testing class)
                continue;
            }
            subtype = instantiable(subtype);
            test(subtype);
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private Class<? extends WithReferenceComparable> instantiable(Class<? extends WithReferenceComparable> cls) {
        final Class<?> substitute = noninstantiableSubstitutes.get(cls);
        return (Class<? extends WithReferenceComparable>) (substitute!=null?substitute:cls);
    }

    private <T extends WithReferenceComparable<T>> void test(Class<T> cls) {
        new ComparableByReferenceContractTester<>(cls).test();
    }
}
