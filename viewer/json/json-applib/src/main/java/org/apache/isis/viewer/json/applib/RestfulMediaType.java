package org.apache.isis.viewer.json.applib;

import javax.ws.rs.core.MediaType;

/**
 * Values per the profile parameter
 * 
 * @see http://buzzword.org.uk/2009/draft-inkster-profile-parameter-00.html
 */
public final class RestfulMediaType {
    
    
    private RestfulMediaType(){}

    private static final String BASE = MediaType.APPLICATION_JSON + ";profile=urn:org.restfulobjects/";
    
    public final static String APPLICATION_JSON_HOME_PAGE = BASE + "homepage";
    public final static String APPLICATION_JSON_USER = BASE + "user";
    public final static String APPLICATION_JSON_CAPABILITIES = BASE + "capabilities";
    public final static String APPLICATION_JSON_LIST = BASE + "list";
    public final static String APPLICATION_JSON_SCALAR_VALUE = BASE + "scalarvalue";
    public final static String APPLICATION_JSON_DOMAIN_OBJECT = BASE + "domainobject";
    public final static String APPLICATION_JSON_OBJECT_PROPERTY = BASE + "objectproperty";
    public final static String APPLICATION_JSON_OBJECT_COLLECTION = BASE + "objectcollection";
    public final static String APPLICATION_JSON_OBJECT_ACTION = BASE + "objectaction";
    public final static String APPLICATION_JSON_DOMAIN_TYPES = BASE + "domaintypes";
    public final static String APPLICATION_JSON_DOMAIN_TYPE = BASE + "domaintype";
    public final static String APPLICATION_JSON_TYPE_PROPERTY = BASE + "typeproperty";
    public final static String APPLICATION_JSON_TYPE_COLLECTION = BASE + "typecollection";
    public final static String APPLICATION_JSON_TYPE_ACTION = BASE + "typeaction";
    public final static String APPLICATION_JSON_TYPE_ACTION_PARAMETER = BASE + "typeactionparameter"; 
    public final static String APPLICATION_JSON_ERROR = BASE + "error";

}
