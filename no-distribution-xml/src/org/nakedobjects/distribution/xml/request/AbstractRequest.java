package org.nakedobjects.distribution.xml.request;

import org.nakedobjects.distribution.xml.Request;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.reflect.PojoAdapter;
import org.nakedobjects.object.security.Session;


public abstract class AbstractRequest implements Request {
    protected Object response;
    private static int nextId = 0;
    protected final int id = nextId++;
    protected final Session session;

    public AbstractRequest(final Session session) {
        this.session = session;
    }

    protected PojoAdapter getNakedObject(ObjectTransferCarrier object) {
        return PojoAdapter.createAdapter(object.getObject(), object.getOid());
    }

    protected Naked[] getNakedObjects(ObjectTransferCarrier[] parameters) {
        Naked parametersOut[] = new Naked[parameters.length];
        for (int i = 0; i < parametersOut.length; i++) {
            parametersOut[i] = PojoAdapter.createAdapter(parameters[i].getObject(), parameters[i].getOid());
        }
        return parametersOut;
    }

    protected ObjectTransferCarrier  getObject(NakedObject object) {
        return new ObjectTransferCarrier(object.getObject(), object.getOid());
    }

    protected ObjectTransferCarrier[] getObjects(Naked[] parameters) {
        ObjectTransferCarrier parametersOut[] = new ObjectTransferCarrier[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            parametersOut[i] = getObject((NakedObject) parameters[i]);
        }
        return parametersOut;
    }

    public final void setResponse(Object response) {
        this.response = response;
    }

    public Object getResponse() {
        return response;
    }
    
    public int getId() {
        return id;
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