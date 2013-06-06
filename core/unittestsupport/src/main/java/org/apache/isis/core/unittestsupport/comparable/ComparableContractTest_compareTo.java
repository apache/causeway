package org.apache.isis.core.unittestsupport.comparable;

import java.util.List;

import com.google.common.collect.Lists;

import org.junit.Test;

public abstract class ComparableContractTest_compareTo<T extends Comparable<T>> {

    /**
     * Return an array of tuples; each tuple should consist of 4 elements, whereby
     * item0  < item1 = item2 < item3
     * 
     * Typically item0 should be null valued (if supported by the impl).
     */
    protected abstract List<List<T>> orderedTuples();

    @Test
    public void compareAllOrderedTuples() {

        new ComparableContractTester<T>(orderedTuples()).test();
    }

    /**
     * Syntax sugar to remove boilerplate from subclasses.
     */
    protected <E> List<E> listOf(E... elements) {
        return Lists.newArrayList(elements);
    }

}
