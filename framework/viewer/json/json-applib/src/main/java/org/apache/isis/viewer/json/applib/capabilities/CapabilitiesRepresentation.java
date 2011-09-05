package org.apache.isis.viewer.json.applib.capabilities;

import org.apache.isis.viewer.json.applib.JsonRepresentation;
import org.apache.isis.viewer.json.applib.JsonRepresentation.HasExtensions;
import org.apache.isis.viewer.json.applib.JsonRepresentation.HasLinks;
import org.apache.isis.viewer.json.applib.JsonRepresentation.LinksToSelf;
import org.apache.isis.viewer.json.applib.blocks.Link;
import org.codehaus.jackson.JsonNode;

public class CapabilitiesRepresentation extends JsonRepresentation implements LinksToSelf, HasLinks, HasExtensions {

    public CapabilitiesRepresentation(JsonNode jsonNode) {
        super(jsonNode);
    }

    public Link getSelf() {
        return getLink("self");
    }

    public JsonRepresentation getCapabilities() {
        return getMap("capabilities");
    }

    public JsonRepresentation getLinks() {
        return getArray("links");
    }
    public JsonRepresentation getExtensions() {
        return getMap("extensions");
    }

}
