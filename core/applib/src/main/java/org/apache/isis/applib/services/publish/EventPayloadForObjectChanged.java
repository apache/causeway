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
public class EventPayloadForObjectChanged<T> implements EventPayload {
    
    private final T changed;
    private ObjectStringifier stringifier;

    public EventPayloadForObjectChanged(T changed) {
        this.changed = changed;
    }

    /**
     * Injected by Isis runtime immediately after instantiation.
     */
    @Programmatic
    public void withStringifier(ObjectStringifier stringifier) {
        this.stringifier = stringifier;
    }

    public T getChanged() {
        return changed;
    }

    public String getClassName() {
        if(stringifier == null) {
            throw new IllegalStateException("ObjectStringifier has not been injected");
        }
        return stringifier.classNameOf(changed);
    }

    @Override
    public String toString() {
        if(stringifier == null) {
            throw new IllegalStateException("ObjectStringifier has not been injected");
        }
        return stringifier.toString(changed);
    }
}