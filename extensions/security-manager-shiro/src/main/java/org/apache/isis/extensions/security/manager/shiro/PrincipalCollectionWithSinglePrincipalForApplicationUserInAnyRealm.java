package org.apache.isis.extensions.security.manager.shiro;

import java.util.Collection;

import org.apache.shiro.subject.SimplePrincipalCollection;

@SuppressWarnings("rawtypes")
public class PrincipalCollectionWithSinglePrincipalForApplicationUserInAnyRealm extends SimplePrincipalCollection {

    private static final long serialVersionUID = 1L;

    @SuppressWarnings("unchecked")
    @Override
    public void add(Object principal, String realmName) {
        if (realmName == null) {
            throw new IllegalArgumentException("realmName argument cannot be null.");
        }
        if (principal == null) {
            throw new IllegalArgumentException("principal argument cannot be null.");
        }
        final Collection principalsLazy = getPrincipalsLazy(realmName);
        if(principal instanceof PrincipalForApplicationUser) {
            principalsLazy.clear();
        }
        principalsLazy.add(principal);
    }

    @Override
    public void addAll(Collection principals, String realmName) {
        for (Object principal : principals) {
            add(principal, realmName);
        }
    }

}
