package org.nakedobjects.object.defaults;

import org.nakedobjects.NakedObjects;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedCollection;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.persistence.TitleCriteria;
import org.nakedobjects.object.reflect.internal.InternalAbout;


public class FastFinder implements InternalNakedObject {
	private NakedObjectSpecification specification;
	private String term;
	
	public String getIconName() {
		return specification.getShortName();
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
	
	public Naked actionFind() {
		NakedCollection instances = NakedObjects.getObjectManager().findInstances(new TitleCriteria(specification, term, true));
		if(instances.size() == 1) { 
			return (NakedObject) instances.elements().nextElement();
		} else {
			return instances;
		}
	}
	
	public void aboutFromClass(InternalAbout about) {
		about.unusable();
	}
	
	public void setFromClass(NakedObjectSpecification nakedClass) {
		this.specification = nakedClass;
	}
	
	public NakedObjectSpecification getSpecification() {
        return specification;
    }
	
	public String titleString() {
		return "Search for '" + term + "'";
	}

}
