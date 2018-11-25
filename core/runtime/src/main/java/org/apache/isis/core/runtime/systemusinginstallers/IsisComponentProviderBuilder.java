package org.apache.isis.core.runtime.systemusinginstallers;

import java.util.List;

import org.apache.isis.applib.AppManifest;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.core.commons.resource.ResourceStreamSource;
import org.apache.isis.core.commons.resource.ResourceStreamSourceContextLoaderClassPath;
import org.apache.isis.core.runtime.authentication.AuthenticationManager;
import org.apache.isis.core.runtime.authentication.standard.AuthenticationManagerStandard;
import org.apache.isis.core.runtime.authorization.AuthorizationManager;
import org.apache.isis.core.runtime.authorization.standard.AuthorizationManagerStandard;

import static org.apache.isis.commons.internal.base._With.computeIfAbsent;
import static org.apache.isis.config.internal._Config.acceptBuilder;

public class IsisComponentProviderBuilder {
    
    private AppManifest appManifest;
    private AuthenticationManager authenticationManager;
    private AuthorizationManager authorizationManager;
//    private final List<ResourceStreamSource> resourceStreamSources = _Lists.newArrayList();
    
    public IsisComponentProviderBuilder appManifest(AppManifest appManifest) {
        this.appManifest = appManifest;
        return this;
    }
    
    public IsisComponentProviderBuilder authenticationManager(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
        return this;
    }
    
    public IsisComponentProviderBuilder authorizationManager(AuthorizationManager authorizationManager) {
        this.authorizationManager = authorizationManager;
        return this;
    }
    
//    public IsisComponentProviderBuilder addResourceStreamSource(ResourceStreamSource source) {
//        resourceStreamSources.add(source);
//        return this;
//    }
    
    // -- SHORTCUTS
    
//    /**
//     * Default will read <tt>isis.properties</tt> (and other optional property files) from the 'config'
//     * package on the current classpath.
//     */
//    public IsisComponentProviderBuilder addConfigPackageAsResourceStreamSource() {
//        addResourceStreamSource(ResourceStreamSourceContextLoaderClassPath.create("config"));
//        return this;
//    }
    
    // -- BUILD
    
    public IsisComponentProvider build() {
        
        authenticationManager = computeIfAbsent(authenticationManager, 
                IsisComponentProviderBuilder::authenticationManagerWithBypass);
        
        authorizationManager = computeIfAbsent(authorizationManager, 
                AuthorizationManagerStandard::new);
        
//        acceptBuilder(builder->{
//            resourceStreamSources.forEach(builder::addResourceStreamSource);
//        });
        
        return new IsisComponentProvider(appManifest, authenticationManager, authorizationManager);
    }
    
    // -- HELPER
    
    
    /**
     * The standard authentication manager, configured with the 'bypass' authenticator 
     * (allows all requests through).
     * <p>
     * integration tests ignore appManifest for authentication and authorization.
     */
    private static AuthenticationManager authenticationManagerWithBypass() {
        final AuthenticationManagerStandard authenticationManager = new AuthenticationManagerStandard();
        authenticationManager.addAuthenticator(new AuthenticatorBypass());
        return authenticationManager;
    }
    
    
}
