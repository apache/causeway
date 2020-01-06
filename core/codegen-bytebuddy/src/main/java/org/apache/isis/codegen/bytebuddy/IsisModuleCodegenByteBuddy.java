package org.apache.isis.codegen.bytebuddy;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import org.apache.isis.codegen.bytebuddy.services.ProxyFactoryServiceByteBuddy;
import org.apache.isis.commons.IsisModuleCommons;

@Configuration
@Import({
        // modules
        IsisModuleCommons.class,
        
        // services
        ProxyFactoryServiceByteBuddy.class
})
public class IsisModuleCodegenByteBuddy {
}
