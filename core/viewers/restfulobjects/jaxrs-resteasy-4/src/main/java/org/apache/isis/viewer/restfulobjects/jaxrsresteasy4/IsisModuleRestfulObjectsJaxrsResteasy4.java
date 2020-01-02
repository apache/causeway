package org.apache.isis.viewer.restfulobjects.jaxrsresteasy4;

import org.apache.isis.viewer.restfulobjects.viewer.IsisModuleRestfulObjectsViewer;
import org.apache.isis.viewer.restfulobjects.jaxrsresteasy4.webmodule.WebModuleRestfulObjects;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
        // modules
        IsisModuleRestfulObjectsViewer.class,

        // @Service's
        WebModuleRestfulObjects.class,

})
public class IsisModuleRestfulObjectsJaxrsResteasy4 {
}
