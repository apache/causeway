
package org.nakedobjects.distribution.broadcast;

import org.nakedobjects.container.configuration.ComponentException;
import org.nakedobjects.distribution.ObjectUpdateMessage;
import org.nakedobjects.distribution.RequestContext;
import org.nakedobjects.object.UpdateNotifier;

import java.io.IOException;

import org.apache.log4j.Logger;


public class ServerEndPoint implements ConnectionFromClient {
    private final static Logger LOG = Logger.getLogger(ServerEndPoint.class);
    public static final String OBJECT_MANAGER = "object-store";
	private UpdateBroadcaster notifier;
    private RequestContext server;
    private Listener fileListener;
    private Listener requestListener;
	private Parameters params;

     public void shutdown() {
        LOG.info("Object Server shutting down " + new java.util.Date());

        if (requestListener != null) {
            requestListener.shutdown();
        }

        if (fileListener != null) {
            fileListener.shutdown();
        }
    }

    public UpdateNotifier getNotifier() {
        // TODO Auto-generated method stub
        return null;
    }

    public void init(RequestContext server) throws ComponentException {
        this.server = server;
        params = new Parameters();
        String host = params.getHost();
        
        LOG.info("Request settings: host=" + ((host == null) ? "localhost" : host) +
            ", request-port: " + params.getRequestPort() + ", file-port=" + params.getFilePort());
        LOG.info("Multicast settings: group=" + params.getUpdateAddress() + ", port=" + 
        		params.getUpdatePort() + ", ttl=" + params.getUpdateTtl());

        try {
	        requestListener = new RequestListener(host, params.getRequestPort(), server);
	        fileListener = new FileListener(host, params.getFilePort(), server);
	
	        notifier = new UpdateBroadcaster(params.getUpdateAddress(), 
	        		params.getUpdatePort(), (byte) params.getUpdateTtl(), 
					params.getUpdatePackageSize());
	
	        requestListener.start();
	        fileListener.start();
	
        } catch(IOException e) {
        	LOG.error("Initialization error " + e);
        	throw new ComponentException("Failed to initialize all connections");
        }
    }

    public String toString() {
    	StringBuffer str = new StringBuffer();
    	String host = params.getHost();
    	str.append("ServerEndPoint [requests=");
    	str.append(host == null ? "localhost" : host);
    	str.append(":");
    	str.append(params.getRequestPort());
    	str.append("/");
    	str.append(params.getFilePort());
    	str.append(",updates=");
    	str.append(params.getUpdateAddress());
    	str.append(":");
    	str.append(params.getUpdatePort());
    	str.append("]");
    	return  str.toString();
	}

	public void broadcast(ObjectUpdateMessage msg) {
		notifier.broadcast(msg);
	}
}



/*
Naked Objects - a framework that exposes behaviourally complete
business objects directly to the user.
Copyright (C) 2000 - 2005  Naked Objects Group Ltd

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
