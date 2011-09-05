package org.apache.isis.viewer.json.applib.homepage;

import org.apache.isis.viewer.json.applib.JsonRepresentation;
import org.apache.isis.viewer.json.applib.JsonRepresentation.HasExtensions;
import org.apache.isis.viewer.json.applib.JsonRepresentation.HasLinks;
import org.apache.isis.viewer.json.applib.JsonRepresentation.LinksToSelf;
import org.apache.isis.viewer.json.applib.blocks.Link;
import org.codehaus.jackson.JsonNode;

public class HomePageRepresentation extends JsonRepresentation implements LinksToSelf, HasLinks, HasExtensions {

    public HomePageRepresentation(JsonNode jsonNode) {
        super(jsonNode);
    }

    public Link getSelf() {
        return getLink("self");
    }
    public Link getUser() {
        return getLink("user");
    }
    public Link getServices() {
        return getLink("services");
    }
    public Link getCapabilities() {
        return getLink("capabilities");
    }

    public JsonRepresentation getLinks() {
        return getArray("links");
    }
    public JsonRepresentation getExtensions() {
        return getMap("extensions");
    }

}
