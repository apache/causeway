package org.apache.isis.viewer.json.applib.blocks;

import org.apache.isis.viewer.json.applib.JsonRepresentation;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.JsonNodeFactory;

public class ArgumentList extends JsonRepresentation {
    
    public ArgumentList() {
        super(new ArrayNode(JsonNodeFactory.instance));
    }

    public void add(ArgumentNode value) {
        asArrayNode().add(value.asJsonNode());
    }

    public void add(Link value) {
        add(ArgumentNode.argOf(value));
    }

    @Override
    public void arrayAdd(String value) {
        add(ArgumentNode.argOf(value));
    }

    @Override
    public void arrayAdd(int value) {
        add(ArgumentNode.argOf(value));
    }

    @Override
    public void arrayAdd(boolean value) {
        add(ArgumentNode.argOf(value));
    }

    @Override
    public void arrayAdd(long value) {
        add(ArgumentNode.argOf(value));
    }

    @Override
    public void arrayAdd(double value) {
        add(ArgumentNode.argOf(value));
    }

    
    @Override
    public void arrayAdd(float value) {
        add(ArgumentNode.argOf(value));
    }

    @Override
    public void arrayAdd(Object o) {
        throw new UnsupportedOperationException("use add(ArgumentNode)");
    }

    @Override
    public void arrayAdd(JsonRepresentation value) {
        throw new UnsupportedOperationException("use add(ArgumentNode)");
    }

    @Override
    public void arrayAdd(JsonNode value) {
        throw new UnsupportedOperationException("use add(ArgumentNode)");
    }

}
