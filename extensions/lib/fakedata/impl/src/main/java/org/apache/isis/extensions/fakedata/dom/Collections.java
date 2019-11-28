package org.apache.isis.extensions.fakedata.dom;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Predicate;

import org.apache.isis.applib.annotation.Programmatic;

public class Collections extends AbstractRandomValueGenerator{

    public Collections(final FakeDataService fakeDataService) {
        super(fakeDataService);
    }

    /**
     * @deprecated - renamed to {@link #anyEnum(Class)}.
     */
    @Deprecated
    @Programmatic
    public <E extends Enum<E>> E randomEnum(final Class<E> enumType) {
        return anyEnum(enumType);
    }

    @Programmatic
    public <E extends Enum<E>> E anyEnum(final Class<E> enumType) {
        final E[] enumConstants = enumType.getEnumConstants();
        return enumConstants[fake.ints().upTo(enumConstants.length)];
    }

    @Programmatic
    public <E extends Enum<E>> E anyEnumExcept(final Class<E> enumType, final Predicate<E> except) {
        return find(
            new Callable<E>() {
                @Override
                public E call() {
                    return anyEnum(enumType);
                }
            }, except);
    }

    /**
     * @deprecated - renamed to {@link #anyBounded(Class)}.
     */
    @Deprecated
    @Programmatic
    public <T> T randomBounded(final Class<T> cls) {
        final List<T> list = fake.repositoryService.allInstances(cls);
        return anyOf(list);
    }

    @Programmatic
    public <T> T anyBounded(final Class<T> cls) {
        final List<T> list = fake.repositoryService.allInstances(cls);
        return anyOf(list);
    }

    @Programmatic
    public <T> T anyBoundedExcept(final Class<T> cls, final Predicate<T> except) {
        final List<T> list = fake.repositoryService.allInstances(cls);
        return anyOfExcept(list, except);
    }

    @Programmatic
    public <T> T anyOf(final List<T> list) {
        final int randomIdx = fake.ints().upTo(list.size());
        return list.get(randomIdx);
    }

    @Programmatic
    public <T> T anyOf(final Collection<T> collection) {
        final int randomIdx = fake.ints().upTo(collection.size());
        int i = 0;
        for (T element : collection) {
            if (randomIdx == i++) {
                return element;
            }
        }
        throw new RuntimeException(
                "failed to obtain random element from collection - most likely a bug in the FakeData service itself");
    }

    @Programmatic
    public <T> T anyOfExcept(final List<T> list, final Predicate<T> except) {
        return find(
                new Callable<T>() {
                    @Override
                    public T call() {
                        return anyOf(list);
                    }
                }, except);
    }

    @Programmatic
    public <T> T anyOfExcept(final Collection<T> collection, final Predicate<T> except) {
        return find(
                new Callable<T>() {
                    @Override
                    public T call() {
                        return anyOf(collection);
                    }
                }, except);
    }

    @Programmatic
    public char anyOf(final char... elements) {
        final int randomIdx = fake.ints().upTo(elements.length);
        return elements[randomIdx];
    }

    @Programmatic
    public char anyOfExcept(final char[] elements, final Predicate<Character> except) {
        return find(
                new Callable<Character>() {
                    @Override
                    public Character call() {
                        return anyOf(elements);
                    }
                }, except);
    }

    @Programmatic
    public byte anyOf(final byte... elements) {
        final int randomIdx = fake.ints().upTo(elements.length);
        return elements[randomIdx];
    }

    @Programmatic
    public byte anyOfExcept(final byte[] elements, final Predicate<Byte> except) {
        return find(
                new Callable<Byte>() {
                    @Override
                    public Byte call() {
                        return anyOf(elements);
                    }
                }, except);
    }

    @Programmatic
    public short anyOf(final short... elements) {
        final int randomIdx = fake.ints().upTo(elements.length);
        return elements[randomIdx];
    }

    @Programmatic
    public short anyOfExcept(final short[] elements, final Predicate<Short> except) {
        return find(
                new Callable<Short>() {
                    @Override
                    public Short call() {
                        return anyOf(elements);
                    }
                }, except);
    }

    @Programmatic
    public int anyOf(final int... elements) {
        final int randomIdx = fake.ints().upTo(elements.length);
        return elements[randomIdx];
    }

    @Programmatic
    public int anyOfExcept(final int[] elements, final Predicate<Integer> except) {
        return find(
                new Callable<Integer>() {
                    @Override
                    public Integer call() {
                        return anyOf(elements);
                    }
                }, except);
    }

    @Programmatic
    public long anyOf(final long... elements) {
        final int randomIdx = fake.ints().upTo(elements.length);
        return elements[randomIdx];
    }

    @Programmatic
    public long anyOfExcept(final long[] elements, final Predicate<Long> except) {
        return find(
                new Callable<Long>() {
                    @Override
                    public Long call() {
                        return anyOf(elements);
                    }
                }, except);
    }

    @Programmatic
    public float anyOf(final float... elements) {
        final int randomIdx = fake.ints().upTo(elements.length);
        return elements[randomIdx];
    }

    @Programmatic
    public float anyOfExcept(final float[] elements, final Predicate<Float> except) {
        return find(
                new Callable<Float>() {
                    @Override
                    public Float call() {
                        return anyOf(elements);
                    }
                }, except);
    }

    @Programmatic
    public double anyOf(final double... elements) {
        final int randomIdx = fake.ints().upTo(elements.length);
        return elements[randomIdx];
    }

    @Programmatic
    public double anyOfExcept(final double[] elements, final Predicate<Double> except) {
        return find(
                new Callable<Double>() {
                    @Override
                    public Double call() {
                        return anyOf(elements);
                    }
                }, except);
    }

    @Programmatic
    public boolean anyOf(final boolean... elements) {
        final int randomIdx = fake.ints().upTo(elements.length);
        return elements[randomIdx];
    }

    @Programmatic
    public boolean anyOfExcept(final boolean[] elements, final Predicate<Boolean> except) {
        return find(
                new Callable<Boolean>() {
                    @Override
                    public Boolean call() {
                        return anyOf(elements);
                    }
                }, except);
    }

    @Programmatic
    public <T> T anyOf(final T... elements) {
        final int randomIdx = fake.ints().upTo(elements.length);
        return elements[randomIdx];
    }

    @Programmatic
    public <T> T anyOfExcept(final T[] elements, final Predicate<T> except) {
        return find(
                new Callable<T>() {
                    @Override
                    public T call() {
                        return anyOf(elements);
                    }
                }, except);
    }

    static <E> E find(final Callable<E> block, final Predicate<E> except) {
        for(int i=0; i<100; i++) {
            final E e;
            try {
                e = block.call();
            } catch (Exception e1) {
                throw new RuntimeException("Problem finding candidate values");
            }
            if(!except.test(e)) {
                return e;
            }
        }
        throw new RuntimeException("Failed to find a random element in collection");
    }

}
