package org.nakedobjects.object.defaults;

import org.nakedobjects.container.configuration.Configuration;
import org.nakedobjects.object.NakedClass;
import org.nakedobjects.object.NakedCollection;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectManager;
import org.nakedobjects.object.NakedObjectRuntimeException;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.NakedObjectSpecificationLoader;
import org.nakedobjects.object.NotPersistableException;
import org.nakedobjects.object.ObjectStoreException;
import org.nakedobjects.object.control.ActionAbout;
import org.nakedobjects.object.control.ClassAbout;
import org.nakedobjects.object.defaults.value.TextString;

import org.apache.log4j.Logger;

public class SimpleNakedClass extends AbstractNakedObject implements NakedClass {
    private final static Logger LOG = Logger.getLogger(SimpleNakedClass.class);
    private final TextString className = new TextString();
    private NakedObjectSpecification specification;
    private boolean createPersistentInstances;

    {
       	createPersistentInstances = Configuration.getInstance().getBoolean("nakedclass.create-persistent", true);    
    }

    public SimpleNakedClass(String name) {
        specification = NakedObjectSpecificationLoader.getInstance().loadSpecification(name);
        className.setValue(name);
    }
    
    public SimpleNakedClass() {}
    
	public void aboutActionFind(ActionAbout about) throws ObjectStoreException {
		about.setDescription("Get a simple finder object to start searches within the " + getSingularName() + " instances");
		about.setName("Find " + getPluralName());
		about.unusableOnCondition(! getObjectManager().hasInstances(forNakedClass()), 
				"No instances available to find");
    	ClassAbout ca = specification.getClassAbout();
    	if(ca != null && ca.canAccess().isVetoed() ) {
    	    about.invisible();
    	}
    }

    public String getSingularName() {
        return forNakedClass().getSingularName();
    }

    public void aboutActionInstances(ActionAbout about) throws ObjectStoreException {
    	about.setDescription("Get the " + getSingularName() + " instances");
    	about.setName(getPluralName());
    	ClassAbout ca = specification.getClassAbout();
    	if(ca != null && ca.canAccess().isVetoed() ) {
    	    about.invisible();
    	} else {
	    	about.unusableOnCondition(! getObjectManager()
	                                                       .hasInstances(forNakedClass()), "No instances available");
    	}  	
    }

    public String getPluralName() {
        return forNakedClass().getPluralName();
    }

    public String getFullName() {
        return forNakedClass().getFullName();
    }

    public void aboutActionNewInstance(ActionAbout about) {
       	about.setDescription("Create a new " + getSingularName() + " instance");
    	about.setName("New " + getSingularName());
    	ClassAbout ca = specification.getClassAbout();
    	if(ca != null && ca.canUse().isVetoed() ) {
    	    about.invisible();
    	}
     }

	public FastFinder actionFind() {
		FastFinder find = new FastFinder();
		find.setContext(getContext());
		find.setFromClass(forNakedClass());
		return find;
    }

    public NakedCollection actionInstances() {
       return allInstances();
    }
    
    public NakedCollection allInstances() {
        return getObjectManager().allInstances(forNakedClass());
    }

    public NakedCollection findInstances(String searchTerm) {
        return getObjectManager().findInstances(forNakedClass(), searchTerm);
    }
    
    public NakedObject actionNewInstance() {
        return newInstance();
    }
    
    public NakedObject newInstance() {
        NakedObjectManager objectManager = getObjectManager();
        
        if(createPersistentInstances) {
        	objectManager.startTransaction();
        }

        NakedObject object;
        object = (NakedObject) forNakedClass().acquireInstance();
        object.setContext(getContext());
        object.created();

        if(createPersistentInstances) {
	        try {
	            getObjectManager().makePersistent(object);
		        objectManager.endTransaction();
	        } catch (NotPersistableException e) {
	            object = getObjectManager().generatorError("Failed to create instance of " + this, e);	
	            LOG.error("Failed to create instance of " + this, e);
	        }
        }

        return object;
    }
    
    public TextString getClassName() {
        return className;
    }
    
    public String getName() {
        return className.stringValue();
    }

    public String toString() {
        StringBuffer s = new StringBuffer();

        s.append("NakedClassSpec");
        s.append(" [");

        // type of object - EO, Primitive, Collection, with Status etc
        // Persistent/transient & Resolved or not
        s.append(isPersistent() ? "P" : (isFinder() ? "F" : "T"));
        s.append(isResolved() ? "R" : "-");

        // obect identifier
        if (getOid() != null) {
            s.append(":");
            s.append(getOid().toString().toUpperCase());
        } else {
            s.append(":-");
        }


        // title
        s.append(' ');
        s.append(className.title());
        
         s.append("]");

        s.append("  " + Long.toHexString(super.hashCode()).toUpperCase());

        return s.toString();
    }

    public NakedObjectSpecification forNakedClass() {
        if(specification == null) {
            if(getName().length() == 0) {
                throw new NakedObjectRuntimeException();
            }
            specification = NakedObjectSpecificationLoader.getInstance().loadSpecification(getName());
        }
        return specification;
    }   
    
    public Title title() {
        return getClassName().title();
    }
 
    
}


/*
Naked Objects - a framework that exposes behaviourally complete
business objects directly to the user.
Copyright (C) 2000 - 2004  Naked Objects Group Ltd

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

The authors can be contacted via www.nakedobjects.org (the
registered address of Naked Objects Group is Kingsway House, 123 Goldworth
Road, Woking GU21 1NR, UK).
*/