package org.nakedobjects.distribution.reflect;

import org.nakedobjects.object.NakedClassLoader;
import org.nakedobjects.object.reflect.Action;
import org.nakedobjects.object.reflect.ActionDelegate;
import org.nakedobjects.object.reflect.Field;
import org.nakedobjects.object.reflect.OneToManyAssociation;
import org.nakedobjects.object.reflect.OneToManyAssociationIF;
import org.nakedobjects.object.reflect.OneToOneAssociation;
import org.nakedobjects.object.reflect.OneToOneAssociationIF;
import org.nakedobjects.object.reflect.Value;
import org.nakedobjects.object.reflect.ValueIf;

import org.apache.log4j.Logger;


class ProxyClassLoader extends NakedClassLoader {
    private static final Logger LOG = Logger.getLogger(ProxyClassLoader.class);

    protected Action createAction(ActionDelegate localDelegate) {
        ActionDelegate fullDelegate = new RemoteAction(localDelegate);
        return new Action(fullDelegate.getName(), fullDelegate);
    }

    protected Field createField(OneToManyAssociationIF local) {
        OneToManyAssociationIF oneToManyDelegate = new RemoteOneToManyAssociation(local);
        OneToManyAssociation association = new OneToManyAssociation(oneToManyDelegate.getName(), oneToManyDelegate.getType(),
                oneToManyDelegate);
        return association;
    }

    protected Field createField(OneToOneAssociationIF local) {
        OneToOneAssociationIF oneToOneDelegate = new RemoteOneToOneAssociation(local);
        OneToOneAssociation association = new OneToOneAssociation(oneToOneDelegate.getName(), oneToOneDelegate.getType(),
                oneToOneDelegate);
        return association;
    }

    protected Field createField(ValueIf local) {
        ValueIf valueDelegate = new RemoteValue(local);
        return new Value(valueDelegate.getName(), valueDelegate.getType(), valueDelegate);
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