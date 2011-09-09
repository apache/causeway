package org.apache.isis.viewer.json.applib.blocks;

import org.apache.isis.viewer.json.applib.JsonRepresentation;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.ObjectNode;

public class ArgumentMap extends JsonRepresentation {
    
    public ArgumentMap() {
        super(new ObjectNode(JsonNodeFactory.instance));
    }

    public void put(String key, ArgumentNode argumentNode) {
        super.mapPut(key, argumentNode); // same processing as JsonRepresentation
    }
    
}
