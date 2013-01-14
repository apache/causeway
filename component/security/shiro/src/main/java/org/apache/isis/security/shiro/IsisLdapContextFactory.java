package org.apache.isis.security.shiro;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.ldap.LdapContext;

import org.apache.shiro.realm.ldap.JndiLdapContextFactory;

/**
 * An extension of {@link JndiLdapContextFactory} that allows a different authentication mechanism
 * for system-level authentications (as used by authorization lookups, for example)
 * compared to regular authentication.
 * 
 * <p>
 * See {@link IsisLdapRealm} for typical configuration within <tt>shiro.ini</tt>.
 */
public class IsisLdapContextFactory extends JndiLdapContextFactory {

    private String systemAuthenticationMechanism;

    /**
     * HACK
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    protected LdapContext createLdapContext(Hashtable env) throws NamingException {
        if(getSystemUsername() != null && getSystemUsername().equals(env.get(Context.SECURITY_PRINCIPAL))) {
            env.put(Context.SECURITY_AUTHENTICATION, getSystemAuthenticationMechanism());
        }
        return super.createLdapContext(env);
    }

    public String getSystemAuthenticationMechanism() {
        return systemAuthenticationMechanism != null? systemAuthenticationMechanism: getAuthenticationMechanism();
    }
    public void setSystemAuthenticationMechanism(String systemAuthenticationMechanism) {
        this.systemAuthenticationMechanism = systemAuthenticationMechanism;
    }
    
    
}
