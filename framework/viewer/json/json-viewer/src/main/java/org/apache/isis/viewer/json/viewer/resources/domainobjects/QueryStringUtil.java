package org.apache.isis.viewer.json.viewer.resources.domainobjects;

import org.apache.isis.viewer.json.applib.JsonRepresentation;
import org.apache.isis.viewer.json.applib.RestfulResponse.HttpStatusCode;
import org.apache.isis.viewer.json.applib.util.JsonMapper;
import org.apache.isis.viewer.json.applib.util.UrlEncodingUtils;
import org.apache.isis.viewer.json.viewer.JsonApplicationException;

public final class QueryStringUtil {
    
    private QueryStringUtil(){}

    public static JsonRepresentation parseQueryString(String argumentsQueryString, final String descriptor, final String id) {
        if(argumentsQueryString == null) {
            return JsonRepresentation.newMap();
        }
        String argumentsTrimmed = argumentsQueryString.trim();
        if(argumentsTrimmed .isEmpty()) {
            return JsonRepresentation.newMap();
        }
        final String urlDecode = UrlEncodingUtils.urlDecode(argumentsTrimmed);
        try {
            return JsonMapper.instance().read(urlDecode);
        } catch (Exception e) {
            throw JsonApplicationException.create(HttpStatusCode.BAD_REQUEST,
                    "%s '%s' has query arguments that cannot be parsed as JSON", e, descriptor, id);
        }
    }

}
