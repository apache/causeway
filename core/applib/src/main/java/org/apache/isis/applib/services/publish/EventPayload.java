package org.apache.isis.applib.services.publish;

import org.apache.isis.applib.annotation.Programmatic;




/**
 * The payload of an event, representing the information to be published 
 * in some canonicalized form.
 * 
 * <p>
 * This should be prepared in a way that can be processed by the {@link EventSerializer}.  For example:
 * <ul>
 * <li>The {@link EventSerializer.Simple simple event serializer} simply invokes
 * {@link Object#toString() toString()} on the payload.  Use the {@link Simple simple} implementation
 * which simply wraps a string.
 * </li>
 * <li>The <tt>RestfulObjectsSpecEventSerializer</tt> event serializer expects a pojo domain object
 * and serializes it out according to the <a href="http://restfulobject.org">Restful Objects spec</a>.
 * Use the {@link EventPayloadForActionInvocation object payload} implementation if you simply wish to reference some
 * persistent domain object.
 * </li>
 * </ul>
 * 
 * @see EventMetadata
 */
public interface EventPayload {

    /**
     * Injected by Isis runtime immediately after instantiation.
     */
    @Programmatic
    void withStringifier(ObjectStringifier stringifier);

}
