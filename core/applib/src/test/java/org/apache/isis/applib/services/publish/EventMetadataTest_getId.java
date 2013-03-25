package org.apache.isis.applib.services.publish;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.UUID;

import org.junit.Test;

public class EventMetadataTest_getId {

    @Test
    public void test() {
        UUID transactionId = UUID.fromString("1bd8e5d4-2d67-4395-b5e8-d74acd766766");
        int sequence = 2;
        String user = "fred";
        long timestamp = 1364120978631L;
        String title = "some title";
        EventMetadata eventMetadata = new EventMetadata(transactionId, sequence, user, timestamp, title);
        
        assertThat(eventMetadata.getTransactionId(), is(UUID.fromString("1bd8e5d4-2d67-4395-b5e8-d74acd766766")));
        assertThat(eventMetadata.getSequence(), is(2));
        assertThat(eventMetadata.getUser(), is("fred"));
        assertThat(eventMetadata.getTimestamp(), is(1364120978631L));
        assertThat(eventMetadata.getId(), is("1bd8e5d4-2d67-4395-b5e8-d74acd766766.2"));
        assertThat(eventMetadata.getTitle(), is("some title"));
    }

}
