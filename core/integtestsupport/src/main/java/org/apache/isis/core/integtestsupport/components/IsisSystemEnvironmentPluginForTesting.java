package org.apache.isis.core.integtestsupport.components;

import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.plugins.environment.DeploymentType;
import org.apache.isis.core.plugins.environment.IsisSystemEnvironment;
import org.apache.isis.core.plugins.environment.IsisSystemEnvironmentPlugin;

/**
 * To enable this plugin add a META-INF entry to your application as specified by the 
 * Java 7 ServiceLoader mechanism.
 *  
 * @since 2.0.0-M2
 */
public class IsisSystemEnvironmentPluginForTesting implements IsisSystemEnvironmentPlugin {

    @Override
    public IsisSystemEnvironment getIsisSystemEnvironment() {
        
        final IsisConfiguration config = getConfiguration();
        
        final String deploymentTypeLiteral = config.getString("isis.deploymentType");
        if(_Strings.isNullOrEmpty(deploymentTypeLiteral)) {
            return IsisSystemEnvironment.getDefault();
        }
        
        // at this point, the deploymentType seem explicitly set via config, so we override any 
        // environment variables that might be present
        
        // throws if type can not be parsed
        final DeploymentType deploymentType = DeploymentType.valueOf(deploymentTypeLiteral);
        
        return IsisSystemEnvironment.of(deploymentType);
        
    }
    
    // HELPER

    private IsisConfiguration getConfiguration() {
        IsisConfiguration configuration = IsisConfiguration.loadDefault();
        return configuration;
    }

}
