package org.nakedobjects.example.musicagent;

import org.nakedobjects.application.Title;
import org.nakedobjects.application.valueholder.TextString;

import java.util.Vector;



public class Musician extends BaseObject {
	private final TextString name = new TextString();
	private final Vector instruments =  new Vector();
	private final Vector parts = new Vector();
	

	public TextString getName() {
		return name;
	}

	public void addToParts(Part part) {
		parts.add(part);
		part.setMusician(this);
		objectChanged();
	}
	
	public void removeFromParts(Part part) {
		parts.remove(part);	
		part.setMusician(null);
		objectChanged();
	}
	
	public Vector getParts() {
		return parts;
	}
	
	public void addToInstruments(Instrument instrument) {
		instruments.add(instrument);
		instrument.getPlayers().add(this);
		objectChanged();
	}
	
	public void removeFromInstruments(Instrument instrument) {
		instruments.remove(instrument);	
		instrument.getPlayers().remove(this);
		objectChanged();
	}
	
	public Vector getInstruments() {
		return instruments;
	}
	
	public Title title() {
		return name.title();
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
