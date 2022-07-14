package org.apache.isis.applib.services.bookmark;

import java.util.Optional;

import lombok.Getter;

/**
 * SPI to allow other modules to extend the bookmarking mechanism.
 *
 * <p>
 * Originally introduced to allow JPA CommandLog implementation use CommandLogEntryPK for its primary key.
 * </p>
 */
public interface IdStringifier<T> {

    boolean handles(Class<?> candidateClass);

    Class<T> getHandledClass();

    String stringify(final T value);

    T parse(final String stringified);

    abstract class Abstract<T> implements IdStringifier<T> {

        @Getter
        private final Class<T> handledClass;
        /**
         * Allows for a Stringifier to handle (for example) both <code>Integer.class</code> and <code>int.class</code>.
         */
        @Getter
        private final Optional<Class<T>> primitiveClass;

        public Abstract(java.lang.Class<T> handledClass) {
            this(handledClass, null);
        }
        public Abstract(java.lang.Class<T> handledClass, @org.springframework.lang.Nullable java.lang.Class<T> primitiveClass) {
            this.handledClass = handledClass;
            this.primitiveClass = Optional.ofNullable(primitiveClass);
        }

        @Override
        public boolean handles(Class<?> candidateClass) {
            return candidateClass == handledClass || primitiveClass.isPresent() && primitiveClass.get() == candidateClass ;
        }
    }

}
