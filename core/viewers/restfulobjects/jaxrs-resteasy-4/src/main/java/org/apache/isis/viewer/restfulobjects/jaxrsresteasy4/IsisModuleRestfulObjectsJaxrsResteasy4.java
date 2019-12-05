package org.apache.isis.viewer.restfulobjects.jaxrsresteasy4;

import org.apache.isis.viewer.restfulobjects.viewer.IsisModuleRestfulObjectsViewer;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
        // modules
        IsisModuleRestfulObjectsViewer.class
})
public class IsisModuleRestfulObjectsJaxrsResteasy4 {
}
