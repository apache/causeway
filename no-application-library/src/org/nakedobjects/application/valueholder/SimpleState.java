package org.nakedobjects.application.valueholder;

import org.nakedobjects.application.NakedObjectRuntimeException;
import org.nakedobjects.application.Title;
import org.nakedobjects.application.ValueParseException;
import org.nakedobjects.application.control.State;


public class SimpleState extends BusinessValueHolder implements State {
	private static final long serialVersionUID = 1L;
	private String name;
	private int id;
	private State[] states;
	
	public SimpleState(int id, String name) {
		if(id < 0) {
			throw new IllegalArgumentException("Id must be 0 or greater");
		}
		
		this.id = id;
		this.name = name;
	}

	public Object getValue() {
        return this;
    } 
	
	public SimpleState(State[] states) {
		this.states = states;
	}
	
	public boolean userChangeable() {
	    return false;
	}
	
	public void clear() {
		id = -1;
		name = null;
	}

	public void parseUserEntry(String text) throws ValueParseException {
		throw new NakedObjectRuntimeException();
	}

	public void reset() {
		id = -1;
		name = null;
	}

	public void restoreFromEncodedString(String data) {
		id = Integer.valueOf(data).intValue();
		name = "unmatched state";
		for (int i = 0; i < states.length; i++) {
			if(id == ((SimpleState) states[i]).id) {
				name = ((SimpleState) states[i]).name;
				break;
			}
		}

	}

	public String asEncodedString() {
		return String.valueOf(id);
	}

	public void copyObject(BusinessValueHolder object) {
		throw new NakedObjectRuntimeException();
	}

	public boolean isEmpty() {
		return id == -1;
	}

	public boolean equals(Object object) {
  		if(object instanceof SimpleState) {
			int cid = ((SimpleState) object).id;
			if(cid == id) {
				return true;
			}
		}
		return false; 
	}

	public boolean isSameAs(BusinessValueHolder object) {
		if(object instanceof SimpleState) {
			int cid = ((SimpleState) object).id;
			if(cid == id) {
				return true;
			}
		}
		return false; 
	}

	/**
	 * copies the state across from the specified state
	 */
	public void changeTo(State state) {
		this.id = ((SimpleState) state).id;
		this.name = ((SimpleState) state).name;
	}

    public String titleString() {
        return name;
    }
    
    public Title title() {
        return new Title(name);
    }
}


/*
 * Naked Objects - a framework that exposes behaviourally complete business
 * objects directly to the user. Copyright (C) 2000 - 2005 Naked Objects Group
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
