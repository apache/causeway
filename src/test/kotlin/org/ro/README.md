### Links to be implemented
* http://localhost:8080/restful/user
```
{
  "userName" : "sven",
  "roles" : [ "iniRealm:admin_role" ],
  "links" : [ {
    "rel" : "self",
    "href" : "http://localhost:8080/restful/user",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/user\""
  }, {
    "rel" : "up",
    "href" : "http://localhost:8080/restful/",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/homepage\""
  }, {
    "rel" : "urn:org.apache.isis.restfulobjects:rels/logout",
    "href" : "http://localhost:8080/restful/user/logout",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/homepage\""
  } ],
  "extensions" : { }
}
```

* http://localhost:8080/restful/menuBars (xml)
* http://localhost:8080/restful/version
```
 {
   "links" : [ {
     "rel" : "self",
     "href" : "http://localhost:8080/restful/version",
     "method" : "GET",
     "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/version\""
   }, {
     "rel" : "up",
     "href" : "http://localhost:8080/restful/",
     "method" : "GET",
     "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/homepage\""
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
 }
``` 
 * http://localhost:8080/restful/domain-types
