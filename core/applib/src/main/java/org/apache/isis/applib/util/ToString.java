package org.apache.isis.applib.util;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.isis.commons.internal.collections._Lists;

/**
 * Fluent Object to String Composition.
 *
 * @param <T>
 * @since 2.0.0
 *
 */
public class ToString<T> {

    public static <T> ToString<T> toString(String name, Function<? super T, ?> getter) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(getter);
        return new ToString<>(name, getter);
    }

    private final List<String> names = _Lists.newArrayList();
    private final List<Function<? super T, ?>> getters = _Lists.newArrayList();

    private ToString(String name, Function<? super T, ?> getter) {
        names.add(name);
        getters.add(getter);
    }

    public ToString<T> thenToString(String name, Function<? super T, ?> getter){
        Objects.requireNonNull(name);
        Objects.requireNonNull(getter);
        names.add(name);
        getters.add(getter);
        return this;
    }

    public String toString(T target){
        return toString(target, value->""+value);
    }

    public String toString(T target, Function<Object, String> valueToStringFunction){

        if(valueToStringFunction==null) {
            return toString(target);
        }

        if(target==null) {
            return "null";
        }

        Objects.requireNonNull(valueToStringFunction);

        final Iterator<String> nameIterator = names.iterator();

        return String.format("%s{%s}",

                target.getClass().getSimpleName(),

                getters.stream()
                .map(getter->getter.apply(target))
                .map(valueToStringFunction)
                .map(valueLiteral->nameIterator.next()+"="+valueLiteral)
                .collect(Collectors.joining(", "))

                );
    }

}
