package org.apache.isis.viewer.json.tck;

import org.apache.isis.runtimes.dflt.webserver.WebServer;
import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

public class IsisWebServerRule implements MethodRule {

    private static ThreadLocal<WebServer> WEBSERVER = new ThreadLocal<WebServer>() {
        protected WebServer initialValue() {
            WebServer webServer = new WebServer();
            webServer.run(39393);
            return webServer;
        };
    }; 
    
    @Override
    public Statement apply(Statement base, FrameworkMethod method, Object target) {
        getWebServer(); // creates and starts running if required
        return base;
    }
    
    public WebServer getWebServer() {
        return WEBSERVER.get();
    }

}
