package org.apache.isis.viewer.json.applib;

import java.io.IOException;

import org.apache.isis.viewer.json.applib.blocks.Link;
import org.apache.isis.viewer.json.applib.util.JsonMapper;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;


/**
 * A wrapper around {@link JsonNode} that provides some additional helper methods.
 */
public class JsonRepresentation {

    private final JsonNode jsonNode;

    public JsonRepresentation(JsonNode jsonNode) {
        this.jsonNode = jsonNode;
    }

    public JsonNode getJsonNode() {
        return jsonNode;
    }

    public int size() {
        if(!jsonNode.isArray()) {
            throw new IllegalStateException("Is an array");
        }
        return jsonNode.size();
    }

    public String getString(String key) {
        JsonNode subNode = jsonNode.get(key);
        if(subNode == null) {
            return null;
        }
        ensureValue(key, subNode, "string");
        if(!subNode.isTextual()) {
            throw new IllegalArgumentException("'" + key + "' (" + subNode.toString() + ") is not a string");
        }
        return subNode.getTextValue();
    }

    private void ensureValue(String key, JsonNode subNode, String requiredType) {
        if(subNode.isValueNode() ) {
            return;
        }
        if(subNode.isArray()) {
            throw new IllegalArgumentException("'" + key + "' (a list) is not a " + requiredType);
        } else {
            throw new IllegalArgumentException("'" + key + "' (a map) is not a " + requiredType);
        }
    }

    public Link getLink(String key) throws JsonMappingException {
        JsonNode jsonNode2 = jsonNode.get(key);
        // TODO: review, rather heavyweight
        try {
            return JsonMapper.instance().read(jsonNode2.toString(), Link.class);
        } catch (JsonParseException e) {
            // shouldn't happen
            throw new RuntimeException(e);
        } catch (IOException e) {
            // shouldn't happen
            throw new RuntimeException(e);
        }
    }

}
