package org.apache.isis.subdomains.base.applib.with;

import java.util.List;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.apache.isis.core.unittestsupport.comparable.ComparableContractTester;

public class ComparableByTitleContractTester<T extends WithTitleComparable<T>> {
    protected final Class<T> cls;

    public ComparableByTitleContractTester(Class<T> cls) {
        this.cls = cls;
    }

    public static <E> List<E> listOf(E... elements) {
        return Lists.newArrayList(elements);
    }

    public void test() {
        System.out.println("ComparableByTitleContractTester: " + cls.getName());
        new ComparableContractTester<>(orderedTuples()).test();

        testToString();

    }

    protected void testToString() {
        final String str = "ABC";

        final T withTitle = newWithTitle(str);
        String expectedToString = MoreObjects.toStringHelper(withTitle).add("title", "ABC").toString();

        assertThat(withTitle.toString(), is(expectedToString));
    }

    @SuppressWarnings("unchecked")
    protected List<List<T>> orderedTuples() {
        return listOf(
                listOf(
                        newWithTitle(null),
                        newWithTitle("ABC"),
                        newWithTitle("ABC"),
                        newWithTitle("DEF")));
    }

    private T newWithTitle(String reference) {
        final T wr = newWithTitle();
        wr.setTitle(reference);
        return wr;
    }

    private T newWithTitle() {
        try {
            return cls.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
