package org.apache.isis.extensions.executionoutbox.restclient.api.delete;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import org.apache.isis.extensions.executionoutbox.restclient.api.Jsonable;

import lombok.Getter;

public class DeleteMessage implements Jsonable {

    private static final ObjectWriter writer;

    static {
        final ObjectMapper mapper = new ObjectMapper();
        writer = mapper.writer().withDefaultPrettyPrinter();
    }

    @Getter
    private final StringValue interactionId;
    @Getter
    private final IntValue sequence;

    public DeleteMessage(final String interactionId, final int sequence) {
        this.interactionId = new StringValue(interactionId);
        this.sequence = new IntValue(sequence);
    }

    public String asJson() {
        try {
            return writer.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        return "[DELETE MESSAGE] \n" +
                "interactionId: " + interactionId + "\n" +
                "sequence     : " + sequence + "\n";
    }

}
