package org.apache.isis.webapp;

import org.apache.isis.webapp.modules.h2console.H2ManagerMenu;
import org.apache.isis.webapp.modules.h2console.WebModuleH2Console;
import org.apache.isis.webapp.modules.logonlog.WebModuleLogOnExceptionLogger;
import org.apache.isis.webapp.modules.resources.WebModuleStaticResources;
import org.apache.isis.webapp.modules.sse.WebModuleServerSentEvents;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
	IsisWebAppContextListener.class,
    IsisWebAppContextInitializer.class,
    
    // default modules
    WebModuleLogOnExceptionLogger.class,
    WebModuleStaticResources.class,
    WebModuleServerSentEvents.class,
    
    // h2 console
    WebModuleH2Console.class,
    H2ManagerMenu.class,
    
})
public class IsisBootWebApp {

}
