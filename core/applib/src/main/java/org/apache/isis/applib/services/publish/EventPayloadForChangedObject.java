package org.apache.isis.applib.services.publish;

import org.apache.isis.applib.annotation.NotPersistable;
import org.apache.isis.applib.annotation.Programmatic;

/**
 * An immutable pojo that simply references some other (persistent) object.
 * 
 * <p>
 * This class is annotated as a domain object for the benefit of the
 * <tt>RestfulObjectsSpecEventSerializer</tt>.
 */
@NotPersistable
public class EventPayloadForChangedObject<T> implements EventPayload {
    
    private final T changed;
    private ObjectStringifier stringifier = new ObjectStringifier.Simple();

    public EventPayloadForChangedObject(T changed) {
        this.changed = changed;
    }

    @Programmatic
    public EventPayloadForChangedObject<T> with(ObjectStringifier stringifier) {
        this.stringifier = stringifier;
        return this;
    }

    public T getChanged() {
        return changed;
    }
    
    @Override
    public String toString() {
        return "CHANGED_OBJECT:"+ stringifier.toString(changed);
    }
}