/*
 * Created on Jan 23, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.nakedobjects.object.control;

import org.nakedobjects.object.Naked;
import org.nakedobjects.object.Title;
import org.nakedobjects.object.ValueParseException;
import org.nakedobjects.object.value.AbstractNakedValue;
import org.nakedobjects.utility.NotImplementedException;


public class State extends AbstractNakedValue {
	private static final long serialVersionUID = 1L;
	private String name;
	private int id;
	private State[] states;
	
	public State(int id, String name) {
		if(id < 0) {
			throw new IllegalArgumentException("Id must be 0 or greater");
		}
		
		this.id = id;
		this.name = name;
	}

	public State(State[] states) {
		this.states = states;
	}

	public About about() {
		return FieldAbout.READ_ONLY;
	}
	
	public void clear() {
		id = -1;
		name = null;
	}

	public void parse(String text) throws ValueParseException {
		throw new NotImplementedException();
	}

	public void reset() {
		id = -1;
		name = null;
	}

	public void restoreString(String data) {
		id = Integer.valueOf(data).intValue();
		name = "unmatched state";
		for (int i = 0; i < states.length; i++) {
			if(id == states[i].id) {
				name = states[i].name;
				break;
			}
		}

	}

	public String saveString() {
		return String.valueOf(id);
	}

	public void copyObject(Naked object) {
		throw new NotImplementedException();
	}

	public boolean isEmpty() {
		return id == -1;
	}

	public boolean isSameAs(Naked object) {
		if(object instanceof State) {
			int cid = ((State) object).id;
			if(cid == id) {
				return true;
			}
		}
		return false; 
	}

	public Title title() {
		return new Title(name);
	}

	/**
	 * copies the state across from the specified state
	 */
	public void changeTo(State state) {
		this.id = state.id;
		this.name = state.name;
	}
}
