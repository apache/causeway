package org.incode.module.base.dom.with;

import java.util.List;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;

import org.apache.isis.core.unittestsupport.comparable.ComparableContractTester;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ComparableByNameContractTester<T extends WithNameComparable<T>> {
    protected final Class<T> cls;

    public ComparableByNameContractTester(Class<T> cls) {
        this.cls = cls;
    }

    public static <E> List<E> listOf(E... elements) {
        return Lists.newArrayList(elements);
    }

    public void test() {
        System.out.println("ComparableByNameContractTester: " + cls.getName());
        new ComparableContractTester<>(orderedTuples()).test();

        testToString();

    }

    protected void testToString() {
        final String str = "ABC";

        final T withName = newWithName(str);
        String expectedToString = Objects.toStringHelper(withName).add("name", "ABC").toString();

        assertThat(withName.toString(), is(expectedToString));
    }

    @SuppressWarnings("unchecked")
    protected List<List<T>> orderedTuples() {
        return listOf(
                listOf(
                        newWithName(null),
                        newWithName("ABC"),
                        newWithName("ABC"),
                        newWithName("DEF")));
    }

    private T newWithName(String reference) {
        final T wr = newWithName();
        wr.setName(reference);
        return wr;
    }

    private T newWithName() {
        try {
            return cls.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
