package org.apache.isis.viewer.json.applib.domainobjects;

import org.apache.isis.viewer.json.applib.JsonRepresentation;
import org.apache.isis.viewer.json.applib.JsonRepresentation.HasExtensions;
import org.apache.isis.viewer.json.applib.JsonRepresentation.HasLinks;
import org.apache.isis.viewer.json.applib.JsonRepresentation.LinksToSelf;
import org.apache.isis.viewer.json.applib.blocks.Link;
import org.codehaus.jackson.JsonNode;


public class DomainObjectRepresentation extends JsonRepresentation implements LinksToSelf, HasLinks, HasExtensions {

    public DomainObjectRepresentation(JsonNode jsonNode) {
        super(jsonNode);
    }

    public Link getSelf() {
        return getLink("self");
    }

    public String getOid() {
        return getString("oid");
    }

    public String getTitle() {
        return getString("title");
    }
    
    /**
     * Requires xom:xom:1.1 (LGPL) to be added as a dependency.
     */
    public JsonRepresentation getProperties() {
        return xpath("/*[memberType='property']");
    }

    /**
     * Requires xom:xom:1.1 (LGPL) to be added as a dependency.
     */
    public JsonRepresentation getActions() {
        return xpath("/*[memberType='action']");
    }


    
    public JsonRepresentation getLinks() {
        return getArray("links");
    }
    public JsonRepresentation getExtensions() {
        return getMap("extensions");
    }

}
