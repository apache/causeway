package org.incode.module.base.dom.with;

import java.util.List;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;

import org.apache.isis.core.unittestsupport.comparable.ComparableContractTester;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ComparableByDescriptionContractTester<T extends WithDescriptionComparable<T>> {
    protected final Class<T> cls;

    public ComparableByDescriptionContractTester(Class<T> cls) {
        this.cls = cls;
    }

    public static <E> List<E> listOf(E... elements) {
        return Lists.newArrayList(elements);
    }

    public void test() {
        System.out.println("ComparableByDescriptionContractTester: " + cls.getName());
        new ComparableContractTester<>(orderedTuples()).test();

        testToString();
    }

    protected void testToString() {
        final String str = "ABC";

        final T withDescription = newWithDescription(str);
        String expectedToString = Objects.toStringHelper(withDescription).add("description", "ABC").toString();

        assertThat(withDescription.toString(), is(expectedToString));
    }

    @SuppressWarnings("unchecked")
    protected List<List<T>> orderedTuples() {
        return listOf(
                listOf(
                        newWithDescription(null),
                        newWithDescription("ABC"),
                        newWithDescription("ABC"),
                        newWithDescription("DEF")));
    }

    private T newWithDescription(String reference) {
        final T wr = newWithDescription();
        wr.setDescription(reference);
        return wr;
    }

    private T newWithDescription() {
        try {
            return cls.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
