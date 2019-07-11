package org.apache.isis.extensions.secman.shiro;

import java.util.Collection;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.UnavailableSecurityManagerException;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.mgt.RealmSecurityManager;
import org.apache.shiro.realm.Realm;

final class ShiroUtils {

    private ShiroUtils() {
    }

    public static synchronized RealmSecurityManager getSecurityManager() {
        org.apache.shiro.mgt.SecurityManager securityManager;
        try {
            securityManager = SecurityUtils.getSecurityManager();
        } catch(UnavailableSecurityManagerException ex) {
            throw new AuthenticationException(ex);
        }
        if(!(securityManager instanceof RealmSecurityManager)) {
            throw new AuthenticationException();
        }
        return (RealmSecurityManager) securityManager;
    }

    public static IsisModuleSecurityRealm getIsisModuleSecurityRealm() {
        final RealmSecurityManager securityManager = getSecurityManager();
        final Collection<Realm> realms = securityManager.getRealms();
        for (Realm realm : realms) {
            if(realm instanceof IsisModuleSecurityRealm) {
                IsisModuleSecurityRealm imsr = (IsisModuleSecurityRealm) realm;
                return imsr;
            }
        }
        return null;
    }
    
}
