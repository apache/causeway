package org.nakedobjects.distribution;

import org.nakedobjects.object.NakedClass;
import org.nakedobjects.object.NakedClassManager;


public class LoadClassRequest extends Request {
	private WrappedString name;

	public LoadClassRequest(String name) {
		this.name = new WrappedString(name);
	}

	protected void generateResponse(RequestContext context) {
		NakedClass cls = NakedClassManager.getInstance().getNakedClass(name.getValue());
		response = cls.getReflector().stringValue();
	}

	public NakedClass getNakedClass() {
		NakedClass cls = new NakedClass();
		cls.getName().setValue(name.getValue());
		cls.getReflector().setValue((String) response);
		return cls;
	}

	public String toString() {
		return "LoadClass " + name;
	}
}
