package org.apache.isis.viewer.json.viewer.resources;

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
        StringBuilder builder = new StringBuilder();
        boolean nextUpper = false;
        for(char c: name().toCharArray()) {
            if(c == '_') {
                nextUpper = true;
            } else {
                builder.append(nextUpper?c:Character.toLowerCase(c));
                nextUpper = false;
            }
        }
        return builder.toString();
    }

}
