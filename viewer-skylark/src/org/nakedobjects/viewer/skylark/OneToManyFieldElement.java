package org.nakedobjects.viewer.skylark;

import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.control.Allow;
import org.nakedobjects.object.control.Consent;
import org.nakedobjects.object.control.Hint;
import org.nakedobjects.object.control.Veto;
import org.nakedobjects.object.reflect.OneToManyAssociation;
import org.nakedobjects.object.security.ClientSession;
import org.nakedobjects.viewer.skylark.basic.ObjectOption;
import org.nakedobjects.viewer.skylark.basic.RemoveOneToManyAssociationOption;

import org.apache.log4j.Logger;

public class OneToManyFieldElement extends ObjectField implements ObjectContent {
    private static final Logger LOG = Logger.getLogger(OneToManyFieldElement.class);
	private static final UserAction REMOVE_ASSOCIATION = new RemoveOneToManyAssociationOption();
    private final NakedObject element;

	   public NakedObject getObject() {
	        return element;
	    }

	public OneToManyFieldElement(NakedObject parent, NakedObject element, OneToManyAssociation association) {
		super(parent, association);
	        this.element = element;
	}
	
	public String debugDetails() {
		return super.debugDetails() +  "  element:" + element + "\n";  
	}
	
	private OneToManyAssociation getOneToManyAssociation() {
		return (OneToManyAssociation) getField();
	}

	public void menuOptions(MenuOptionSet options) {
		super.menuOptions(options);
		ObjectOption.menuOptions(element, options);
		options.add(MenuOptionSet.OBJECT, REMOVE_ASSOCIATION);
	}
	
	public String toString() {
		return getObject() + "/" + getField();
	}	
	
	public void clear() {
    	NakedObject parentObject = getParent();
		OneToManyAssociation association = getOneToManyAssociation();
        NakedObject associatedObject = getObject();
        LOG.debug("Remove " + associatedObject + " from " + parentObject);
        association.clearAssociation(parentObject, associatedObject);
	}
	
	public void setObject(NakedObject object) {
/*	   	NakedObject parentObject = getParent();
		OneToManyAssociationSpecification association = getOneToManyAssociation();
        NakedObject associatedObject = getObject();
        LOG.debug("Remove " + associatedObject + " from " + parentObject);
        association.clearAssociation(parentObject, associatedObject);
*/
        
	}

    public Consent canClear() {
    	NakedObject parentObject = getParent();
		OneToManyAssociation association = getOneToManyAssociation();
        NakedObject associatedObject = getObject();
        Hint about = association.getHint(ClientSession.getSession(), parentObject, associatedObject, false);
        Consent edit = about.canUse();
        if (edit.isAllowed()) {
            String status = "Clear the association to this object from '" + parentObject.titleString() + "'";
            return new Allow(status);
        } else {
            return new Veto(edit.getReason());
        }
    }
    
    public Consent canSet(NakedObject dragSource) {
        return Veto.DEFAULT; 
   }
    
    
 
}


/*
Naked Objects - a framework that exposes behaviourally complete
business objects directly to the user.
Copyright (C) 2000 - 2005  Naked Objects Group Ltd

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