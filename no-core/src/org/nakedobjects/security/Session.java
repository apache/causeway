package org.nakedobjects.security;

import org.apache.log4j.Logger;

public class Session {
	private static final Logger LOG = Logger.getLogger(Session.class);
	private static final SecurityContext defaultContext = new SecurityContext();
	private static Session session;  // for each client JVm there is one user
	private SecurityContext securityContext;
	
	public static Session getSession() {
		if(session == null) {
			throw new NakedObjectSecurityException("No existing session");
		}
		return session;
	}
	
	public static void initSession() {
		if(session != null) {
			throw new NakedObjectSecurityException("Already existing session");
		}
		session = new Session(defaultContext);
	}
	
	public static void setLoggedOn(SecurityContext securityContext) {
		if(session.securityContext != defaultContext) {
			throw new NakedObjectSecurityException("Cannot logon when already logged on");
		}
		session.securityContext = securityContext;
		
		LOG.info("User logged on " + session.securityContext);
	}
	
	private Session(SecurityContext securityContext) {
		this.securityContext = securityContext;
	}
	
	public SecurityContext getSecurityContext() {
		return securityContext;
	}
	
	public static void logoff() {
		LOG.info("User logged off " + session.securityContext);
		session.securityContext = defaultContext;
	}

	public static boolean isLoggedOn() {
		return session.securityContext != null;
	}
	
	public void shutdown() {
	    session = null;
	}
}

/*
Naked Objects - a framework that exposes behaviourally complete
business objects directly to the user.
Copyright (C) 2000 - 2003  Naked Objects Group Ltd

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

The authors can be contacted via www.nakedobjects.org (the
registered address of Naked Objects Group is Kingsway House, 123 Goldworth
Road, Woking GU21 1NR, UK).
*/
