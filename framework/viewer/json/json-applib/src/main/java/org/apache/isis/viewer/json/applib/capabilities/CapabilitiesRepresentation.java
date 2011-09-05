package org.apache.isis.viewer.json.applib.capabilities;

import org.apache.isis.viewer.json.applib.JsonRepresentation;
import org.apache.isis.viewer.json.applib.LinksToSelf;
import org.apache.isis.viewer.json.applib.blocks.Link;
import org.codehaus.jackson.JsonNode;

public class CapabilitiesRepresentation extends JsonRepresentation implements LinksToSelf {

    public CapabilitiesRepresentation(JsonNode jsonNode) {
        super(jsonNode);
    }

    public Link getSelf() {
        return getLink("self");
    }

}
