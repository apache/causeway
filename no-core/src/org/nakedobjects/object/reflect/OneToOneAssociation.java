package org.nakedobjects.object.reflect;

import org.nakedobjects.object.InvalidEntryException;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.TextEntryParseException;
import org.nakedobjects.object.control.Hint;
import org.nakedobjects.object.control.DefaultHint;
import org.nakedobjects.object.security.Session;


public class OneToOneAssociation extends NakedObjectAssociation {
	private final OneToOnePeer reflectiveAdapter;
	
    public OneToOneAssociation(String name, NakedObjectSpecification type, OneToOnePeer association) {
        super(name, type);
        this.reflectiveAdapter = association;
    }
    
    protected boolean canAccess(Session session, NakedObject object) {
    	return getHint(session, object, null).canAccess().isAllowed();
    }
    
    protected boolean canUse(Session session, NakedObject object) {
    	return getHint(session, object, null).canUse().isAllowed();
    }
    
    protected void clear(NakedObject inObject) {
    	Naked associate = get(inObject);
    	if(associate != null) {
    		clearAssociation(inObject, (NakedObject) associate);
    	}
    }

    protected void clearAssociation(NakedObject inObject, NakedObject associate) {
        if (associate == null) {
            throw new NullPointerException("Must specify the item to remove/dissociate");
        }
    	reflectiveAdapter.clearAssociation(inObject, associate);
    }

	protected Naked get(NakedObject fromObject) {
		return reflectiveAdapter.getAssociation(fromObject);
	}
    
    protected Hint getHint(Session session, NakedObject object, NakedObject value) {
        if(hasHint()) {
            return reflectiveAdapter.getHint(session, object, value);
        } else {
            return new DefaultHint();
//            return new DefaultHint(getName());
        }
    }

    protected String getLabel(Session session, NakedObject object) {
      	Hint about = getHint(session, object, (NakedObject) get(object));

        return getLabel(about);
    }

	public boolean hasHint() {
		return reflectiveAdapter.hasHint();
	}

	protected void setValue(NakedObject inObject, Object associate) {
    	reflectiveAdapter.setValue(inObject, associate);
	}

	protected void initValue(NakedObject inObject, Object associate) {
    	reflectiveAdapter.initValue(inObject, associate);
	}

	public boolean isDerived() {
		return reflectiveAdapter.isDerived();
	}

	protected void setAssociation(NakedObject inObject, NakedObject associate) {
    	reflectiveAdapter.setAssociation(inObject, associate);
    }

	protected void initAssociation(NakedObject inObject, NakedObject associate) {
    	reflectiveAdapter.initAssociation(inObject, associate);
    }
	
    public String toString() {
        return "OneToOne " + (isValue() ? "VALUE" : "OBJECT") + " [" + super.toString() + ",type=" + getSpecification().getShortName() + "]";
    }

    protected void parseTextEntry(NakedObject inObject, String text) throws TextEntryParseException, InvalidEntryException {
        reflectiveAdapter.parseTextEntry(inObject, text);    
    }
    
    public boolean isEmpty(NakedObject inObject) {
        return reflectiveAdapter.isEmpty(inObject);
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