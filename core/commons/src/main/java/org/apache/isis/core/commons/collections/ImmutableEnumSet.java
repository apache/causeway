package org.apache.isis.core.commons.collections;

import java.util.EnumSet;
import java.util.Iterator;

/**
 * Immutable variant of {@link EnumSet}
 * 
 * @since 2.0
 */
public final class ImmutableEnumSet<E extends Enum<E>> 
implements Iterable<E>, java.io.Serializable {

    private static final long serialVersionUID = 1L;
    
    private final EnumSet<E> delegate;

    private ImmutableEnumSet(EnumSet<E> delegate) {
        this.delegate = delegate;
    }

    public static <E extends Enum<E>> ImmutableEnumSet<E> from(EnumSet<E> delegate) {
        return new ImmutableEnumSet<>(delegate);
    }
    
    public static <E extends Enum<E>> ImmutableEnumSet<E> noneOf(Class<E> enumType) {
        return from(EnumSet.noneOf(enumType));
    }

    public static <E extends Enum<E>> ImmutableEnumSet<E> of(E e1) {
        return from(EnumSet.of(e1));
    }
    
    public static <E extends Enum<E>> ImmutableEnumSet<E> of(E e1, E e2) {
        return from(EnumSet.of(e1, e2));
    }
    
    public static <E extends Enum<E>> ImmutableEnumSet<E> of(E e1, E e2, E e3) {
        return from(EnumSet.of(e1, e2, e3));
    }
    
    public static <E extends Enum<E>> ImmutableEnumSet<E> of(E e1, E e2, E e3, E e4) {
        return from(EnumSet.of(e1, e2, e3, e4));
    }

    public static <E extends Enum<E>> ImmutableEnumSet<E> complementOf(ImmutableEnumSet<E> other) {
        return from(EnumSet.complementOf(other.delegate));
    }
    
    public static <E extends Enum<E>> ImmutableEnumSet<E> allOf(Class<E> enumType) {
        return from(EnumSet.allOf(enumType));
    }
    
    public boolean contains(E element) {
        return delegate.contains(element);
    }
    
    public EnumSet<E> toEnumSet() {
        return delegate.clone();
    }
    
    @Override
    public Iterator<E> iterator() {
        return delegate.iterator();
    }




}
