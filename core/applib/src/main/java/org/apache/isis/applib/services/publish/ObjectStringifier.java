package org.apache.isis.applib.services.publish;


/**
 * To optionally configure the way that {@link EventPayloadForObjectChanged} and {@link EventPayloadForActionInvocation}
 * create the <tt>toString()</tt> form of their contents.
 */
public interface ObjectStringifier {
    public String toString(Object object);
    static class Simple implements ObjectStringifier {
        @Override
        public String toString(Object object) {
            return object != null? object.toString(): null;
        }
    }
}