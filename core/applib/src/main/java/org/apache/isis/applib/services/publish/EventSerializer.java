package org.apache.isis.applib.services.publish;

import org.apache.isis.applib.annotation.Programmatic;
import org.w3c.dom.Document;

public interface EventSerializer {

    /**
     * Combine the {@link EventMetadata metadata} and the {@link EventPayload payload}
     * into some serialized form (such as JSON, XML or a string) that can then be published.
     * 
     * <p>
     * This method returns an object for maximum flexibility, which is then
     * handed off to the {@link PublishingService}.  It's important to make sure that the
     * publishing service is able to handle the serialized form.  Strings are a good
     * lowest common denominator, but in some cases are type-safe equivalent, such as a
     * {@link Document w3c DOM Document} or a JSON node might be passed instead.
     *  
     * @return a string, some JSON, some XML or some other standard serialized form. 
     */
    public Object serialize(EventMetadata metadata, EventPayload payload);
    
    public static class Simple implements EventSerializer {

        @Programmatic
        @Override
        public Object serialize(EventMetadata metadata, EventPayload payload) {
            return "PUBLISHED: \n    metadata: " + metadata.getGuid() + ":" + metadata.getUser() + ":" + metadata.getTimestamp() + ":    payload:s\n"+ payload.toString();
        }
    }

}
