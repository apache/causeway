package org.nakedobjects.distribution;

import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.NakedClNakedObjectSpecification;


public class LoadClassRequest extends Request {
	private WrappedString name;

	public LoadClassRequest(String name) {
		this.name = new WrappedString(name);
	}

	protected void generateResponse(RequestContext context) {
		NakedObjectSpecification cls = NakedObjectSpecification.getNakedClass(name.getValue());
		response = new WrappedString(cls.getReflector().stringValue());
	}

	public NakedObjectSpecification getNakedClass() {
		NakedObjectSpecification cls = new NakedObjectSpecification();
		cls.getName().setValue(name.getValue());
		cls.getReflector().setValue(((WrappedString) response).getValue());
		return cls;
	}

	public String toString() {
		return "LoadClass " + name;
	}
}
