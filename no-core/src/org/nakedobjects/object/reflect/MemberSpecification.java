package org.nakedobjects.object.reflect;


import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.control.About;
import org.nakedobjects.object.control.Allow;
import org.nakedobjects.object.control.Permission;
import org.nakedobjects.object.security.Session;


public abstract class MemberSpecification {
    // About to use if none has been coded in
    protected class DefaultAbout implements About {
        public Permission canAccess() {
            return Allow.DEFAULT;
        }

        public Permission canUse() {
            return Allow.DEFAULT;
        }
        
        public String debug() {
			return "no details (DefaultAbout)";
		}

        public String getDescription() {
            return "";
        }

        public String getName() {
            return label;
        }
    }
    
    private final String label;
    private final String name;
    
    protected MemberSpecification(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Name must always be set");
        }
 
        this.label = NameConvertor.naturalName(name);
        this.name = NameConvertor.simpleName(name);
    }
    
    public abstract boolean canAccess(Session session, NakedObject object);
    
    public abstract boolean canUse(Session session, NakedObject object);
    
    /**
     * Return the default label for this member.  This is based on the name of this member.
     * 
     * @see #getName()
     */
    public String getLabel() {
        return label;
    }
    	
    protected String getLabel(About about) {
    	if (about == null) {
    		return label;
    	} else {
    		if (about.getName() == null) {
    			return label;
    		} else {
    			return about.getName();
    		}
    	}
    }

    /**
     * Return the label for this member in the current security context, and for the specified object.
     * 
     * @see #getLabel()
     */
    public abstract String getLabel(Session session, NakedObject object);

    /**
     Returns the name of the member.
     */
    public String getName() {
        return name;
    }

    /**
     Returns true if an about method is defined for this Member.
     */
    public abstract boolean hasAbout();

    public String toString() {
    	return "name=" + getName() + ",label='" + getLabel() + "'";
    }


}

/*
Naked Objects - a framework that exposes behaviourally complete
business objects directly to the user.
Copyright (C) 2000 - 2003  Naked Objects Group Ltd

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