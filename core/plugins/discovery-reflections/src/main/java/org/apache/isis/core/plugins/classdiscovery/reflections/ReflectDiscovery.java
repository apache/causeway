package org.apache.isis.core.plugins.classdiscovery.reflections;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.validation.constraints.NotNull;

import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.core.plugins.classdiscovery.ClassDiscovery;
import org.reflections.Reflections;

/**
 *
 * package private utility class
 *
 */
class ReflectDiscovery implements ClassDiscovery {

    private final Reflections reflections;

    // -- CONSTRUCTORS

    public static ReflectDiscovery of(List<String> packagePrefixes) {
        return new ReflectDiscovery(packagePrefixes);
    }

    public static ReflectDiscovery of(String packageNamePrefix) {
        return new ReflectDiscovery(packageNamePrefix);
    }

    public static ReflectDiscovery of(final Object... params) {
        return new ReflectDiscovery(params);
    }

    // -- HIDDEN CONSTRUCTOR

    private ReflectDiscovery(final Object... params) {
        this.reflections = new Reflections(params);
    }

    // -- IMPLEMENTATION

    @Override @NotNull
    public Set<Class<?>> getTypesAnnotatedWith(Class<? extends Annotation> annotation) {

        Objects.requireNonNull(annotation);

        // ensure unique entries
        return streamTypesAnnotatedWith(annotation).collect(Collectors.toCollection(HashSet::new));
    }

    @Override @NotNull
    public <T> Set<Class<? extends T>> getSubTypesOf(Class<T> type) {

        Objects.requireNonNull(type);

        // ensure unique entries
        return streamSubTypesOf(type).collect(Collectors.toCollection(HashSet::new));
    }

    // -- HELPER


    private Stream<Class<?>> streamTypesAnnotatedWith(Class<? extends Annotation> annotation) {

        // ensure non-null elements
        return _NullSafe.stream(reflections.getTypesAnnotatedWith(annotation))
                .filter(_NullSafe::isPresent);
    }

    private <T> Stream<Class<? extends T>> streamSubTypesOf(final Class<T> type) {

        // ensure non-null elements
        return _NullSafe.stream(reflections.getSubTypesOf(type))
                .filter(_NullSafe::isPresent);
    }



}
