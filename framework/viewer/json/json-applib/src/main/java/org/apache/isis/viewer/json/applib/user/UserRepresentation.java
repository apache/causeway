package org.apache.isis.viewer.json.applib.user;

import org.apache.isis.viewer.json.applib.JsonRepresentation;
import org.apache.isis.viewer.json.applib.blocks.Link;
import org.codehaus.jackson.JsonNode;


public class UserRepresentation extends JsonRepresentation {

    public UserRepresentation(JsonNode jsonNode) {
        super(jsonNode);
    }
    
    public Link getRepresentationType() {
        return getLink("representationType");
    }

    public String getUserName() {
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

}
