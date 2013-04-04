package org.apache.isis.objectstore.jdo.applib.service.publish;

import java.util.List;

import org.apache.isis.applib.AbstractService;
import org.apache.isis.applib.annotation.ActionSemantics;
import org.apache.isis.applib.annotation.ActionSemantics.Of;
import org.apache.isis.applib.annotation.Bulk;
import org.apache.isis.applib.annotation.Hidden;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Named;
import org.apache.isis.applib.annotation.NotInServiceMenu;
import org.apache.isis.applib.query.QueryDefault;
import org.apache.isis.applib.services.publish.EventMetadata;
import org.apache.isis.applib.services.publish.EventPayload;
import org.apache.isis.applib.services.publish.EventSerializer;
import org.apache.isis.applib.services.publish.PublishingService;
import org.apache.isis.objectstore.jdo.applib.service.publish.PublishedEvent.State;

/**
 * An implementation of {@link PublishingService} that persists events as
 * entities into a JDO-backed database.
 */
@Named("Integration")
public class PublishingServiceJdo extends AbstractService implements PublishingService {

    private EventSerializer eventSerializer;

    @Override
    @Hidden
    public void publish(EventMetadata metadata, EventPayload payload) {
        final String serializedEvent = eventSerializer.serialize(metadata, payload).toString();
        final PublishedEvent publishedEvent = newTransientInstance(PublishedEvent.class);
        publishedEvent.setSerializedForm(serializedEvent);
        publishedEvent.setId(metadata.getId());
        publishedEvent.setTransactionId(metadata.getTransactionId().toString());
        publishedEvent.setSequence(metadata.getSequence());
        publishedEvent.setEventType(metadata.getEventType());
        publishedEvent.setTimestamp(metadata.getTimestamp());
        publishedEvent.setUser(metadata.getUser());
        publishedEvent.setTitle(metadata.getTitle());
        persist(publishedEvent);
    }

    @ActionSemantics(Of.SAFE)
    @MemberOrder(sequence="1")
    public List<PublishedEvent> queuedEvents() {
        return allMatches(
                new QueryDefault<PublishedEvent>(PublishedEvent.class, 
                        "publishedevent_of_state", 
                        "state", PublishedEvent.State.QUEUED));
    }

    @ActionSemantics(Of.SAFE)
    @MemberOrder(sequence="2")
    public List<PublishedEvent> processedEvents() {
        return allMatches(
                new QueryDefault<PublishedEvent>(PublishedEvent.class, 
                        "publishedevent_of_state", 
                        "state", PublishedEvent.State.PROCESSED));
    }

    @ActionSemantics(Of.SAFE)
    @MemberOrder(sequence="3")
    public void purgeProcessed() {
        // REVIEW: this is not particularly performant.
        // much better would be to go direct to the JDO API.
        List<PublishedEvent> processedEvents = processedEvents();
        for (PublishedEvent publishedEvent : processedEvents) {
            publishedEvent.delete();
        }
    }

    @Hidden
    @Override
    public void setEventSerializer(EventSerializer eventSerializer) {
        this.eventSerializer = eventSerializer;
    }

}
