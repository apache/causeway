package org.apache.isis.viewer.json.applib.domainobjects;

import org.apache.isis.viewer.json.applib.JsonRepresentation;
import org.apache.isis.viewer.json.applib.JsonRepresentation.HasExtensions;
import org.apache.isis.viewer.json.applib.JsonRepresentation.HasLinks;
import org.apache.isis.viewer.json.applib.JsonRepresentation.LinksToSelf;
import org.apache.isis.viewer.json.applib.blocks.Link;
import org.codehaus.jackson.JsonNode;


public class ObjectActionRepresentation extends JsonRepresentation implements LinksToSelf, HasLinks, HasExtensions {

    public ObjectActionRepresentation(JsonNode jsonNode) {
        super(jsonNode);
    }
    
    public Link getSelf() {
        return getLink("self");
    }

    public String getDisabledReason() {
        return getString("disabledReason");
    }

    public String getMemberType() {
        return getString("memberType");
    }

    public Link getInvoke() {
        return getLink("invoke");
    }

    public String getActionId() {
        return getString("actionId");
    }

    public JsonRepresentation getLinks() {
        return getArray("links");
    }
    public JsonRepresentation getExtensions() {
        return getMap("extensions");
    }

    public Link getActionDetails() {
        return getLink("actionDetails");
    }

}
