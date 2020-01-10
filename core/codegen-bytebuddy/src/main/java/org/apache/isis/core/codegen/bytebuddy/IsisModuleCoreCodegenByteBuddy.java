package org.apache.isis.core.codegen.bytebuddy;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import org.apache.isis.core.codegen.bytebuddy.services.ProxyFactoryServiceByteBuddy;
import org.apache.isis.core.commons.IsisModuleCoreCommons;

@Configuration
@Import({
        // modules
        IsisModuleCoreCommons.class,
        
        // services
        ProxyFactoryServiceByteBuddy.class
})
public class IsisModuleCoreCodegenByteBuddy {
}
