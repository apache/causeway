package org.apache.isis.core.webapp;

import org.apache.isis.core.webapp.modules.logonlog.WebModuleLogOnExceptionLogger;
import org.apache.isis.core.webapp.modules.resources.WebModuleStaticResources;
import org.apache.isis.core.webapp.modules.sse.WebModuleServerSentEvents;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
	IsisWebAppContextListener.class,
    IsisWebAppContextInitializer.class,
    
    //default modules
    WebModuleLogOnExceptionLogger.class,
    WebModuleStaticResources.class,
    WebModuleServerSentEvents.class,
})
public class IsisBootWebApp {

}
