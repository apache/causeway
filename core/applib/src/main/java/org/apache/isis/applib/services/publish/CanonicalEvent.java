package org.apache.isis.applib.services.publish;

/**
 * An event in canonical form to be published
 * 
 */
public interface CanonicalEvent {

    public final static class Default implements CanonicalEvent {

        private final String str;
        
        public Default(String str) {
            this.str = str;
        }

        @Override
        public String asString() {
            return str;
        }

        @Override
        public String toString() {
            return asString();
        }
    }

    public String asString();
}
