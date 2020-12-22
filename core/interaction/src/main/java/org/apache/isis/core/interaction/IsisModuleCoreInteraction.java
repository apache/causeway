package org.apache.isis.core.interaction;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import org.apache.isis.core.interaction.scope.IsisInteractionScopeBeanFactoryPostProcessor;

@Configuration
@Import({
    
    IsisInteractionScopeBeanFactoryPostProcessor.class
    
})
public class IsisModuleCoreInteraction {


}
