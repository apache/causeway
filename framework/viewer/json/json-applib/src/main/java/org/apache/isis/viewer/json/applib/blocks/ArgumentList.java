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
        nodeAsArray().add(value.getJsonNode());
    }

    public void add(Link value) {
        add(ArgumentNode.argOf(value));
    }

    @Override
    public void add(String value) {
        add(ArgumentNode.argOf(value));
    }

    @Override
    public void add(int value) {
        add(ArgumentNode.argOf(value));
    }

    @Override
    public void add(boolean value) {
        add(ArgumentNode.argOf(value));
    }

    @Override
    public void add(long value) {
        add(ArgumentNode.argOf(value));
    }

    @Override
    public void add(double value) {
        add(ArgumentNode.argOf(value));
    }

    
    @Override
    public void add(float value) {
        add(ArgumentNode.argOf(value));
    }

    @Override
    public void add(Object o) {
        throw new UnsupportedOperationException("use add(ArgumentNode)");
    }

    @Override
    public void add(JsonRepresentation value) {
        throw new UnsupportedOperationException("use add(ArgumentNode)");
    }

    @Override
    public void add(JsonNode value) {
        throw new UnsupportedOperationException("use add(ArgumentNode)");
    }

}
