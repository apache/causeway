package org.nakedobjects.object.collection;

import org.nakedobjects.object.NakedClass;
import org.nakedobjects.object.NakedClassManager;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectManager;
import org.nakedobjects.object.Title;
import org.nakedobjects.object.UnsupportedFindException;
import org.nakedobjects.object.control.Permission;
import org.nakedobjects.object.control.Veto;
import org.nakedobjects.security.Role;

import java.util.Vector;


public class InstanceCollection extends TypedCollection {
    /*
     * This is temporary to make the debuging work - needs createFinder and
     * acquireInstance in NakedClass TODO remove this
     */
    public InstanceCollection() {
        this(Role.class.getName());
    }

    /** @deprecated */
    public InstanceCollection(NakedClass cls) {
        super(cls.fullName());
        fillAllInstances(cls);
    }

    public InstanceCollection(NakedClass cls, Vector elements) {
        super(cls.fullName());
        this.elements = elements;
    }

    /** @deprecated */
    public InstanceCollection(NakedObject pattern) {
        super(pattern.getClass());
        fillSelectedInstances(pattern);
    }

    private InstanceCollection(NakedClass cls, NakedClass cls2) {
        super(cls.fullName());
    }
 
    /** @deprecated */
    public InstanceCollection(String cls) {
        super(cls);
        NakedClass pattern = NakedClassManager.getInstance().getNakedClass(cls);
        fillAllInstances(pattern);
    }
    
    public static InstanceCollection allInstances(NakedClass cls) {
    	return new InstanceCollection(cls);
    }

    public static InstanceCollection allInstances(String cls) {
    	return new InstanceCollection(cls);
    }

    public static InstanceCollection findInstances(NakedClass cls, String term) {
    	Vector elements;
    	try {
    		elements =  NakedObjectManager.getInstance().getInstances(cls, term);
    	} catch (UnsupportedFindException e) {
    		LOG.warn("Fast find not supported " + term);
    		elements = new Vector();
    	}
    	InstanceCollection instances = new InstanceCollection(cls, elements);
    	return instances;
    }

    public static InstanceCollection findInstances(NakedObject pattern) {
    	return new InstanceCollection(pattern);
    }


    /**
     * Returns a veto. The user should not be able to add instannces to the set
     * of instances
     */
    public Permission canAdd(NakedObject object) {
        return Veto.DEFAULT;
    }

    /**
     * Returns a veto. The user should not be able to remove instannces to the
     * set of instances
     */
    public Permission canRemove(NakedObject object) {
        return Veto.DEFAULT;
    }

    private void fillAllInstances(NakedClass cls) {
        elements = NakedObjectManager.getInstance().getInstances(cls);
    }

    private void fillSelectedInstances(NakedObject pattern) {
        try {
            // this.pattern = pattern;
            if (!pattern.isFinder()) { throw new IllegalArgumentException("pattern must be a finder object: " + pattern); }
            elements = NakedObjectManager.getInstance().getInstances(pattern);
        } catch (UnsupportedFindException e) {
            LOG.warn("Finder not supported " + pattern);
            elements = new Vector();
        }
    }

    /**
     * The instances collections are always shown as persistent as they are
     * based on the ObjectStore, which is used to persist objects, although the
     * collection does not exist in its own right on the store.
     */
    public boolean isPersistent() {
        return true;
    }

    public void resolve() {}

    public Title title() {
        return new Title(NakedClassManager.getInstance().getNakedClass(getType().getName()).getPluralName()).append("(" + size()
                + ")");
    }
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business
 * objects directly to the user. Copyright (C) 2000 - 2003 Naked Objects Group
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