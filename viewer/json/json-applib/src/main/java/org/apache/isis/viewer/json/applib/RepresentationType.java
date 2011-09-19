package org.apache.isis.viewer.json.applib;

import org.apache.isis.viewer.json.applib.util.Enums;

public enum RepresentationType {
    
    HOME_PAGE,
    USER,
    CAPABILITIES,
    LIST,
    SCALAR_VALUE,
    DOMAIN_OBJECT,
    OBJECT_PROPERTY,
    OBJECT_COLLECTION,
    OBJECT_ACTION,
    DOMAIN_TYPE,
    DOMAIN_TYPE_PROPERTY,
    DOMAIN_TYPE_COLLECTION,
    DOMAIN_TYPE_ACTION,
    DOMAIN_TYPE_ACTION_PARAMETER, 
    ERROR;
    
    private String name;
    private RepresentationType() {
        this.name = Enums.enumToCamelCase(this);
    }
    public String getName() {
        return name;
    }
    
    public static RepresentationType lookup(String value) {
        for(RepresentationType representationType: values()) {
            if (representationType.getName().equals(value)) {
                return representationType;
            }
        }
        return null;
    }
}
