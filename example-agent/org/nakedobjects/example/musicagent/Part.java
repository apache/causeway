
package org.nakedobjects.example.musicagent;

import org.nakedobjects.application.Title;
import org.nakedobjects.application.value.TextString;



public class Part extends BaseObject {
	private final TextString description = new TextString();
	private Performance performance;
	private Musician musician;
	private Instrument instrument;
	
	
	public TextString getDescription() {
		return description;
	}

	public Instrument getInstrument() {
		resolve(instrument);
		return instrument;
	}
	

    public Musician getMusician() {
		resolve(musician);
		return musician;
	}

	public Performance getPerformance() {
		resolve(performance);
		return performance;
	}

	public void associateMusician(Musician musician) {
		musician.addToParts(this);
	}

	public void dissociateMusician(Musician musician) {
		musician.removeFromParts(this);
	}

	public void setInstrument(Instrument instrument) {
		this.instrument = instrument;
		objectChanged();
	}
	
	public void setMusician(Musician musician) {
		this.musician = musician;
		objectChanged();
	}

	public void associatePerformance(Performance performance) {
		performance.addParts(this);	
	}
	
	public void dissociatePerformance(Performance performance) {
		performance.removeParts(this);	
	}
	
	public void setPerformance(Performance performance) {
		this.performance = performance;
		objectChanged();
	}

	public Title title() {
		return description.title();
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
