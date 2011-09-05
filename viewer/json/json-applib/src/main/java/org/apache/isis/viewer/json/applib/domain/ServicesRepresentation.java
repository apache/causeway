package org.apache.isis.viewer.json.applib.domain;

import org.apache.isis.viewer.json.applib.JsonRepresentation;
import org.apache.isis.viewer.json.applib.blocks.Link;
import org.codehaus.jackson.JsonNode;


public class ServicesRepresentation extends JsonRepresentation {

    public ServicesRepresentation(JsonNode jsonNode) {
        super(jsonNode);
    }

    public Link getRepresentationType() {
        return getLink("representationType");
    }

    public Link getSelf() {
        return getLink("self");
    }

}
