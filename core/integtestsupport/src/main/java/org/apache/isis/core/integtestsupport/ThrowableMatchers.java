package org.apache.isis.core.integtestsupport;

import java.util.List;

import com.google.common.base.Throwables;
import com.google.common.collect.FluentIterable;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

public class ThrowableMatchers {

    ThrowableMatchers(){}

    public static TypeSafeMatcher<Throwable> causedBy(final Class<?> type) {
        return new TypeSafeMatcher<Throwable>() {
            @Override
            protected boolean matchesSafely(final Throwable throwable) {
                final List<Throwable> causalChain = Throwables.getCausalChain(throwable);
                return !FluentIterable.from(causalChain).filter(type).isEmpty();
            }

            @Override public void describeTo(final Description description) {
                description.appendText("Caused by " + type.getName());
            }
        };
    }

}
