package org.nakedobjects.distribution;

import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.NakedObjectSpecificationLoader;
import org.nakedobjects.object.io.TransferableReader;
import org.nakedobjects.object.io.TransferableWriter;


public class LoadClassRequest extends Request {
    private final String className;

    public LoadClassRequest(String className) {
        this.className = className;
    }

    public LoadClassRequest(TransferableReader data) {
        className = data.readString();
    }

    protected void generateResponse(RequestContext context) {
        NakedObjectSpecification cls = NakedObjectSpecificationLoader.getInstance().loadSpecification(className);
        response = new Response();
        response.writeString(cls.getReflector().stringValue());
        //		response = cls.getReflector().stringValue();
    }

    public void writeData(TransferableWriter data) {
        data.writeString(className);
    }

    public NakedObjectSpecification getNakedClass() {
        sendRequest();
        NakedObjectSpecification cls = new NakedObjectSpecification();
        cls.getName().setValue(className);
        cls.getReflector().setValue(response.readString());
        //		cls.getReflector().setValue(response.getValue());
        return cls;
    }

    public String toString() {
        return "LoadClass " + className;
    }
}