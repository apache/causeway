package org.nakedobjects.object.defaults;

import org.nakedobjects.NakedObjects;
import org.nakedobjects.object.NakedClass;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.NakedObjectSpecificationLoader;
import org.nakedobjects.object.UserContext;

import java.util.Vector;

import org.apache.log4j.Logger;



public abstract class AbstractUserContext implements UserContext {
    private static final Logger LOG = Logger.getLogger(AbstractUserContext.class);
    
    public static String fieldOrder() {
    	return "user, classes, objects";
    }
    	
    // TODO - the fudge of specifying A concrete class instead of NakedClass get round a limition with NakedClass.isType()
	private final Vector classes = new Vector();
    private final Vector objects = new Vector();
//	private User user;

    protected NakedClass addClass(Class cls) {
        return addClass(cls.getName());
	}
	
	protected NakedClass addClass(String className) {
	     LOG.info("Added class " + className + " to " + this);
	    NakedObjectSpecification nc = NakedObjectSpecificationLoader.getInstance().loadSpecification(className);
	    NakedClass nakedClass = NakedObjects.getObjectManager().getNakedClass(nc);
        classes.addElement(nakedClass);
        return nakedClass;
	}
/*
	public void associateUser(User user) {
		user.setRootObject(PojoAdapter.createAdapter(this));
		this.setUser(user);
	}
	
	public void dissociateUser(User user) {
		user.setRootObject(null);
		this.setUser(null);
	}
*/
    public Vector getClasses() {
        return classes;
    }
    
    public void addToClasses(NakedClass cls) {
        classes.addElement(cls);
        objectChanged();
    }
    
    private void objectChanged() {}

    public void removeFromClasses(NakedClass cls) {
        classes.addElement(cls);
        objectChanged();
    }
    
    public Vector getObjects() {
    	return objects;
    }
    
    public void addToObjects(NakedObject cls) {
        objects.addElement(cls);
        objectChanged();
    }
    
    public void removeFromObjects(NakedObject cls) {
        objects.addElement(cls);
        objectChanged();
    }
}
