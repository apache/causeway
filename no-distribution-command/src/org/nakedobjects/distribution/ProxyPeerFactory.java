package org.nakedobjects.distribution;

import org.nakedobjects.object.reflect.ActionPeer;
import org.nakedobjects.object.reflect.OneToManyPeer;
import org.nakedobjects.object.reflect.OneToOnePeer;
import org.nakedobjects.object.reflect.ReflectionPeerFactory;

import junit.framework.Assert;


public class ProxyPeerFactory implements ReflectionPeerFactory {
    private Distribution connection;
    private DataFactory objectDataFactory;

    public ActionPeer createAction(ActionPeer peer) {
        return new ProxyAction(peer, connection, objectDataFactory);
    }

    public OneToManyPeer createField(OneToManyPeer peer) {
        return new ProxyOneToManyAssociation(peer, connection, objectDataFactory);
    }

    public OneToOnePeer createField(OneToOnePeer peer) {
        return new ProxyOneToOneAssociation(peer, connection, objectDataFactory);
     }
    

    /**
     * .NET property
     * 
     * @property
     */
    public void set_Connection(Distribution connection) {
        this.connection = connection;
    }

    /**
     * .NET property
     * 
     * @property
     */
    public void set_ObjectDataFactory(DataFactory objectDataFactory) {
        this.objectDataFactory = objectDataFactory;
    }

    public void setConnection(Distribution connection) {
        this.connection = connection;
    }
    public void setObjectDataFactory(DataFactory objectDataFactory) {
        this.objectDataFactory = objectDataFactory;
    }

    public void init() {
        Assert.assertNotNull("Connection required", connection);
        Assert.assertNotNull("Data Factory required", objectDataFactory);
    }

    public void shutdown() {
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