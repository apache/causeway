package org.apache.isis.viewer.json.applib.blocks;

import org.apache.isis.viewer.json.applib.JsonRepresentation;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.ObjectNode;

public class ArgumentNode extends JsonRepresentation {
    
    public static ArgumentNode argOf(String value) {
        ObjectNode objectNode = newValue();
        objectNode.put("value", value);
        return new ArgumentNode(objectNode);
    }

    public static ArgumentNode argOf(boolean value) {
        ObjectNode objectNode = newValue();
        objectNode.put("value", value);
        return new ArgumentNode(objectNode);
    }

    public static ArgumentNode argOf(double value) {
        ObjectNode objectNode = newValue();
        objectNode.put("value", value);
        return new ArgumentNode(objectNode);
    }

    public static ArgumentNode argOf(float value) {
        ObjectNode objectNode = newValue();
        objectNode.put("value", value);
        return new ArgumentNode(objectNode);
    }

    public static ArgumentNode argOf(int value) {
        ObjectNode objectNode = newValue();
        objectNode.put("value", value);
        return new ArgumentNode(objectNode);
    }

    public static ArgumentNode argOf(long value) {
        ObjectNode objectNode = newValue();
        objectNode.put("value", value);
        return new ArgumentNode(objectNode);
    }
    
    public static ArgumentNode argOf(Link value) {
        ObjectNode objectNode = newValue();
        objectNode.put("value", value.asJsonNode());
        return new ArgumentNode(objectNode);
    }

    private static ObjectNode newValue() {
        return new ObjectNode(JsonNodeFactory.instance);
    }

    private ArgumentNode(ObjectNode node) {
        super(node);
    }

}
