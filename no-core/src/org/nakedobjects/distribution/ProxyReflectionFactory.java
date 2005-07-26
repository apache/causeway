package org.nakedobjects.distribution;

import org.nakedobjects.object.ReflectionFactory;
import org.nakedobjects.object.help.HelpLookup;
import org.nakedobjects.object.help.HelpManager;
import org.nakedobjects.object.help.OneToOneHelp;
import org.nakedobjects.object.reflect.Action;
import org.nakedobjects.object.reflect.ActionPeer;
import org.nakedobjects.object.reflect.NakedObjectField;
import org.nakedobjects.object.reflect.OneToManyAssociation;
import org.nakedobjects.object.reflect.OneToManyPeer;
import org.nakedobjects.object.reflect.OneToOneAssociation;
import org.nakedobjects.object.reflect.OneToOnePeer;


public class ProxyReflectionFactory implements ReflectionFactory {
    private ClientDistribution connection;

    private HelpLookup helpLookup = new HelpLookup(null);
    private DataFactory objectDataFactory;

    public ProxyReflectionFactory() {}

    public Action createAction(String className, ActionPeer localDelegate) {
        ActionPeer fullDelegate = new ProxyAction(localDelegate, connection, objectDataFactory);
        return new Action(className, fullDelegate.getName(), fullDelegate);
    }

    public NakedObjectField createField(String className, OneToManyPeer local) {
        OneToManyPeer oneToManyDelegate = new ProxyOneToManyAssociation(local, connection);
        OneToManyAssociation association = new OneToManyAssociation(className, oneToManyDelegate.getName(), oneToManyDelegate
                .getType(), oneToManyDelegate);
        return association;
    }

    public NakedObjectField createField(String className, OneToOnePeer local) {

        OneToOnePeer oneToOneDelegate = new ProxyOneToOneAssociation(local, connection);
        oneToOneDelegate = new OneToOneHelp(oneToOneDelegate, helpLookup);
        OneToOneAssociation association = new OneToOneAssociation(className, oneToOneDelegate.getName(), oneToOneDelegate
                .getType(), oneToOneDelegate);
        return association;
    }

    public void init() {}

    /**
     * .NET property
     * 
     * @property
     */
    public void set_Connection(ClientDistribution connection) {
        this.connection = connection;
    }

    /**
     * .NET property
     * 
     * @property
     */
    public void set_HelpManager(HelpManager helpManager) {
        setHelpManager(helpManager);
    }

    /**
     * .NET property
     * 
     * @property
     */
    public void set_ObjectDataFactory(DataFactory objectDataFactory) {
        this.objectDataFactory = objectDataFactory;
    }

    public void setConnection(ClientDistribution connection) {
        this.connection = connection;
    }

    public void setHelpManager(HelpManager helpManager) {
        this.helpLookup = new HelpLookup(helpManager);
    }

    public void setObjectDataFactory(DataFactory objectDataFactory) {
        this.objectDataFactory = objectDataFactory;
    }

    public void shutdown() {}
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business objects directly to the
 * user. Copyright (C) 2000 - 2005 Naked Objects Group Ltd
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