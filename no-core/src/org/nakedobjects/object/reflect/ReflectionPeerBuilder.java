package org.nakedobjects.object.reflect;

import org.nakedobjects.object.Action;
import org.nakedobjects.object.NakedObjectField;


public class ReflectionPeerBuilder {
    private ReflectionPeerFactory[] factories;

    public void setFactories(ReflectionPeerFactory[] factories) {
        this.factories = factories;
    }

    /**
     * Expose as a .Net property.
     * 
     * @property
     */
    public void set_Factories(ReflectionPeerFactory[] factories) {
        this.factories = factories;
    }

    public Action createAction(String className, ActionPeer actionPeer) {
        ActionPeer peer = actionPeer;
        for (int i = 0; i < factories.length; i++) {
            peer = factories[i].createAction(peer);
        }
        return new ActionImpl(className, peer.getName(), peer);
    }

    public NakedObjectField createField(String className, OneToManyPeer fieldPeer) {
        OneToManyPeer peer = fieldPeer;
        for (int i = 0; i < factories.length; i++) {
            peer = factories[i].createField(peer);
        }
        return new OneToManyAssociationImpl(className, peer.getName(), peer.getType(), peer);
    }

    public NakedObjectField createField(String className, OneToOnePeer fieldPeer) {
        OneToOnePeer peer = fieldPeer;
        for (int i = 0; i < factories.length; i++) {
            peer = factories[i].createField(peer);
        }
        return new OneToOneAssociationImpl(className, peer.getName(), peer.getType(), peer);
    }
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business objects directly to the user.
 * Copyright (C) 2000 - 2005 Naked Objects Group Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation; either version 2 of the License, or (at your
 * option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to
 * the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address of Naked Objects Group is
 * Kingsway House, 123 Goldworth Road, Woking GU21 1NR, UK).
 */