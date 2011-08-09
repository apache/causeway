package org.apache.isis.viewer.json.applib;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.isis.viewer.json.applib.util.JsonMapper;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;

import com.google.common.base.Charsets;

class JsonNodeUtils {
    
    private JsonNodeUtils(){}

    static <T> T convert(JsonNode subNode, Class<T> requiredType) {
        try {
            // TODO: review, rather heavyweight
            return JsonMapper.instance().read(subNode.toString(), requiredType);
        } catch (JsonParseException e) {
            // shouldn't happen
            throw new RuntimeException(e);
        } catch (IOException e) {
            // shouldn't happen
            throw new RuntimeException(e);
        }
    }

    static InputStream asInputStream(JsonNode jsonNode) {
        String jsonStr = jsonNode.toString();
        byte[] bytes = jsonStr.getBytes(Charsets.UTF_8);
        return new ByteArrayInputStream(bytes);
    }

    static JsonNode walkNode(JsonNode node, String path) {
        String[] keys = path.split("\\.");
        for(String key: keys) {
            node = node.path(key);
        }
        return node;
    }
}
