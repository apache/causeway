package org.nakedobjects.object.defaults;

import org.nakedobjects.object.InternalCollection;
import org.nakedobjects.object.NakedClass;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.UserContext;
import org.nakedobjects.object.control.FieldAbout;
import org.nakedobjects.object.defaults.collection.InternalCollectionVector;
import org.nakedobjects.object.security.Role;
import org.nakedobjects.object.security.User;

import org.apache.log4j.Logger;



public abstract class AbstractUserContext extends AbstractNakedObject implements UserContext {
    private static final Logger LOG = Logger.getLogger(AbstractUserContext.class);
    
    public static String fieldOrder() {
    	return "user, classes, objects";
    }
    	
    // TODO - the fudge of specifying A concrete class instead of NakedClass get round a limition with NakedClass.isType()
	private final InternalCollection classes = new InternalCollectionVector(SimpleNakedClass.class, this);
    private final InternalCollection objects = new InternalCollectionVector(NakedObject.class, this);
	private User user;
    
    public void aboutClasses(FieldAbout about, NakedObject element, boolean add) {
    	about.modifiableOnlyByRole(Role.SYSADMIN);
    }
    
    public void aboutObjects(FieldAbout about, NakedObject element, boolean add) {
    	about.modifiableOnlyByUser(user);
    }
    
    public void aboutUser(FieldAbout about, User user) {
    	about.modifiableOnlyByRole(Role.SYSADMIN);
    }
    
    protected NakedClass addClass(Class class1) {
        LOG.info("Added class " + class1 + " to " + this);
		NakedObjectSpecification nc = NakedObjectSpecification.getSpecification(class1.getName());
		NakedClass nakedClass = getObjectManager().getNakedClass(nc);
        classes.add(nakedClass);
        return nakedClass;
	}
	
	protected NakedClass addClass(String className) {
	     LOG.info("Added class " + className + " to " + this);
	    NakedObjectSpecification nc = NakedObjectSpecification.getSpecification(className);
	    NakedClass nakedClass = getObjectManager().getNakedClass(nc);
	    getObjectManager().makePersistent(nakedClass);
        classes.add(nakedClass);
        return nakedClass;
	}

	public void associateUser(User user) {
		user.setRootObject(this);
		this.setUser(user);
	}
	
	public void dissociateUser(User user) {
		user.setRootObject(null);
		this.setUser(null);
	}

    public InternalCollection getClasses() {
        return classes;
    }
    
    public InternalCollection getObjects() {
    	return objects;
    }
    
    public User getUser() {
    	resolve(user);
    	return user;
    }
    	
    public void setUser(User user) {
    	this.user = user;
    	objectChanged();
    }
}
