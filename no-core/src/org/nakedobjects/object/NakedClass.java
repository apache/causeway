package org.nakedobjects.object;

import org.nakedobjects.object.collection.InstanceCollection;
import org.nakedobjects.object.control.About;
import org.nakedobjects.object.control.ActionAbout;
import org.nakedobjects.object.control.ClassAbout;
import org.nakedobjects.object.reflect.Action;
import org.nakedobjects.object.reflect.Field;
import org.nakedobjects.object.reflect.Member;
import org.nakedobjects.object.reflect.NakedClassException;
import org.nakedobjects.object.reflect.OneToManyAssociation;
import org.nakedobjects.object.reflect.OneToOneAssociation;
import org.nakedobjects.object.reflect.Reflector;
import org.nakedobjects.object.reflect.Value;
import org.nakedobjects.object.reflect.simple.JavaReflector;
import org.nakedobjects.object.value.TextString;
import org.nakedobjects.security.SecurityContext;
import org.nakedobjects.security.Session;
import org.nakedobjects.utility.Configuration;

import java.io.Serializable;
import java.util.Vector;

import org.apache.log4j.Logger;


public final class NakedClass extends AbstractNakedObject implements Serializable {
    private final static Logger LOG = Logger.getLogger(NakedClass.class);
    public final static NakedClass SELF;
    
    static {
        SELF = new NakedClass();
		SELF.getName().setValue(NakedClass.class.getName());
		SELF.getReflector().setValue(JavaReflector.class.getName());
    }
        
    public static NakedClass createNakedClass(String name, String reflector) {
        NakedClass nc = new NakedClass();
        nc.getName().setValue(name);
        nc.getReflector().setValue(reflector);
        nc.setResolved();
        return nc;
    }
    
    private Action[] classActions = new Action[0];
    private Action[] objectActions = new Action[0];
    private Field[] fields = new Field[0];
    private Field[] viewFields;
    private final TextString className = new TextString();
    private final TextString reflectorClass = new TextString();

    private NakedObject pattern;
    private Reflector reflector;
    private NakedClassList subclasses = new NakedClassList();
    private NakedClass superclass;
	private boolean createPresistent;

	public void aboutActionFind(ActionAbout about) throws ObjectStoreException {
		about.setDescription("Get a finder object to start searches within the " + getSingularName() + " instances");
		about.setName(getSingularName() + " finder");
		about.unusableOnCondition(! NakedObjectManager.getInstance().hasInstances(this), 
				"No instances available to find");
    }

	public void aboutActionFastFind(ActionAbout about) throws ObjectStoreException {
		about.setDescription("Get a simple finder object to start searches within the " + getSingularName() + " instances");
		about.setName(getSingularName() + " fast finder");
		about.unusableOnCondition(! NakedObjectManager.getInstance().hasInstances(this), 
				"No instances available to find");
    }

    public void aboutActionInstances(ActionAbout about) throws ObjectStoreException {
    	about.setDescription("Get the " + getSingularName() + " instances");
    	about.setName(getPluralName());
    	about.unusableOnCondition(! NakedObjectManager.getInstance()
                                                       .hasInstances(this), "No instances available");
    }

    public void aboutActionNewInstance(ActionAbout about) {
       	about.setDescription("Create a new " + getSingularName() + " instance");
    	about.setName("New " + getShortName());
    	about.unusableOnCondition(! getClassAbout().canUse().isAllowed(), "????");
     }

    public About aboutExplorationActionClass() {
        return ActionAbout.DISABLE;
    }

    public About aboutExplorationActionClone() {
        return ActionAbout.DISABLE;
    }

    public About aboutExplorationActionDestroyObject() {
        return ActionAbout.DISABLE;
    }

    public About aboutExplorationActionMakePersistent() {
        return ActionAbout.DISABLE;
    }

    /**
    Creates an object of the type represented by this object. This method only creates a java object (using newInstance
    on the Class object returned using the getJavaType method).
    */
    public NakedObject acquireInstance() {
    	LOG.debug("acquire instance of " + getShortName());
        return reflector().acquireInstance();
    }

	public NakedObject actionFind() {
        return createFinder();
    }

	public FastFinder actionFastFind() {
		FastFinder find = new FastFinder();
		find.setFromClass(this);
		return find;
    }

    public InstanceCollection actionInstances() {
        return InstanceCollection.allInstances(this);
    }

    public NakedObject actionNewInstance() {
    	return createInstance();
    }
    
    protected About classAbout() {
        return reflector().classAbout();
    }
    

    /**
     * Creates a finder object with no references or values.
     * @return NakedObject
     */
    public NakedObject createFinder() {
        NakedObject finder = acquireInstance();

        finder.makeFinder();

        Field[] fields = getFields();

        for (int fld = 0; fld < fields.length; fld++) {
            fields[fld].clear(finder);
        }

        return finder;
    }

    /**
     * Creates an instance of the class represented by this object.
     */
     public NakedObject createInstance() {
        NakedObjectManager objectManager = NakedObjectManager.getInstance();
        
        if(createPresistent) {
        	objectManager.startTransaction();
        }

        NakedObject object;
        object = acquireInstance();
        object.created();

        if(createPresistent) {
	        try {
	            NakedObjectManager.getInstance().makePersistent(object);
		        objectManager.endTransaction();
	        } catch (NotPersistableException e) {
	            object = new NakedError("Failed to create instance of " + this);	
	            LOG.error("Failed to create instance of " + this, e);
	        }
        }

        return object;
    }

    private void debugAboutDetail(StringBuffer text, Member member, About about) {
        text.append("    " + member.toString() + "\n");

        String desc = about.getDescription();

        if (desc != null && !desc.equals("")) {
            text.append("       desc:  " + desc + "\n");
        }

        String aboutDesc = about.debug();

        if (aboutDesc != null && !aboutDesc.equals("")) {
            text.append("       about: " + aboutDesc + "\n");
        }
    }

    public String debugInterface() {
        StringBuffer text = new StringBuffer();

        // list fields
        Field[] fields = getFields();

        text.append("  Fields" + "\n");

        if (fields.length == 0) {
            text.append("    none\n");
        }

        SecurityContext context = new SecurityContext();
        NakedObject example = acquireInstance();//pattern();
		for (int i = 0; i < fields.length; i++) {
            //text.append("    " + attributes[i].toString() + "\n");
            if (fields[i] instanceof Value) {
                Value f = (Value) fields[i];
                debugAboutDetail(text, f, f.getAbout(context, example));
            } else if (fields[i] instanceof OneToManyAssociation) {
                OneToManyAssociation f = (OneToManyAssociation) fields[i];
                debugAboutDetail(text, f, f.getAbout(context, example));
            } else if (fields[i] instanceof OneToOneAssociation) {
                OneToOneAssociation f = (OneToOneAssociation) fields[i];
                debugAboutDetail(text, f, f.getAbout(context, example, null));
            }
        }
        
        text.append("\n  Object Actions" + "\n");

        if (objectActions.length == 0) {
            text.append("    none\n");
        }

        for (int i = 0; i < objectActions.length; i++) {
            debugAboutDetail(text, objectActions[i], objectActions[i].getAbout(context, example));
        }

        text.append("\n  Class Actions" + "\n");

        if (classActions.length == 0) {
            text.append("    none\n");
        }

        for (int i = 0; i < classActions.length; i++) {
            debugAboutDetail(text, classActions[i],
                    classActions[i].getAbout(context, example));
        }

        // return as string
        text.append("\n");

        return text.toString();
    }
    
    private Action getAction(Action[] availableActions, Action.Type type, String name, NakedClass[] parameters) {
   	outer:
    	    for (int i = 0; i < availableActions.length; i++) {
    	        Action action = availableActions[i];
    	        if (action.getType().equals(type)) {
    	            String cname = action.getName();
                    if(name == null || cname.equals(name)) {
    	                if(action.parameters().length == parameters.length) {
    	                    for (int j = 0; j < parameters.length; j++) {
    	                        if(action.parameters()[j] != parameters[j]) {
    	                            continue outer;
    	                        }
    	                    }
	                        return action;
    	                }
    	            }
            }
        }
        
    	return null;
    }
    
    private Action[] getActions(Action[] availableActions, Action.Type type, int noParameters) {
       Vector actions = new Vector();
        for (int i = 0; i < availableActions.length; i++) {
            Action action = availableActions[i];
            if (action.getType().equals(type) && noParameters == -1 || action.parameters().length == noParameters) {
                actions.addElement(action);
            }
        }
        
        Action[] results = new Action[actions.size()];
        actions.copyInto(results);
        return results;
    }
 
    public final About getClassAbout() {
        About about = classAbout();

        if (about != null) {
            return about;
        } else {
        	return ClassAbout.INSTANTIABLE;
        }
    }
    public Action getClassAction(Action.Type type, NakedClass[] parameters) {
        reflector();
    	return getAction(classActions, type, null, parameters);        
    }
 
    public Action getClassAction(Action.Type type, String name) {
        return getClassAction(type, name, new NakedClass[0]);
    }
   
    public Action getClassAction(Action.Type type, String name, NakedClass[] parameters) {
        reflector();
    	return getAction(classActions, type, name, parameters);        
    }
 
    public Action[] getClassActions(Action.Type type) {
        reflector();
    	return getActions(classActions, type, -1);
    }
    
    public Action[] getClassActions(Action.Type type, int noParameters) {
        reflector();
     	return getActions(objectActions, type, noParameters);
    }

    public Field getField(String name) {
        reflector();
    	for (int i = 0; i < fields.length; i++) {
            if (fields[i].getName().equals(name)) {
                return fields[i];
            }
        }

        throw new NakedClassException("No field called " + name + " in " +
            getSingularName());
    }

    public Field[] getFields() {
       	reflector();
        return fields;
    }

    public String getIconName() {
        return getShortName();
    }

    public NakedClass getNakedClass() {
        return SELF;
    }

    public TextString getName() {
        return className;
    }

    /**
       Returns the name of the NakedClass.  This is the fully qualified name of the Class object that
       this object represents (i.e. it includes the package name).
     */
    public String fullName() {
        return getName().title().toString();
    }
    
    public Action getObjectAction(Action.Type type, NakedClass[] parameters) {
        reflector();
    	return getAction(objectActions, type, null, parameters);
    }
    
    public Action getObjectAction(Action.Type type, String name) {
        return getObjectAction(type, name, new NakedClass[0]);
    }
    
    public Action getObjectAction(Action.Type type, String name, NakedClass[] parameters) {
        reflector();
        return getAction(objectActions, type, name, parameters);
    }

    public Action[] getObjectActions(Action.Type type) {
        reflector();
     	return getActions(objectActions, type, -1);
    }
    
    public Action[] getObjectActions(Action.Type type, int noParameters) {
    	reflector();
    	return getActions(objectActions, type, noParameters);
    }

    /**
    Returns the short name (with spacing) for this object in a pluralised form.  The plural from is obtained from the defining classes
    pluralName method, if it exists, or by adding 's', 'es', or 'ies dependending of the name's ending.
    */
    public final String getPluralName() {
        String pluralName = reflector().pluralName();

        if (pluralName != null) {
            return pluralName;
        } else {
            String name = getSingularName();

            if (name.endsWith("y")) {
                name = name.substring(0, name.length() - 1) + "ies";
            } else if (name.endsWith("s")) {
                name += "es";
            } else {
                name += 's';
            }

            return name;
        }
    }

    /**
       Returns the class name without the package. Removes the text up to, and including the last period (".").
     */
    public String getShortName() {
        return reflector().shortName();
    }

    /**
       Returns the short name (with spacing) of this NakedClass object.  This is the objects name with package name removed.

       <p>Removes the text up to, and including the last period (".").</p>
     */
    public String getSingularName() {
        String singularName = reflector().singularName();

        return singularName != null ? singularName: getShortName();
    }

    public TextString getReflector() {
        return reflectorClass;
    }

    public Field[] getVisibleFields(NakedObject object) {
        if (this.viewFields != null) {
            return viewFields;
        }
        
        reflector();
        SecurityContext context = Session.getSession().getSecurityContext();

        // 	NakedObject pattern = pattern();
        Field[] viewFields = new Field[fields.length];
        int v = 0;

        for (int i = 0; i < fields.length; i++) {
            boolean useField = fields[i].canAccess(context, object);

            if (useField) {
                viewFields[v++] = fields[i];
            }
        }

        Field[] selectedFields = new Field[v];

        for (int i = 0; i < selectedFields.length; i++) {
            selectedFields[i] = viewFields[i];
        }

        this.viewFields = selectedFields;

        return selectedFields;
    }

    public boolean hasSubclasses() {
        return subclasses != null;
    }

    void init(Reflector reflector, String superclass, Field[] fields, Action[] objectActions, Action[] classActions) {
    	createPresistent = Configuration.getInstance().getBoolean("nakedclass.create-persistent", true);
    	
        LOG.debug("NakedClass " + this);
    	this.reflector = reflector;
    	if(superclass != null) {
	    	this.superclass = NakedClassManager.getInstance().getNakedClass(superclass);
	    	LOG.debug("  Superclass " + superclass);
	    	this.superclass.subclasses.addSubclass(this);
    	}
    	this.fields = fields;
    	this.objectActions = objectActions;
    	this.classActions = classActions;
    }

    /**
    Returns true if this NakedClass represents a collection -  of, or subclassed from, NakedCollection.
    */
    public boolean isCollection() {
        return reflector().isCollection();
    }

    /**
     * Determines if this class respresents the same class, or a subclass, of the specified class. 
     */
	public boolean isOfType(NakedClass cls) {
		// TODO check subclasses of cls and compare against this
		return cls == this;
	}

 
    public NakedObject pattern() {
        LOG.debug("pattern requested for " + this);
        pattern = createFinder();

        return pattern;
    }

    private Reflector reflector() {
		if(reflector == null) {
			if(getName().isEmpty() && getReflector().isEmpty()) {
				throw new  NakedObjectRuntimeException("No class name or reflector class specified: " + this);
			} else if(getName().isEmpty()) {
				throw new  NakedObjectRuntimeException("No naked class specified: " + this);
			} else if(getReflector().isEmpty()) {
				throw new  NakedObjectRuntimeException("No reflector class specified: " + this);
			}
			
	    	NakedClassManager.getInstance().reflect(this);
			
			if(reflector == null) {
				throw new NakedObjectRuntimeException();
			}
		}
		return reflector;
	}

    public NakedClass superclass() {
        return superclass;
    }
    
    public NakedClass[] subclasses() {
        return subclasses.toArray();
    }
    
    public Summary summary() {
        return new Summary("NakedClass: ").append(getName()).concat("/", getReflector());
    }

    public Title title() {
        return new Title(getPluralName());
    }

    
    public String toString() {
        StringBuffer s = new StringBuffer();

        s.append("NakedClass");
        s.append(" [");

        // type of object - EO, Primitive, Collection, with Status etc
        // Persistent/transient & Resolved or not
        s.append(isPersistent() ? "P" : (isFinder() ? "F" : "T"));
        s.append(isResolved() ? "R" : "-");

        // obect identifier
        if (getOid() != null) {
            s.append(":");
            s.append(getOid().toString().toUpperCase());
        } else {
            s.append(":-");
        }


        // title
        s.append(' ');
        s.append(className.title());
        
         s.append("]");

        s.append("  " + Long.toHexString(super.hashCode()).toUpperCase());

        return s.toString();
    }   
    
 
    private class NakedClassList {
        private Vector classes = new Vector();

        public void addSubclass(NakedClass cls) {
            classes.addElement(cls);
        }

        public NakedClass[] toArray() {
            NakedClass[] classesArray = new NakedClass[classes.size()];
            classes.copyInto(classesArray);
            return classesArray;
        }
    }

    
}

/*
Naked Objects - a framework that exposes behaviourally complete
business objects directly to the user.
Copyright (C) 2000 - 2003  Naked Objects Group Ltd

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
