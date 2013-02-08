package org.apache.isis.applib.services.publish;

import java.util.UUID;

/**
 * Standard metadata about an event to be published.
 * 
 * <p>
 * This is a class rather than an interface so that it may be easily extended in the future.

 * @see EventPayload
 */
public class EventMetadata {
    
    private final UUID guid;
    private final String user;
    private final long timestamp;
    
    public EventMetadata(UUID guid, String user, long timestamp) {
        this.guid = guid;
        this.user = user;
        this.timestamp = timestamp;
    }
    
    /**
     * Unique identifier of this event.
     */
    public UUID getGuid() {
        return guid;
    }
    /**
     * Represents the user that was responsible for generating the event.  
     */
    public String getUser() {
        return user;
    }
    /**
     * The timestamp, in milliseconds, since an epoch time.
     * @return
     */
    public long getTimestamp() {
        return timestamp;
    }
}