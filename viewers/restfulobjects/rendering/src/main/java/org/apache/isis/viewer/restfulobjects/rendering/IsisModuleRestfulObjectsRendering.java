package org.apache.isis.viewer.restfulobjects.rendering;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import org.apache.isis.core.runtime.IsisModuleRuntime;
import org.apache.isis.viewer.restfulobjects.applib.IsisModuleRestfulObjectsApplib;
import org.apache.isis.viewer.restfulobjects.rendering.domainobjects.JsonValueEncoder;
import org.apache.isis.viewer.restfulobjects.rendering.service.RepresentationServiceContentNegotiator;
import org.apache.isis.viewer.restfulobjects.rendering.service.acceptheader.AcceptHeaderServiceForRest;
import org.apache.isis.viewer.restfulobjects.rendering.service.conneg.ContentNegotiationServiceForRestfulObjectsV1_0;
import org.apache.isis.viewer.restfulobjects.rendering.service.conneg.ContentNegotiationServiceOrgApacheIsisV1;
import org.apache.isis.viewer.restfulobjects.rendering.service.conneg.ContentNegotiationServiceXRoDomainType;
import org.apache.isis.viewer.restfulobjects.rendering.service.swagger.SwaggerServiceDefault;
import org.apache.isis.viewer.restfulobjects.rendering.service.swagger.SwaggerServiceMenu;
import org.apache.isis.viewer.restfulobjects.rendering.service.swagger.internal.ClassExcluderDefault;
import org.apache.isis.viewer.restfulobjects.rendering.service.swagger.internal.SwaggerSpecGenerator;
import org.apache.isis.viewer.restfulobjects.rendering.service.swagger.internal.TaggerDefault;
import org.apache.isis.viewer.restfulobjects.rendering.service.swagger.internal.ValuePropertyFactoryDefault;

@Configuration
@Import({
        // modules
        IsisModuleRestfulObjectsApplib.class,
        IsisModuleRuntime.class,

        // @Component's
        ClassExcluderDefault.class,
        SwaggerSpecGenerator.class,
        TaggerDefault.class,
        ValuePropertyFactoryDefault.class,

        
        // @Service's
        AcceptHeaderServiceForRest.class,
        ContentNegotiationServiceForRestfulObjectsV1_0.class,
        ContentNegotiationServiceOrgApacheIsisV1.class,
        ContentNegotiationServiceXRoDomainType.class,
        JsonValueEncoder.class,
        RepresentationServiceContentNegotiator.class,
        SwaggerServiceDefault.class,
        SwaggerServiceMenu.class,

})
public class IsisModuleRestfulObjectsRendering {
}
