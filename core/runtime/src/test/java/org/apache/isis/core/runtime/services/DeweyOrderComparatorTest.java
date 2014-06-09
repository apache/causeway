package org.apache.isis.core.runtime.services;

import java.util.*;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.CoreMatchers.is;

public class DeweyOrderComparatorTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void emptySet() throws Exception {
        assertThatSorting(
                ofS(),
                ofL());
    }

    @Test
    public void singleElement() throws Exception {
        assertThatSorting(
                ofS("1"),
                ofL("1")
        );
    }

    @Test
    public void inOrder() throws Exception {
        assertThatSorting(
                ofS("1", "2"),
                ofL("1", "2")
        );
    }

    @Test
    public void notInOrder() throws Exception {
        assertThatSorting(
                ofS("2", "1"),
                ofL("1", "2")
        );
    }

    @Test
    public void notInOrderDepth2() throws Exception {
        assertThatSorting(
                ofS("1.2", "1.1"),
                ofL("1.1", "1.2")
        );
    }

    @Test
    public void differentDepths() throws Exception {
        assertThatSorting(
                ofS("2", "1.3", "1.2", "1.2.2", "1.2.1", "1.1"),
                ofL("1.1", "1.2", "1.2.1", "1.2.2", "1.3", "2")
        );
    }

    @Test
    public void mismatchedDepth3() throws Exception {
        assertThatSorting(
                ofS("1.2.2", "1.2.1", "1.1"),
                ofL("1.1", "1.2.1", "1.2.2")
        );
    }

    private static Collection<String> ofS(String... str) {
        return Arrays.asList(str);
    }

    private static List<String> ofL(String... str) {
        return Lists.newArrayList(ofS(str));
    }

    private static void assertThatSorting(Collection<String> input, List<String> expected) {
        final SortedSet<String> treeSet = new TreeSet<String>(new DeweyOrderComparator());
        treeSet.addAll(input);
        final List<String> strings = Arrays.asList(Iterators.toArray(treeSet.iterator(), String.class));
        Assert.assertThat(strings, is(expected));
    }

}
