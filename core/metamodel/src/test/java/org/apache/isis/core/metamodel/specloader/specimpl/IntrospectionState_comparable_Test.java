package org.apache.isis.core.metamodel.specloader.specimpl;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import static org.apache.isis.core.metamodel.specloader.specimpl.IntrospectionState.MEMBERS_BEING_INTROSPECTED;
import static org.apache.isis.core.metamodel.specloader.specimpl.IntrospectionState.TYPE_AND_MEMBERS_INTROSPECTED;
import static org.apache.isis.core.metamodel.specloader.specimpl.IntrospectionState.NOT_INTROSPECTED;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class IntrospectionState_comparable_Test {

    @Test
    public void all_of_em() throws Exception {
        assertComparison(NOT_INTROSPECTED, NOT_INTROSPECTED, equal());
        assertComparison(NOT_INTROSPECTED, MEMBERS_BEING_INTROSPECTED, negative());
        assertComparison(NOT_INTROSPECTED, TYPE_AND_MEMBERS_INTROSPECTED, negative());

        assertComparison(MEMBERS_BEING_INTROSPECTED, NOT_INTROSPECTED, positive());
        assertComparison(MEMBERS_BEING_INTROSPECTED, MEMBERS_BEING_INTROSPECTED, equal());
        assertComparison(MEMBERS_BEING_INTROSPECTED, TYPE_AND_MEMBERS_INTROSPECTED, negative());

        assertComparison(TYPE_AND_MEMBERS_INTROSPECTED, NOT_INTROSPECTED, positive());
        assertComparison(TYPE_AND_MEMBERS_INTROSPECTED, MEMBERS_BEING_INTROSPECTED, positive());
        assertComparison(TYPE_AND_MEMBERS_INTROSPECTED, TYPE_AND_MEMBERS_INTROSPECTED, equal());
    }

    private Matcher<Integer> equal() {
        return new TypeSafeMatcher<Integer>() {
            @Override protected boolean matchesSafely(final Integer integer) {
                return integer == 0;
            }

            @Override public void describeTo(final Description description) {
                description.appendText("equal");
            }
        };
    }

    private static Matcher<Integer> negative() {
        return new TypeSafeMatcher<Integer>() {
            @Override protected boolean matchesSafely(final Integer integer) {
                return integer < 0;
            }

            @Override public void describeTo(final Description description) {
                description.appendText("negative");
            }
        };
    }

    private static Matcher<Integer> positive() {
        return new TypeSafeMatcher<Integer>() {
            @Override protected boolean matchesSafely(final Integer integer) {
                return integer > 0;
            }

            @Override public void describeTo(final Description description) {
                description.appendText("positive");
            }
        };
    }

    private static void assertComparison(
            final IntrospectionState a,
            final IntrospectionState b, final Matcher<Integer> matcher) {
        assertThat(a.compareTo(b), is(matcher));
    }
}