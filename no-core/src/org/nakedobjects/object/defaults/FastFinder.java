package org.nakedobjects.object.defaults;

import org.nakedobjects.object.NakedCollection;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectManager;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.reflect.internal.InternalAbout;


public class FastFinder implements InternalNakedObject {
	private NakedObjectSpecification forClass;
	private String term;
    private NakedObjectManager objectManager;
	
	public String getIconName() {
		return forClass.getShortName();
	}

	public String getTerm() {
		return term;
	}
	
	public void setTerm(String term) {
        this.term = term;
    }
	
	public void aboutActionFind(InternalAbout about) {
		about.unusableOnCondition(term == null || term.trim().length() == 0, "Search term needed");
	}
	
	public NakedObject actionFind() {
		NakedCollection instances = objectManager.findInstances(forClass, term, true);
		if(instances.size() == 1) { 
			return (NakedObject) instances.elements().nextElement();
		} else {
			return instances;
		}
	}
	
	public void setObjectManager(NakedObjectManager objectManager) {
        this.objectManager = objectManager;
    }
	
	public void aboutFromClass(InternalAbout about) {
		about.unusable();
	}
	
	public void setFromClass(NakedObjectSpecification nakedClass) {
		this.forClass = nakedClass;
	}
	
	public NakedObjectSpecification getForClass() {
        return forClass;
    }
	
	public String titleString() {
		return "Search for '" + term + "'";
	}

}
