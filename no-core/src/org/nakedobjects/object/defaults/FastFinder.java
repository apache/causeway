package org.nakedobjects.object.defaults;

import org.nakedobjects.object.NakedCollection;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.control.ActionAbout;
import org.nakedobjects.object.control.FieldAbout;
import org.nakedobjects.object.defaults.value.TextString;


public class FastFinder extends AbstractNakedObject {
	private final TextString term = new TextString();
	private NakedObjectSpecification forClass;
	
	public String getIconName() {
		return forClass.getShortName();
	}

	public TextString getTerm() {
		return term;
	}
	
	public void aboutActionFind(ActionAbout about) {
		about.unusableOnCondition(term.isEmpty(), "Search term needed");
	}
	
	public NakedObject actionFind() {
		NakedCollection instances = getObjectManager().findInstances(forClass, term.stringValue());
		if(instances.size() == 1) { 
			return (NakedObject) instances.elements().nextElement();
		} else {
			return instances;
		}
	}
	
	public void aboutFromClass(FieldAbout about) {
		about.unmodifiable();
	}
	
	public NakedObjectSpecification getFromClass() {
		return forClass;
	}
	
	public void setFromClass(NakedObjectSpecification nakedClass) {
		this.forClass = nakedClass;
	}
	
	public boolean isFinder() {
		return false;
	}
	
	public Title title() {
		return new Title("Search for '").concat(term).concat("'");
	}

}
