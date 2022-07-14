package org.apache.isis.applib.services.bookmark;

import java.util.Optional;

import org.springframework.lang.Nullable;

import org.apache.isis.commons.internal.base._Strings;

import lombok.Getter;
import lombok.NonNull;
import lombok.val;

/**
 * SPI to allow other modules to extend the bookmarking mechanism.
 *
 * <p>
 * Also used internally so that simple values (such as numbers) are trivially serialized.
 * </p>
 */
public interface IdStringifier<T> {

    /**
     * Whether this {@link IdStringifier} is able to {@link #stringify(Object)} or {@link #parse(String, Class)} values
     * of this type.
     *
     * <p>
     *     Even though some implementations also require the owning entity type in order to {@link #parse(String, Class)},
     *     we do not consider that as part of this function; we assume that the entity type will be provided
     *     when necessary (by the JDO entity facet, in fact).  This is sufficient.
     * </p>
     * @param candidateValueClass
     */
    boolean handles(Class<?> candidateValueClass);

    Class<T> getHandledClass();

    String stringify(final T value);

    /**
     *
     * @param stringified - as returned by {@link #stringify(Object)}
     * @param owningEntityType - optional, but if provided is a hint as to the type of the owning entity type of this identifier.  Used by JDO implementations for their opaque types <code>ByteIdentity</code> and similar).
     */
    T parse(final String stringified, Class<?> owningEntityType);

    abstract class Abstract<T> implements IdStringifier<T> {

        protected final static char SEPARATOR = '_';

        @Getter
        private final Class<T> handledClass;
        /**
         * Allows for a Stringifier to handle (for example) both <code>Integer.class</code> and <code>int.class</code>.
         */
        @Getter
        private final Optional<Class<T>> primitiveClass;

        public Abstract(
                final Class<T> handledClass) {
            this(handledClass, null);
        }
        public Abstract(
                final Class<T> handledClass,
                final @Nullable Class<T> primitiveClass) {
            this.handledClass = handledClass;
            this.primitiveClass = Optional.ofNullable(primitiveClass);
        }

        @Override
        public boolean handles(Class<?> candidateValueClass) {
            return handledClass.isAssignableFrom(candidateValueClass) || primitiveClass.isPresent() && primitiveClass.get().isAssignableFrom(candidateValueClass) ;
        }
    }

    abstract class AbstractWithPrefix<T> extends Abstract<T> {

        private final String prefix;

        public AbstractWithPrefix(
                final Class<T> handledClass,
                final String typeCode) {
            this(handledClass, typeCode, null);
        }
        public AbstractWithPrefix(
                final Class<T> handledClass,
                final String typeCode,
                final @Nullable Class<T> primitiveClass) {
            super(handledClass, primitiveClass);
            this.prefix = typeCode + SEPARATOR;
        }

        @Override
        public final String stringify(T value) {
            return prefix + doStringify(value);
        }

        /**
         * Overridable hook
         */
        protected String doStringify(T value) {
            return value.toString();
        }

        @Override
        public final T parse(final String stringified, final @Nullable Class<?> owningEntityType) {
            if (stringified == null) {
                return null;
            }
            val suffix = removePrefix(stringified);
            return doParse(suffix, owningEntityType);
        }

        /**
         * Mandatory hook
         */
        protected abstract T doParse(String idStr, Class<?> owningType);

        private String removePrefix(String str) {
            if (str.startsWith(prefix)) {
                return str.substring(prefix.length());
            }
            throw new IllegalArgumentException(String.format("expected id to start with '%s', but got '%s'", prefix, str));
        }

        /**
         * Not API
         */
        public boolean recognizes(String stringified) {
            return stringified.startsWith(prefix);
        }

    }

}
