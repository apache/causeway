package org.nakedobjects.distribution;

import org.nakedobjects.object.InvalidEntryException;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.TextEntryParseException;
import org.nakedobjects.object.control.Hint;
import org.nakedobjects.object.reflect.OneToOnePeer;
import org.nakedobjects.object.security.ClientSession;
import org.nakedobjects.object.security.Session;

import org.apache.log4j.Logger;


public final class ProxyOneToOneAssociation implements OneToOnePeer {
    private final static Logger LOG = Logger.getLogger(ProxyOneToOneAssociation.class);
    private final ClientDistribution connection;
    private final boolean fullProxy = false;
    private OneToOnePeer local;

    public ProxyOneToOneAssociation(OneToOnePeer local, final ClientDistribution connection) {
        this.local = local;
        this.connection = connection;
    }

    public void clearAssociation(NakedObject inObject, NakedObject associate) {
        LOG.debug("remote clear association " + inObject + "/" + associate);
        if (isPersistent(inObject)) {
            connection.clearAssociation(ClientSession.getSession(), getName(), inObject.getOid(), inObject.getSpecification().getFullName(), associate.getOid(), associate.getSpecification().getFullName());
        } else {
            local.clearAssociation(inObject, associate);
        }
    }

    public Hint getHint(Session session, NakedObject inObject, NakedObject associate) {
        if (isPersistent(inObject) && fullProxy) {
            throw new NotExpectedException();
        } else {
            return local.getHint(session, inObject, associate);
        }
    }

    public NakedObject getAssociation(NakedObject inObject) {
        if (isPersistent(inObject) && fullProxy) {
          //  return connection.getOneToOneAssociation(ClientSession.getSession(), inObject);
            throw new NotExpectedException();
       } else {
            return local.getAssociation(inObject);
        }
    }

    public String getName() {
        return local.getName();
    }

    public NakedObjectSpecification getType() {
        return local.getType();
    }

    public boolean hasHint() {
        return local.hasHint();
    }

    public void setValue(NakedObject inObject, Object associate) {
        if (isPersistent(inObject)) {
            connection.setValue(ClientSession.getSession(), getName(), inObject.getOid(), inObject.getSpecification().getFullName(),  associate);
        } else {
	        local.setValue(inObject, associate);
        }
    }

    public void initValue(NakedObject inObject, Object associate) {
        local.initValue(inObject, associate);
    }

    public boolean isDerived() {
        return local.isDerived();
    }

    public boolean isEmpty(NakedObject inObject) {
        return local.isEmpty(inObject);
    }

    private boolean isPersistent(NakedObject inObject) {
        return inObject.getOid() != null;
    }

    public void parseTextEntry(NakedObject inObject, String text) throws TextEntryParseException, InvalidEntryException {
        local.parseTextEntry(inObject, text);
 //       Object associate = local.getAssociation(inObject).getObject();
        if (isPersistent(inObject)) {
            connection.setValue(ClientSession.getSession(), getName(), inObject.getOid(), inObject.getSpecification().getFullName(),  text);
        } 
    }

    public void setAssociation(NakedObject inObject, NakedObject associate) {
        LOG.debug("remote set association " + getName() + " in " + inObject + " with " + associate);
        if (isPersistent(inObject)) {
            connection.setAssociation(ClientSession.getSession(), getName(), inObject.getOid(), inObject.getSpecification().getFullName(), associate.getOid(), associate.getSpecification().getFullName());
        } else {
            local.setAssociation(inObject, associate);
        }
    }
    
    public void initAssociation(NakedObject inObject, NakedObject associate) {
        local.initAssociation(inObject, associate);
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