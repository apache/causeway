package org.apache.isis.viewer.json.applib;

import java.io.IOException;

import net.sf.json.JSON;
import net.sf.json.JSONSerializer;
import net.sf.json.xml.XMLSerializer;

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
        JsonNode subNode = jsonNode.get(key);
        // TODO: extra checking here required
        
        try {
            // TODO: review, rather heavyweight
            return JsonMapper.instance().read(subNode.toString(), Link.class);
        } catch (JsonParseException e) {
            // shouldn't happen
            throw new RuntimeException(e);
        } catch (IOException e) {
            // shouldn't happen
            throw new RuntimeException(e);
        }
    }

    /**
     * Requires XOM to be added as a dependency.
     */
    public String toXml() {
        XMLSerializer serializer = new XMLSerializer();
        JSON json = JSONSerializer.toJSON(jsonNode.toString());
        String xml = serializer.write(json);
        return xml;
    }

    /**
     * Requires XOM to be added as a dependency.
     */
    public JsonRepresentation xpath(String xpathExpression) {
        String xml = toXml();
        // TODO: use XOM's xpath support on the xml to find subnodes, 
        // then serialize back to JSON
        return null;
    }

}
