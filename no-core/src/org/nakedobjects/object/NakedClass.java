package org.nakedobjects.object;

import org.nakedobjects.object.collection.InstanceCollection;
import org.nakedobjects.object.control.About;
import org.nakedobjects.object.control.ActionAbout;
import org.nakedobjects.object.value.TextString;
import org.nakedobjects.utility.Configuration;

import org.apache.log4j.Logger;

public class NakedClass extends AbstractNakedObject {
    private final static Logger LOG = Logger.getLogger(NakedClass.class);
    private final TextString className = new TextString();
    private NakedObjectSpecification nakedClass;
    private boolean createPersistentInstances;

    {
       	createPersistentInstances = Configuration.getInstance().getBoolean("nakedclass.create-persistent", true);    
    }

    public NakedClass(String name) {
        nakedClass = NakedObjectSpecification.getNakedClass(name);
        className.setValue(name);
    }
    
    public NakedClass() {}
    
    public String getIconName() {
        return forNakedClass().getShortName();
    }
    
	public void aboutActionFind(ActionAbout about) throws ObjectStoreException {
		about.setDescription("Get a simple finder object to start searches within the " + getSingularName() + " instances");
		about.setName("Find " + getPluralName());
		about.unusableOnCondition(! getObjectManager().hasInstances(forNakedClass()), 
				"No instances available to find");
    }

    public String getSingularName() {
        return forNakedClass().getSingularName();
    }

    public void aboutActionInstances(ActionAbout about) throws ObjectStoreException {
    	about.setDescription("Get the " + getSingularName() + " instances");
    	about.setName(getPluralName());
    	about.unusableOnCondition(! getObjectManager()
                                                       .hasInstances(forNakedClass()), "No instances available");
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
  // TODO Refactor  	about.unusableOnCondition(! getClassAbout().canUse().isAllowed(), "????");
     }

    public About aboutExplorationActionClass() {
        return ActionAbout.DISABLE;
    }

    public About aboutExplorationActionClone() {
        return ActionAbout.DISABLE;
    }

    public About aboutExplorationActionDestroyObject() {
        return ActionAbout.DISABLE;
    }

    public About aboutExplorationActionMakePersistent() {
        return ActionAbout.DISABLE;
    }

	public FastFinder actionFind() {
		FastFinder find = new FastFinder();
		find.setContext(getContext());
		find.setFromClass(forNakedClass());
		return find;
    }

    public InstanceCollection actionInstances() {
        return getObjectManager().allInstances(forNakedClass());
    }

    public NakedObject actionNewInstance() {
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
	            object = new NakedError("Failed to create instance of " + this);	
	            LOG.error("Failed to create instance of " + this, e);
	        }
        }

        return object;
    }
    

    public TextString getName() {
        return className;
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
        if(nakedClass == null) {
            if(getName().isEmpty()) {
                throw new NakedObjectRuntimeException();
            }
            nakedClass = NakedObjectSpecification.getNakedClass(getName().stringValue());
        }
        return nakedClass;
    }   
    
    public Title title() {
        return getName().title();
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