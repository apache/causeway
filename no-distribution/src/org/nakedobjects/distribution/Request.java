package org.nakedobjects.distribution;

import org.apache.log4j.Logger;
import org.nakedobjects.object.LoadedObjects;

import java.io.Serializable;


public abstract class Request implements Serializable {
	private final static Logger LOG = Logger.getLogger(Request.class);
	private static int nextId = 0;
	private static DistributionInterface connection;
	private static ProxyObjectManager proxyObjectManager;
    
    public static void init(DistributionInterface conn) {
    	Request.connection = conn;
    }
    
    public static void init(ProxyObjectManager proxyObjectManager) {
        Request.proxyObjectManager = proxyObjectManager;
    }
    
    protected final int id;
    protected Serializable response = null;

    protected Request() {
        id = nextId++;
    }

    public void execute() {
        sendRequest();
    }

    protected abstract void generateResponse(RequestContext context);

    protected Serializable getResponse() {
        return response;
    }
    
    protected void sendRequest() {
    	LOG.debug("send request " + this);
    	response = connection.execute(this);
    	LOG.debug("response " + (response == null ? "EMPTY RESPONSE" : response) + ", for request " + this);
    }
    
    protected LoadedObjects getLoadedObjects() {
        return proxyObjectManager.getLoadedObjects();
    }
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business objects directly to the
 * user. Copyright (C) 2000 - 2003 Naked Objects Group Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 * 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address of Naked Objects
 * Group is Kingsway House, 123 Goldworth Road, Woking GU21 1NR, UK).
 */
