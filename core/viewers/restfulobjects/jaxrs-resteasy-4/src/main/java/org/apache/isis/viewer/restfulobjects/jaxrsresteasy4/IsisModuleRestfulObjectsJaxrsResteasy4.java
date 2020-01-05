package org.apache.isis.viewer.restfulobjects.jaxrsresteasy4;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import org.apache.isis.viewer.restfulobjects.jaxrsresteasy4.conneg.RestfulObjectsJaxbWriterForXml;
import org.apache.isis.viewer.restfulobjects.jaxrsresteasy4.webmodule.WebModuleJaxrsResteasy4;
import org.apache.isis.viewer.restfulobjects.viewer.IsisModuleRestfulObjectsViewer;

@Configuration
@Import({
        // modules
        IsisModuleRestfulObjectsViewer.class,

        // @Service's
        IsisJaxrsUtilityServiceResteasy4.class,
        WebModuleJaxrsResteasy4.class,

        // @Component's
        RestfulObjectsJaxbWriterForXml.class,


})
public class IsisModuleRestfulObjectsJaxrsResteasy4 {

}
