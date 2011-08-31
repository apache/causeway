package org.apache.isis.viewer.json.applib.blocks;

import org.apache.isis.viewer.json.applib.JsonRepresentation;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.ObjectNode;

public class ArgumentList extends JsonRepresentation {
    
    public ArgumentList() {
        super(new ArrayNode(JsonNodeFactory.instance));
    }

    public void add(String value) {
        ObjectNode objectNode = newValue();
        objectNode.put("value", value);
        nodeAsArray().add(objectNode);
    }

    public void add(boolean value) {
        ObjectNode objectNode = newValue();
        objectNode.put("value", value);
        nodeAsArray().add(objectNode);
    }

    public void add(double value) {
        ObjectNode objectNode = newValue();
        objectNode.put("value", value);
        nodeAsArray().add(objectNode);
    }

    public void add(int value) {
        ObjectNode objectNode = newValue();
        objectNode.put("value", value);
        nodeAsArray().add(objectNode);
    }

    public void add(long value) {
        ObjectNode objectNode = newValue();
        objectNode.put("value", value);
        nodeAsArray().add(objectNode);
    }
    
    public void add(Link value) {
        ObjectNode objectNode = newValue();
        objectNode.put("value", value.getJsonNode());
        nodeAsArray().add(objectNode);
    }
    
    private static ObjectNode newValue() {
        return new ObjectNode(JsonNodeFactory.instance);
    }
    
}
