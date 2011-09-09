package org.apache.isis.viewer.json.viewer.resources;

import org.apache.isis.viewer.json.applib.util.Enums;

public enum RepresentationType {

    HOME_PAGE,
    USER,
    LIST,
    SCALAR_VALUE,
    OBJECT,
    OBJECT_PROPERTY,
    OBJECT_COLLECTION,
    OBJECT_ACTION,
    DOMAIN_TYPE,
    DOMAIN_PROPERTY,
    DOMAIN_COLLECTION,
    DOMAIN_ACTION,
    DOMAIN_ACTION_PARAMETER;
    
    public String getName() {
        return Enums.enumToCamelCase(this);
    }

}
