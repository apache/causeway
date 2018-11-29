package org.apache.isis.core.runtime.systemusinginstallers;

import org.apache.isis.applib.AppManifest;
import org.apache.isis.core.runtime.authentication.AuthenticationManager;
import org.apache.isis.core.runtime.authentication.standard.AuthenticationManagerStandard;
import org.apache.isis.core.runtime.authorization.AuthorizationManager;
import org.apache.isis.core.runtime.authorization.standard.AuthorizationManagerStandard;

import static org.apache.isis.commons.internal.base._With.computeIfAbsent;

public class IsisComponentProviderBuilder {
    
    private AppManifest appManifest;
    private AuthenticationManager authenticationManager;
    private AuthorizationManager authorizationManager;
    
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
    
    // -- BUILD
    
    public IsisComponentProvider build() {
        
        authenticationManager = computeIfAbsent(authenticationManager, 
                IsisComponentProviderBuilder::authenticationManagerWithBypass);
        
        authorizationManager = computeIfAbsent(authorizationManager, 
                AuthorizationManagerStandard::new);
        
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
