package org.apache.isis.viewer.json.applib.domain;

import org.apache.isis.viewer.json.applib.JsonRepresentation;
import org.codehaus.jackson.JsonNode;


public class DomainObjectRepresentation extends JsonRepresentation {

    public DomainObjectRepresentation(JsonNode jsonNode) {
        super(jsonNode);
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

}
