package org.apache.isis.viewer.json.applib;

import javax.ws.rs.core.MediaType;

import org.apache.isis.viewer.json.applib.capabilities.CapabilitiesRepresentation;
import org.apache.isis.viewer.json.applib.domainobjects.DomainObjectRepresentation;
import org.apache.isis.viewer.json.applib.domainobjects.ListRepresentation;
import org.apache.isis.viewer.json.applib.domainobjects.ObjectActionRepresentation;
import org.apache.isis.viewer.json.applib.domainobjects.ObjectCollectionRepresentation;
import org.apache.isis.viewer.json.applib.domainobjects.ObjectPropertyRepresentation;
import org.apache.isis.viewer.json.applib.domainobjects.ScalarValueRepresentation;
import org.apache.isis.viewer.json.applib.domaintypes.DomainTypeRepresentation;
import org.apache.isis.viewer.json.applib.domaintypes.DomainTypesRepresentation;
import org.apache.isis.viewer.json.applib.domaintypes.TypeActionParameterRepresentation;
import org.apache.isis.viewer.json.applib.domaintypes.TypeActionRepresentation;
import org.apache.isis.viewer.json.applib.domaintypes.TypeCollectionRepresentation;
import org.apache.isis.viewer.json.applib.domaintypes.TypePropertyRepresentation;
import org.apache.isis.viewer.json.applib.homepage.HomePageRepresentation;
import org.apache.isis.viewer.json.applib.user.UserRepresentation;
import org.apache.isis.viewer.json.applib.util.Enums;

public enum RepresentationType {
    
    HOME_PAGE(RestfulMediaType.APPLICATION_JSON_HOME_PAGE, HomePageRepresentation.class),
    USER(RestfulMediaType.APPLICATION_JSON_USER, UserRepresentation.class),
    CAPABILITIES(RestfulMediaType.APPLICATION_JSON_CAPABILITIES, CapabilitiesRepresentation.class),
    LIST(RestfulMediaType.APPLICATION_JSON_LIST, ListRepresentation.class),
    SCALAR_VALUE(RestfulMediaType.APPLICATION_JSON_SCALAR_VALUE, ScalarValueRepresentation.class),
    DOMAIN_OBJECT(RestfulMediaType.APPLICATION_JSON_DOMAIN_OBJECT, DomainObjectRepresentation.class),
    OBJECT_PROPERTY(RestfulMediaType.APPLICATION_JSON_OBJECT_PROPERTY, ObjectPropertyRepresentation.class),
    OBJECT_COLLECTION(RestfulMediaType.APPLICATION_JSON_OBJECT_COLLECTION, ObjectCollectionRepresentation.class),
    OBJECT_ACTION(RestfulMediaType.APPLICATION_JSON_OBJECT_ACTION, ObjectActionRepresentation.class),
    DOMAIN_TYPES(RestfulMediaType.APPLICATION_JSON_DOMAIN_TYPES, DomainTypesRepresentation.class),
    DOMAIN_TYPE(RestfulMediaType.APPLICATION_JSON_DOMAIN_TYPE, DomainTypeRepresentation.class),
    TYPE_PROPERTY(RestfulMediaType.APPLICATION_JSON_TYPE_PROPERTY, TypePropertyRepresentation.class),
    TYPE_COLLECTION(RestfulMediaType.APPLICATION_JSON_TYPE_COLLECTION, TypeCollectionRepresentation.class),
    TYPE_ACTION(RestfulMediaType.APPLICATION_JSON_TYPE_ACTION, TypeActionRepresentation.class),
    TYPE_ACTION_PARAMETER(RestfulMediaType.APPLICATION_JSON_TYPE_ACTION_PARAMETER, TypeActionParameterRepresentation.class), 
    ERROR(RestfulMediaType.APPLICATION_JSON_ERROR, ErrorRepresentation.class),
    GENERIC(MediaType.APPLICATION_JSON, JsonRepresentation.class);
    
    private final String name;
    private final MediaType mediaType;
    private final Class<? extends JsonRepresentation> representationClass;
    
    private RepresentationType(String mediaTypeStr, Class<? extends JsonRepresentation> representationClass) {
        this.representationClass = representationClass;
        this.name = Enums.enumToCamelCase(this);
        this.mediaType = MediaType.valueOf(mediaTypeStr);
    }

    public String getName() {
        return name;
    }
    
    public final MediaType getMediaType() {
        return mediaType;
    }
    
    public Class<? extends JsonRepresentation> getRepresentationClass() {
        return representationClass;
    }
    
    public static RepresentationType lookup(String name) {
        for(RepresentationType representationType: values()) {
            if (representationType.getName().equals(name)) {
                return representationType;
            }
        }
        return RepresentationType.GENERIC;
    }
    
    public static RepresentationType lookup(MediaType mediaType) {
        for(RepresentationType representationType: values()) {
            if (representationType.getMediaType().equals(mediaType)) {
                return representationType;
            }
        }
        return RepresentationType.GENERIC;
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
