package org.nakedobjects.distribution.client;

import org.nakedobjects.distribution.DistributionInterface;
import org.nakedobjects.distribution.RemoteObjectFactory;
import org.nakedobjects.distribution.SessionId;
import org.nakedobjects.distribution.client.reflect.RemoteAction;
import org.nakedobjects.distribution.client.reflect.RemoteOneToManyAssociation;
import org.nakedobjects.distribution.client.reflect.RemoteOneToOneAssociation;
import org.nakedobjects.distribution.client.reflect.RemoteValue;
import org.nakedobjects.object.LoadedObjects;
import org.nakedobjects.object.ReflectionFactory;
import org.nakedobjects.object.reflect.Action;
import org.nakedobjects.object.reflect.ActionSpecification;
import org.nakedobjects.object.reflect.FieldSpecification;
import org.nakedobjects.object.reflect.OneToManyAssociation;
import org.nakedobjects.object.reflect.OneToManyAssociationSpecification;
import org.nakedobjects.object.reflect.OneToOneAssociation;
import org.nakedobjects.object.reflect.OneToOneAssociationSpecification;
import org.nakedobjects.object.reflect.ValueField;
import org.nakedobjects.object.reflect.ValueFieldSpecification;


public class ProxyReflectionFactory implements ReflectionFactory {
    private final DistributionInterface connection;
    private final RemoteObjectFactory factory;
    private final SessionId sessionId;
    private final LoadedObjects loadedObjects;

    public ProxyReflectionFactory(SessionId sessionId, RemoteObjectFactory factory, DistributionInterface connection, LoadedObjects loadedObjects) {
        this.sessionId =sessionId;
        this.factory = factory;
        this.connection = connection;
    	this.loadedObjects = loadedObjects;
    }

    public ActionSpecification createAction(Action localDelegate) {
        Action fullDelegate = new RemoteAction(localDelegate, sessionId, factory, connection, loadedObjects);
        return new ActionSpecification(fullDelegate.getName(), fullDelegate);
    }

    public FieldSpecification createField(OneToManyAssociation local) {
        OneToManyAssociation oneToManyDelegate = new RemoteOneToManyAssociation(local, sessionId, factory, connection);
        OneToManyAssociationSpecification association = new OneToManyAssociationSpecification(oneToManyDelegate.getName(), oneToManyDelegate.getType(),
                oneToManyDelegate);
        return association;
    }

    public FieldSpecification createField(OneToOneAssociation local) {
        OneToOneAssociation oneToOneDelegate = new RemoteOneToOneAssociation(local, sessionId, factory, connection, loadedObjects);
        OneToOneAssociationSpecification association = new OneToOneAssociationSpecification(oneToOneDelegate.getName(), oneToOneDelegate.getType(),
                oneToOneDelegate);
        return association;
    }

    public FieldSpecification createField(ValueField local) {
        ValueField valueDelegate = new RemoteValue(local, sessionId, factory, connection);
        return new ValueFieldSpecification(valueDelegate.getName(), valueDelegate.getType(), valueDelegate);
    }

}

/*
 * Naked Objects - a framework that exposes behaviourally complete business
 * objects directly to the user. Copyright (C) 2000 - 2004 Naked Objects Group
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