package org.apache.isis.core.commons.lang;

import java.util.Collection;
import java.util.List;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

public final class Types {
    
    private Types(){}

    public static <T> Collection<T> filtered(final List<Object> candidates, final Class<T> type) {
        return Collections2.transform(
                    Collections2.filter(candidates, Types.isOfType(type)),
                Types.castTo(type));
    }

    public static final <T> Predicate<Object> isOfType(final Class<T> type) {
        return new Predicate<Object>() {
            @Override
            public boolean apply(Object input) {
                return type.isAssignableFrom(input.getClass());
            }
        };
    }

    public static <T> Function<Object, T> castTo(final Class<T> type) {
        return new Function<Object, T>() {
            @SuppressWarnings("unchecked")
            @Override
            public T apply(final Object input) {
                return (T) input;
            }
        };
    }


}
