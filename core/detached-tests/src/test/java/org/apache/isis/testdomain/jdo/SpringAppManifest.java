package org.apache.isis.testdomain.jdo;

import org.apache.isis.applib.AppManifestAbstract2;
import org.apache.isis.config.AppConfig;
import org.apache.isis.config.IsisConfiguration;
import org.apache.isis.core.runtime.threadpool.ThreadPoolExecutionMode;
import org.apache.isis.core.runtime.threadpool.ThreadPoolSupport;

/**
 * Bootstrap the application.
 */
public class SpringAppManifest extends AppManifestAbstract2 implements AppConfig {

    public static final Builder BUILDER = Builder
            .forModule(new JdoTestDomainModule())
            .withConfigurationPropertiesFile(
                    SpringAppManifest.class, "isis-non-changing.properties")
            .withAuthMechanism("shiro")
            ;

    public SpringAppManifest() {
        super(BUILDER);
        
        ThreadPoolSupport.HIGHEST_CONCURRENCY_EXECUTION_MODE_ALLOWED = 
                ThreadPoolExecutionMode.SEQUENTIAL_WITHIN_CALLING_THREAD;
        
    }

    // Implementing AppConfig, to tell the framework how to bootstrap the configuration.
    @Override
    public IsisConfiguration isisConfiguration() {
        return IsisConfiguration.buildFromAppManifest(this);
    }
    
    
}
