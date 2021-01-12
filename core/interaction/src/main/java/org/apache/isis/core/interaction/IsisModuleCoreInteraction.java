package org.apache.isis.core.interaction;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import org.apache.isis.core.interaction.integration.InteractionAwareTransactionalBoundaryHandler;
import org.apache.isis.core.interaction.scope.InteractionScopeBeanFactoryPostProcessor;

@Configuration
@Import({
    
    InteractionScopeBeanFactoryPostProcessor.class,
    InteractionAwareTransactionalBoundaryHandler.class
    
})
public class IsisModuleCoreInteraction {


}
