package org.apache.isis.viewer.json.viewer.representations;

public enum Rel {
    // IANA registered
    SELF("self"),
    DESCRIBEDBY("describedby"),
    
    // Restful Objects namespace
    OBJECT("object"), 
    SERVICE("service"), 
    DETAILS("details"), 
    MODIFY("modify"), 
    CLEAR("clear"), 
    ADD_TO("addTo"),
    REMOVE_FROM("removeFrom"), 
    INVOKE("invoke"), 
    CAPABILITIES("capabilities"), 
    SERVICES("services"), 
    USER("user"),
    
    // implementation specific
    CONTRIBUTED_BY("contributedBy")
    ;
    
    private final String name;
    private Rel(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
}