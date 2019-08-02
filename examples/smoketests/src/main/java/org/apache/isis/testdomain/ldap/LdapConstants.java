package org.apache.isis.testdomain.ldap;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class LdapConstants {

	/** IP port for the LDAP server to listen on */
    public static final int PORT = 10389;
	public static final String SVEN_PRINCIPAL = "cn=Sven Mojo,o=mojo";
	public static final String OLAF_PRINCIPAL = "cn=Olaf Mojo,o=mojo";
	
}
