package org.apache.isis.extensions.executionoutbox.restclient.api.deleteMany;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import org.apache.isis.extensions.executionoutbox.restclient.api.Jsonable;

import lombok.Getter;

public class DeleteManyMessage implements Jsonable {

    private static final ObjectWriter writer;

    static {
        final ObjectMapper mapper = new ObjectMapper();
        writer = mapper.writer().withDefaultPrettyPrinter();
    }

    @Getter
    private final StringValue interactionsDtoXml;

    public DeleteManyMessage(final String interactionsDtoXml) {
        this.interactionsDtoXml = new StringValue(interactionsDtoXml);
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
        return "[DELETE MANY MESSAGE] \n" +
                "interactionsDtoXml: " + interactionsDtoXml + "\n";
    }

}
