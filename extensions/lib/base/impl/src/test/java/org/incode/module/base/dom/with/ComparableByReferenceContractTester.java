package org.incode.module.base.dom.with;

import java.util.List;

import org.apache.isis.unittestsupport.comparable.ComparableContractTester;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;


import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ComparableByReferenceContractTester<T extends WithReferenceComparable<T>> {
    protected final Class<T> cls;

    public ComparableByReferenceContractTester(Class<T> cls) {
        this.cls = cls;
    }

    public static <E> List<E> listOf(E... elements) {
        return Lists.newArrayList(elements);
    }

    public void test() {
        System.out.println("ComparableByReferenceContractTester: " + cls.getName());
        new ComparableContractTester<>(orderedTuples()).test();

        testToString();

    }

    protected void testToString() {
        final String str = "ABC";

        final T withReference = newWithReference(str);
        String expectedToString = MoreObjects.toStringHelper(withReference).add("reference", "ABC").toString();

        assertThat(withReference.toString(), is(expectedToString));
    }

    @SuppressWarnings("unchecked")
    protected List<List<T>> orderedTuples() {
        return listOf(
                listOf(
                        newWithReference(null),
                        newWithReference("ABC"),
                        newWithReference("ABC"),
                        newWithReference("DEF")));
    }

    private T newWithReference(String reference) {
        final T wr = newWithReference();
        wr.setReference(reference);
        return wr;
    }

    private T newWithReference() {
        try {
            return cls.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
