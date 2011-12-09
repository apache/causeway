package org.apache.isis.viewer.junit.internal;

import org.apache.isis.core.runtime.authentication.AuthenticationManagerInstaller;
import org.apache.isis.core.runtime.authorization.AuthorizationManagerInstaller;
import org.apache.isis.runtimes.dflt.runtime.installerregistry.installerapi.PersistenceMechanismInstaller;
import org.apache.isis.security.dflt.authentication.NoopAuthenticationManagerInstaller;
import org.apache.isis.security.dflt.authorization.NoopAuthorizationManagerInstaller;
import org.apache.isis.viewer.junit.Authenticator;
import org.apache.isis.viewer.junit.Authorizer;
import org.apache.isis.viewer.junit.Persistor;

public class AnnotationInstaller {

    /**
     * Should be called prior to installing; typically called immediately after instantiation.
     * 
     * <p>
     * Note: an alternative design would be to have a 1-arg constructor, but the convention for installers is to make
     * them no-arg.
     */
    // {{ AuthenticationManagerInstaller
    public AuthenticationManagerInstaller addAuthenticatorAnnotatedOn(final Class<?> javaClass)
        throws InstantiationException, IllegalAccessException {
        final Authenticator authenticatorAnnotation = javaClass.getAnnotation(Authenticator.class);
        if (authenticatorAnnotation != null) {
            return addAuthenticatorRepresentedBy(authenticatorAnnotation);
        } else {
            return new NoopAuthenticationManagerInstaller();
        }

    }

    private AuthenticationManagerInstaller addAuthenticatorRepresentedBy(final Authenticator authenticatorAnnotation)
        throws InstantiationException, IllegalAccessException {
        final Class<?> fixtureClass = authenticatorAnnotation.value();
        return (AuthenticationManagerInstaller) fixtureClass.newInstance();
    }

    // }}

    // {{ AuthorizationManagerInstaller
    public AuthorizationManagerInstaller addAuthorizerAnnotatedOn(final Class<?> javaClass)
        throws InstantiationException, IllegalAccessException {
        final Authorizer authorizorAnnotation = javaClass.getAnnotation(Authorizer.class);
        if (authorizorAnnotation != null) {
            return addAuthorizerRepresentedBy(authorizorAnnotation);
        } else {
            return new NoopAuthorizationManagerInstaller();
        }

    }

    private AuthorizationManagerInstaller addAuthorizerRepresentedBy(final Authorizer authorizorAnnotation)
        throws InstantiationException, IllegalAccessException {
        final Class<?> fixtureClass = authorizorAnnotation.value();
        return (AuthorizationManagerInstaller) fixtureClass.newInstance();
    }

    // }}

    // {{ PersistenceMechanismInstaller
    public PersistenceMechanismInstaller addPersistorAnnotatedOn(final Class<?> javaClass)
        throws InstantiationException, IllegalAccessException {
        final Persistor annotation = javaClass.getAnnotation(Persistor.class);
        if (annotation != null) {
            return addPersistorRepresentedBy(annotation);
        } else {
            return new InMemoryPersistenceMechanismInstallerWithinJunit();
        }

    }

    private PersistenceMechanismInstaller addPersistorRepresentedBy(final Persistor annotation)
        throws InstantiationException, IllegalAccessException {
        final Class<?> fixtureClass = annotation.value();
        return (PersistenceMechanismInstaller) fixtureClass.newInstance();
    }
    // }}

    // new InMemoryUserProfileStoreInstaller();

}
