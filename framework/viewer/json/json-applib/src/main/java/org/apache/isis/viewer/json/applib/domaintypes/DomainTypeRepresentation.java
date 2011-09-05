package org.apache.isis.viewer.json.applib.domaintypes;

import org.apache.isis.viewer.json.applib.JsonRepresentation;
import org.apache.isis.viewer.json.applib.LinksToSelf;
import org.apache.isis.viewer.json.applib.blocks.Link;
import org.codehaus.jackson.JsonNode;

public class DomainTypeRepresentation extends JsonRepresentation implements LinksToSelf {

    public DomainTypeRepresentation(JsonNode jsonNode) {
        super(jsonNode);
    }

    public Link getRepresentationType() {
        return getLink("representationType");
    }

    public Link getSelf() {
        return getLink("self");
    }

}
