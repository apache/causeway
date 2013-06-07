package org.apache.isis.core.unittestsupport.comparable;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;

import com.google.common.collect.Lists;

import org.hamcrest.Matchers;

public class ComparableContractTester<T extends Comparable<T>> {

    
    private final List<List<T>> orderedTuples;

    /**
     * Provide an array of tuples; each tuple should consist of 4 elements, whereby
     * item0  < item1 = item2 < item3
     * 
     * Typically item0 should be null valued (if supported by the impl).
     */
    public ComparableContractTester(List<List<T>> orderedTuples) {
        this.orderedTuples = orderedTuples;
    }
    
    public void test() {

        for(List<T> orderedTuple: orderedTuples) {

            T item1 = orderedTuple.get(0);
            T item2 = orderedTuple.get(1);
            T item3 = orderedTuple.get(2);
            T item4 = orderedTuple.get(3);

            assertThat(desc(item1, "<", item2), item1.compareTo(item2), is(Matchers.lessThan(0)));
            assertThat(desc(item2, ">", item1), item2.compareTo(item1), is(Matchers.greaterThan(0)));
            
            assertThat(desc(item2, "==", item3), item2.compareTo(item3), is(0));
            assertThat(desc(item3, "==", item2), item3.compareTo(item2), is(0));
            
            assertThat(desc(item3, "<", item4), item3.compareTo(item4), is(Matchers.lessThan(0)));
            assertThat(desc(item4, ">", item3), item4.compareTo(item3), is(Matchers.greaterThan(0)));
        }
    }

    protected static String desc(Object item1, final String op, Object item2) {
        return nullSafe(item1) + op + nullSafe(item2);
    }

    private static String nullSafe(Object item) {
        return item != null? item.toString(): "null";
    }

    /**
     * Syntax sugar to remove boilerplate from subclasses.
     */
    public static <E> List<E> listOf(E... elements) {
        return Lists.newArrayList(elements);
    }


}
