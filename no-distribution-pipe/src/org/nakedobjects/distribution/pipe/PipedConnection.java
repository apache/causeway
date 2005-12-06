package org.nakedobjects.distribution.pipe;

import org.nakedobjects.distribution.command.Request;
import org.nakedobjects.distribution.command.Response;

import org.apache.log4j.Logger;


public class PipedConnection {
    private static final Logger LOG = Logger.getLogger(PipedConnection.class);
    private Request request;
    private Response response;
    private RuntimeException exception;

    public synchronized void setRequest(Request request) {
        this.request = request;
        notify();
    }

    public synchronized Request getRequest() {
        while (request == null) {
            try {
                wait();
            } catch (InterruptedException e) {
                LOG.error("wait (getRequest) interrupted", e);
            }
        }

        Request r = request;
        request = null;
        notify();
        return r;
    }

    public synchronized void setResponse(Response response) {
        this.response = response;
        notify();
    }
    
    public synchronized void setException(RuntimeException exception) {
        this.exception = exception;
        notify();
    }

    public synchronized Response getResponse() {
        while (response == null && exception == null) {
            try {
                wait();
            } catch (InterruptedException e) {
                LOG.error("wait (getResponse) interrupted", e);
            }
        }

        if(exception != null) {
            RuntimeException toThrow = exception;
            exception = null;
            throw toThrow;
        }
        
        Response r = response;
        response = null;
        notify();
        return r;
    }

}

/*
 * Naked Objects - a framework that exposes behaviourally complete business
 * objects directly to the user. Copyright (C) 2000 - 2005 Naked Objects Group
 * Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address
 * of Naked Objects Group is Kingsway House, 123 Goldworth Road, Woking GU21
 * 1NR, UK).
 */