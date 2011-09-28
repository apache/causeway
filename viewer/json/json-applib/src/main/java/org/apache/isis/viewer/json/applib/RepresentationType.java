package org.apache.isis.viewer.json.applib;

import javax.ws.rs.core.MediaType;

import org.apache.isis.viewer.json.applib.util.Enums;

public enum RepresentationType {
    
    HOME_PAGE(RestfulMediaType.APPLICATION_JSON_HOME_PAGE),
    USER(RestfulMediaType.APPLICATION_JSON_USER),
    CAPABILITIES(RestfulMediaType.APPLICATION_JSON_CAPABILITIES),
    LIST(RestfulMediaType.APPLICATION_JSON_LIST),
    SCALAR_VALUE(RestfulMediaType.APPLICATION_JSON_SCALAR_VALUE),
    DOMAIN_OBJECT(RestfulMediaType.APPLICATION_JSON_DOMAIN_OBJECT),
    OBJECT_PROPERTY(RestfulMediaType.APPLICATION_JSON_OBJECT_PROPERTY),
    OBJECT_COLLECTION(RestfulMediaType.APPLICATION_JSON_OBJECT_COLLECTION),
    OBJECT_ACTION(RestfulMediaType.APPLICATION_JSON_OBJECT_ACTION),
    DOMAIN_TYPES(RestfulMediaType.APPLICATION_JSON_DOMAIN_TYPES),
    DOMAIN_TYPE(RestfulMediaType.APPLICATION_JSON_DOMAIN_TYPE),
    TYPE_PROPERTY(RestfulMediaType.APPLICATION_JSON_TYPE_PROPERTY),
    TYPE_COLLECTION(RestfulMediaType.APPLICATION_JSON_TYPE_COLLECTION),
    TYPE_ACTION(RestfulMediaType.APPLICATION_JSON_TYPE_ACTION),
    TYPE_ACTION_PARAMETER(RestfulMediaType.APPLICATION_JSON_TYPE_ACTION_PARAMETER), 
    ERROR(RestfulMediaType.APPLICATION_JSON_ERROR);
    
    private final String name;
    private final MediaType mediaType;
    
    private RepresentationType(String mediaTypeStr) {
        this.name = Enums.enumToCamelCase(this);
        this.mediaType = MediaType.valueOf(mediaTypeStr);
    }

    public String getName() {
        return name;
    }
    
    public final MediaType getMediaType() {
        return mediaType;
    }
    
    public static RepresentationType lookup(String value) {
        for(RepresentationType representationType: values()) {
            if (representationType.getName().equals(value)) {
                return representationType;
            }
        }
        return null;
    }
    
    public static Parser<RepresentationType> parser() {
        return new Parser<RepresentationType>() {
            @Override
            public RepresentationType valueOf(String str) {
                return RepresentationType.lookup(str);
            }
            @Override
            public String asString(RepresentationType t) {
                return t.getName();
            }
        };
    }
    
    
}
