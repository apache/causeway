package org.apache.isis.viewer.json.applib.domainobjects;

import org.apache.isis.viewer.json.applib.JsonRepresentation;
import org.apache.isis.viewer.json.applib.LinksToSelf;
import org.apache.isis.viewer.json.applib.blocks.Link;
import org.codehaus.jackson.JsonNode;


public class DomainServicesRepresentation extends JsonRepresentation implements LinksToSelf {

    public DomainServicesRepresentation(JsonNode jsonNode) {
        super(jsonNode);
    }

    public Link getSelf() {
        return getLink("self");
    }

}
