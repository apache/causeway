package org.ro.urls

object RESTFUL_VERSION : Response(){
    override val url = "http://localhost:8080/restful/version"
    override val str = """ 
            {
        "links" : [ {
            "rel" : "self",
            "href" : "http://localhost:8080/restful/version",
            "method" : "GET",
            "type" : "application/jsonprofile=\"urn:org.restfulobjects:repr-types/version\""
        }, {
            "rel" : "up",
            "href" : "http://localhost:8080/restful/",
            "method" : "GET",
            "type" : "application/jsonprofile=\"urn:org.restfulobjects:repr-types/homepage\""
        } ],
        "specVersion" : "1.0.0",
        "implVersion" : "UNKNOWN",
        "optionalCapabilities" : {
            "blobsClobs" : "yes",
            "deleteObjects" : "yes",
            "domainModel" : "formal",
            "validateOnly" : "yes",
            "protoPersistentObjects" : "yes"
        },
        "extensions" : { }
    }"""
}
