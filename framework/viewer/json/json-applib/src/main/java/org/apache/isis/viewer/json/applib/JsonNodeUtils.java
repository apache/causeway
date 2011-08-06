package org.apache.isis.viewer.json.applib;

import java.io.IOException;

import org.apache.isis.viewer.json.applib.util.JsonMapper;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;

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
}
