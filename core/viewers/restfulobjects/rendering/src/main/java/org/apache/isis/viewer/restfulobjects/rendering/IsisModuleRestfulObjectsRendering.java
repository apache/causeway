package org.apache.isis.viewer.restfulobjects.rendering;

import org.apache.isis.runtime.IsisModuleRuntime;
import org.apache.isis.viewer.restfulobjects.applib.IsisModuleRestfulObjectsApplib;
import org.apache.isis.viewer.restfulobjects.rendering.domainobjects.JsonValueEncoder;
import org.apache.isis.viewer.restfulobjects.rendering.service.RepresentationServiceContentNegotiator;
import org.apache.isis.viewer.restfulobjects.rendering.service.acceptheader.AcceptHeaderServiceForRest;
import org.apache.isis.viewer.restfulobjects.rendering.service.conneg.ContentNegotiationServiceForRestfulObjectsV1_0;
import org.apache.isis.viewer.restfulobjects.rendering.service.conneg.ContentNegotiationServiceOrgApacheIsisV1;
import org.apache.isis.viewer.restfulobjects.rendering.service.conneg.ContentNegotiationServiceXRoDomainType;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
        // modules
        IsisModuleRestfulObjectsApplib.class,
        IsisModuleRuntime.class,

        // @Service's
        JsonValueEncoder.class,
        RepresentationServiceContentNegotiator.class,
        ContentNegotiationServiceXRoDomainType.class,

        // @DomainService's
        AcceptHeaderServiceForRest.class,
        ContentNegotiationServiceForRestfulObjectsV1_0.class,
        ContentNegotiationServiceOrgApacheIsisV1.class,

})
public class IsisModuleRestfulObjectsRendering {
}
