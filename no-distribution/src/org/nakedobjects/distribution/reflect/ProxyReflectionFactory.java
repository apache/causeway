package org.nakedobjects.distribution.reflect;

import org.nakedobjects.object.ReflectionFactory;
import org.nakedobjects.object.reflect.ActionSpecification;
import org.nakedobjects.object.reflect.Action;
import org.nakedobjects.object.reflect.FieldSpecification;
import org.nakedobjects.object.reflect.OneToManyAssociationSpecification;
import org.nakedobjects.object.reflect.OneToManyAssociation;
import org.nakedobjects.object.reflect.OneToOneAssociationSpecification;
import org.nakedobjects.object.reflect.OneToOneAssociation;
import org.nakedobjects.object.reflect.ValueFieldSpecification;
import org.nakedobjects.object.reflect.ValueField;

import org.apache.log4j.Logger;


public class ProxyReflectionFactory implements ReflectionFactory  {
    private static final Logger LOG = Logger.getLogger(ProxyReflectionFactory.class);

    public ActionSpecification createAction(Action localDelegate) {
        Action fullDelegate = new RemoteAction(localDelegate);
        return new ActionSpecification(fullDelegate.getName(), fullDelegate);
    }

    public FieldSpecification createField(OneToManyAssociation local) {
        OneToManyAssociation oneToManyDelegate = new RemoteOneToManyAssociation(local);
        OneToManyAssociationSpecification association = new OneToManyAssociationSpecification(oneToManyDelegate.getName(), oneToManyDelegate.getType(),
                oneToManyDelegate);
        return association;
    }

    public FieldSpecification createField(OneToOneAssociation local) {
        OneToOneAssociation oneToOneDelegate = new RemoteOneToOneAssociation(local);
        OneToOneAssociationSpecification association = new OneToOneAssociationSpecification(oneToOneDelegate.getName(), oneToOneDelegate.getType(),
                oneToOneDelegate);
        return association;
    }

    public FieldSpecification createField(ValueField local) {
        ValueField valueDelegate = new RemoteValue(local);
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