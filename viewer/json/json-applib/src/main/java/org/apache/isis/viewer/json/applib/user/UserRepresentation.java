package org.apache.isis.viewer.json.applib.user;

import org.apache.isis.viewer.json.applib.JsonRepresentation;
import org.apache.isis.viewer.json.applib.JsonRepresentation.HasExtensions;
import org.apache.isis.viewer.json.applib.JsonRepresentation.HasLinks;
import org.apache.isis.viewer.json.applib.JsonRepresentation.LinksToSelf;
import org.apache.isis.viewer.json.applib.blocks.Link;
import org.codehaus.jackson.JsonNode;


public class UserRepresentation extends JsonRepresentation implements LinksToSelf, HasLinks, HasExtensions {

    public UserRepresentation(JsonNode jsonNode) {
        super(jsonNode);
    }
    
    public Link getSelf() {
        return getLink("self");
    }

    public String getUsername() {
        return getString("username");
    }

    public String getFriendlyName() {
        return getString("friendlyName");
    }

    public String getEmail() {
        return getString("email");
    }

    public JsonRepresentation getRoles() {
        return getRepresentation("roles");
    }

    public JsonRepresentation getLinks() {
        return getArray("links");
    }
    public JsonRepresentation getExtensions() {
        return getMap("extensions");
    }

}
