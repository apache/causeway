package org.nakedobjects.object.defaults;

import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedClass;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectContext;
import org.nakedobjects.object.NakedObjectManager;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.NakedObjectSpecificationLoader;
import org.nakedobjects.object.Oid;
import org.nakedobjects.object.UserContext;
import org.nakedobjects.object.control.Hint;
import org.nakedobjects.object.internal.InternalObjectSpecification;
import org.nakedobjects.object.reflect.Action;
import org.nakedobjects.object.reflect.NakedObjectAssociation;
import org.nakedobjects.object.reflect.NakedObjectField;
import org.nakedobjects.object.reflect.OneToOneAssociation;
import org.nakedobjects.object.reflect.PojoAdapter;
import org.nakedobjects.object.security.Session;

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
    
    /*
    public void aboutClasses(FieldAbout about, NakedObject element, boolean add) {
    	about.modifiableOnlyByRole(Role.SYSADMIN);
    }
    
    public void aboutObjects(FieldAbout about, NakedObject element, boolean add) {
    	about.modifiableOnlyByUser(user);
    }
    
    public void aboutUser(FieldAbout about, User user) {
    	about.modifiableOnlyByRole(Role.SYSADMIN);
    }
    */
    
    protected NakedClass addClass(Class class1) {
        LOG.info("Added class " + class1 + " to " + this);
		NakedObjectSpecification nc = NakedObjectSpecificationLoader.getInstance().loadSpecification(class1.getName());
		NakedClass nakedClass = getObjectManager().getNakedClass(nc);
        classes.addElement(nakedClass);
        return nakedClass;
	}
	
	protected NakedClass addClass(String className) {
	     LOG.info("Added class " + className + " to " + this);
	    NakedObjectSpecification nc = NakedObjectSpecificationLoader.getInstance().loadSpecification(className);
	    NakedClass nakedClass = getObjectManager().getNakedClass(nc);
	    getObjectManager().makePersistent(PojoAdapter.createAdapter(nakedClass));
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
    

    /*
    public User getUser() {
    	resolve(user);
    	return user;
    }
    	
    private void resolve(User user2) {}

    public void setUser(User user) {
    	this.user = user;
    	objectChanged();
    }
    */
    
    private NakedObjectContext context;
    
    public NakedObjectContext getContext() {
        // TODO context needs to assigned to the object properly
//       Assert.assertTrue("must have a context: " + this, context != null);
		 if(context == null) context = NakedObjectContext.getDefaultContext();
       return context;
   }

    protected NakedObjectManager getObjectManager() {
        return getContext().getObjectManager();
    }
    
    
    

    public void clear(OneToOneAssociation specification) {}

    public NakedObject getField(NakedObjectField field) {
        return null;
    }

    public void setAssociation(NakedObjectAssociation field, NakedObject associatedObject) {}

    public void initData(OneToOneAssociation field, Object object) {}

    public String getLabel(Session session, NakedObjectField field) {
        return null;
    }

    public String getLabel(Session session, Action action) {
        return null;
    }

    public void clear(NakedObjectAssociation specification, NakedObject ref) {}

    public boolean canAccess(Session session, NakedObjectField specification) {
        return false;
    }

    public boolean canAccess(Session session, Action action) {
        return false;
    }

    public boolean canUse(Session session, NakedObjectField field) {
        return false;
    }

    public NakedObject execute(Action action, Naked[] parameters) {
        return null;
    }

    public Hint getAbout(Session session, Action action, Naked[] parameters) {
        return null;
    }

    public Hint getAbout(Session session, NakedObjectField field, NakedObject value) {
        return null;
    }
	
    
    
    
    

	
	
	
	
    public void created() {}

    public void deleted() {}

    public String getIconName() {
        return null;
    }

    public Object getObject() {
        return null;
    }

    public Oid getOid() {
        return null;
    }

    public boolean isResolved() {
        return false;
    }

    public boolean isPersistent() {
        return false;
    }

    public void setOid(Oid oid) {}

    public void setResolved() {}

    public void setContext(NakedObjectContext context) {}

    public boolean isEmpty() {
        return false;
    }

    public void copyObject(Naked object) {}

    public NakedObjectSpecification getSpecification() {
        return new InternalObjectSpecification("");
    }

    public boolean isSameAs(Naked object) {
        return false;
    }

    
}
