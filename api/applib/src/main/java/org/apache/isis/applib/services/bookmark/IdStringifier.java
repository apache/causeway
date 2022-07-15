package org.apache.isis.applib.services.bookmark;

import java.util.Optional;

import org.springframework.lang.Nullable;

import lombok.NonNull;
import lombok.val;

/**
 * SPI to converts the identifier (primary key) of an entity, of a given type (eg Integer) into a string, and
 * to convert back again into the key object used to actually look up the target entity instance; supported by both JDO
 * and JPA persistence mechanisms.
 *
 * <p>
 *     This is ultimately used by {@link BookmarkService} where we hold a persistent reference to an entity.  The
 *     resultant string also appears in URLs of the Wicket viewer and Restful Objects viewers, and in mementos eg
 *     in {@link org.apache.isis.schema.cmd.v2.CommandDto} and {@link org.apache.isis.schema.ixn.v2.InteractionDto}.
 * </p>
 *
 * <p>
 *     The framework provides default implementations of this SPI for JDO (data store and application identity) and
 *     for JPA. Because this is an SPI, other modules or application code can provide their own implementations.
 *     An example of such is the JPA implementation of the <code>commandlog</code> extension.
 * </p>
 */
public interface IdStringifier<T> {

    /**
     * Whether this {@link IdStringifier} is able to {@link #enstring(Object)} or {@link #destring(String, Class)} values
     * of this type.
     *
     * <p>
     * Even though some implementations also require the owning entity type in order to {@link #destring(String, Class)},
     * we do not consider that as part of this function; we assume that the entity type will be provided
     * when necessary (by the JDO entity facet, in fact).  This is sufficient.
     * </p>
     *
     * @param candidateValueClass
     */
    boolean handles(Class<?> candidateValueClass);


    /**
     * Convert the value (which will be of the same type as is {@link #handles(Class) handled} into a string
     * representation.
     *
     * @see #destring(String, Class)
     * @see #handles(Class)
     */
    String enstring(final T value);

    /**
     * Convert a string representation of the identifier (as returned by {@link #enstring(Object)}) into an object
     * that can be used to retrieve.
     *
     * @param stringified - as returned by {@link #enstring(Object)}
     * @param targetEntityClass - the class of the target entity, eg <code>Customer</code>.  For both JDO and JPA,
     *                            we always have this information available, and is needed (at least) by the JDO
     *                            implementations of application primary keys using built-ins, eg <code>LongIdentity</code>.
     */
    T destring(final String stringified, Class<?> targetEntityClass);

    abstract class Abstract<T> implements IdStringifier<T> {

        protected final static char SEPARATOR = '_';

        /**
         * eg <code>Integer.class</code>, or JDO-specific <code>DatastoreId</code>, or a custom class for application-defined PKs.
         */
        private final Class<T> valueClass;
        /**
         * Allows for a Stringifier to handle (for example) both <code>Integer.class</code> and <code>int.class</code>.
         */
        private final Optional<Class<T>> primitiveValueClass;

        public Abstract(
                final Class<T> handledClass) {
            this(handledClass, null);
        }
        public Abstract(
                final Class<T> valueClass,
                final @Nullable Class<T> primitiveClass
                ) {
            this.valueClass = valueClass;
            this.primitiveValueClass = Optional.ofNullable(primitiveClass);
        }

        @Override
        public boolean handles(Class<?> candidateValueClass) {
            return valueClass.isAssignableFrom(candidateValueClass) || primitiveValueClass.isPresent() && primitiveValueClass.get().isAssignableFrom(candidateValueClass);
        }
    }

    abstract class AbstractWithPrefix<T> extends Abstract<T> {

        private final String prefix;

        public AbstractWithPrefix(
                @NonNull final Class<T> handledClass,
                @NonNull final String typeCode) {
            this(handledClass, typeCode, null);
        }
        public AbstractWithPrefix(
                @NonNull final Class<T> handledClass,
                @NonNull final String typeCode,
                final @Nullable Class<T> primitiveClass) {
            super(handledClass, primitiveClass);
            this.prefix = typeCode + SEPARATOR;
        }

        @Override
        public final String enstring(T value) {
            return prefix + doStringify(value);
        }

        /**
         * Overridable hook
         */
        protected String doStringify(T value) {
            return value.toString();
        }

        @Override
        public final T destring(final String stringified, final @Nullable Class<?> targetEntityClass) {
            if (stringified == null) {
                return null;
            }
            val suffix = removePrefix(stringified);
            return doDestring(suffix, targetEntityClass);
        }

        /**
         * Mandatory hook
         */
        protected abstract T doDestring(String idStr, Class<?> owningType);

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
