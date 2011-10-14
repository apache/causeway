package org.apache.isis.viewer.json.applib.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.ObjectNode;

import com.google.common.base.Charsets;

public class JsonNodeUtils {
    
    private JsonNodeUtils(){}

    public static InputStream asInputStream(JsonNode jsonNode) {
        String jsonStr = jsonNode.toString();
        byte[] bytes = jsonStr.getBytes(Charsets.UTF_8);
        return new ByteArrayInputStream(bytes);
    }

    public static JsonNode walkNode(JsonNode node, String path) {
        String[] keys = path.split("\\.");
        for(String key: keys) {
            node = node.path(key);
        }
        return node;
    }

    /**
     * Walks the path, ensuring keys exist and are maps, or creating required
     * maps as it goes.
     * 
     * <p>
     * For example, if given a list ("a", "b", "c") and starting with an empty map,
     * then will create:
     * <pre>
     * {
     *   "a": {
     *     "b: {
     *       "c": {
     *       }       
     *     }
     *   }
     * }
     */
    public static ObjectNode walkNodeUpTo(ObjectNode node, List<String> keys) {
        for (String key : keys) {
            JsonNode jsonNode = node.get(key);
            if(jsonNode == null) {
                jsonNode = new ObjectNode(JsonNodeFactory.instance);
                node.put(key, jsonNode);
            } else {
                if(!jsonNode.isObject()) {
                    throw new IllegalArgumentException(String.format("walking path: '%s', existing key '%s' is not a map", keys, key));
                }
            }
            node = (ObjectNode) jsonNode;
        }
        return (ObjectNode) node;
    }

}
