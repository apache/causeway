package org.nakedobjects.object;

import org.nakedobjects.object.control.About;
import org.nakedobjects.object.control.ClassAbout;
import org.nakedobjects.object.reflect.Action;
import org.nakedobjects.object.reflect.ActionSpecification;
import org.nakedobjects.object.reflect.FieldSpecification;
import org.nakedobjects.object.reflect.Member;
import org.nakedobjects.object.reflect.MemberSpecification;
import org.nakedobjects.object.reflect.NakedObjectSpecificationException;
import org.nakedobjects.object.reflect.NameConvertor;
import org.nakedobjects.object.reflect.OneToManyAssociation;
import org.nakedobjects.object.reflect.OneToManyAssociationSpecification;
import org.nakedobjects.object.reflect.OneToOneAssociation;
import org.nakedobjects.object.reflect.OneToOneAssociationSpecification;
import org.nakedobjects.object.reflect.Reflector;
import org.nakedobjects.object.reflect.ValueField;
import org.nakedobjects.object.reflect.ValueFieldSpecification;
import org.nakedobjects.object.reflect.defaults.JavaReflector;
import org.nakedobjects.object.security.Session;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.log4j.Logger;


public final class NakedObjectSpecification {
    private final static Logger LOG = Logger.getLogger(NakedObjectSpecification.class);
    private final static Hashtable classes = new Hashtable();
    private static ReflectionFactory reflectionFactory;

    public static void setReflectionFactory(ReflectionFactory reflectionFactory) {
        NakedObjectSpecification.reflectionFactory = reflectionFactory;
    }
    
    private ActionSpecification[] classActions = new ActionSpecification[0];
    private ActionSpecification[] objectActions = new ActionSpecification[0];
    private FieldSpecification[] fields = new FieldSpecification[0];
    private FieldSpecification[] viewFields;

    private Reflector reflector;
    private ClassList subclasses = new ClassList();
    private NakedObjectSpecification superclass;
	
	private NakedObjectSpecification() {	}


    /**
    Creates an object of the type represented by this object. This method only creates a java object (using newInstance
    on the Class object returned using the getJavaType method).
    */
    public Naked acquireInstance() {
    	LOG.debug("acquire instance of " + getShortName());
        return reflector.acquireInstance();
    }

    protected About classAbout() {
        return reflector.classAbout();
    }
    

    /**
     * Creates a finder object with no references or values.
     * @return NakedObject
     */
    public NakedObject createFinder() {
        NakedObject finder = (NakedObject) acquireInstance();
        FieldSpecification[] fields = getFields();
        for (int fld = 0; fld < fields.length; fld++) {
            fields[fld].clear(finder);
        }
        return finder;
    }

    private void debugAboutDetail(StringBuffer text, MemberSpecification member) {
        text.append("    " + member.toString() + "\n");
    }

    public String debugInterface() {
        StringBuffer text = new StringBuffer();

        // list fields
        FieldSpecification[] fields = getFields();

        text.append("  Fields" + "\n");

        if (fields.length == 0) {
            text.append("    none\n");
        }

        for (int i = 0; i < fields.length; i++) {
            //text.append("    " + attributes[i].toString() + "\n");
            if (fields[i] instanceof ValueFieldSpecification) {
                ValueFieldSpecification f = (ValueFieldSpecification) fields[i];
                debugAboutDetail(text, f);
            } else if (fields[i] instanceof OneToManyAssociationSpecification) {
                OneToManyAssociationSpecification f = (OneToManyAssociationSpecification) fields[i];
                debugAboutDetail(text, f);
            } else if (fields[i] instanceof OneToOneAssociationSpecification) {
                OneToOneAssociationSpecification f = (OneToOneAssociationSpecification) fields[i];
                debugAboutDetail(text, f);
            }
        }
        
        text.append("\n  Object Actions" + "\n");

        if (objectActions.length == 0) {
            text.append("    none\n");
        }

        for (int i = 0; i < objectActions.length; i++) {
            ActionSpecification action = objectActions[i];
            debugAboutDetail(text, action);
        }

        text.append("\n  Class Actions" + "\n");

        if (classActions.length == 0) {
            text.append("    none\n");
        }

        for (int i = 0; i < classActions.length; i++) {
            debugAboutDetail(text, classActions[i]);
        }

        // return as string
        text.append("\n");

        return text.toString();
    }
    
    private ActionSpecification getDefaultAction(ActionSpecification[] availableActions, ActionSpecification.Type type, NakedObjectSpecification[] parameters) {
        outer:
            for (int i = 0; i < availableActions.length; i++) {
            ActionSpecification action = availableActions[i];
            if (action.getActionType().equals(type)) {
                if (action.parameters().length == parameters.length) {
                    for (int j = 0; j < parameters.length; j++) {
                        if (action.parameters()[j] != parameters[j]) {
                            continue outer;
                        }
                    }
                    return action;
                }

            }
        }
        
    	return null;
   }
    
    private ActionSpecification getAction(ActionSpecification[] availableActions, ActionSpecification.Type type, String name, NakedObjectSpecification[] parameters) {
        if(name == null) {
           return null;
        }
        
        String searchName = searchName(name);  
        outer:
            for (int i = 0; i < availableActions.length; i++) {
                ActionSpecification action = availableActions[i];
                if (action.getActionType().equals(type)) {
                    if(action.getName().equals(searchName)) {
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
    
    private ActionSpecification[] getActions(ActionSpecification[] availableActions, ActionSpecification.Type type, int noParameters) {
       Vector actions = new Vector();
        for (int i = 0; i < availableActions.length; i++) {
            ActionSpecification action = availableActions[i];
            if (action.getActionType().equals(type) && (noParameters == -1 || action.parameters().length == noParameters)) {
                actions.addElement(action);
            }
        }
        
        ActionSpecification[] results = new ActionSpecification[actions.size()];
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

    public ActionSpecification getClassAction(ActionSpecification.Type type, String name) {
        return getClassAction(type, name, new NakedObjectSpecification[0]);
    }
   
    public ActionSpecification getClassAction(ActionSpecification.Type type, String name, NakedObjectSpecification[] parameters) {
        if(name == null) {
            return getDefaultAction(classActions, type, parameters);
        } else {
            return getAction(classActions, type, name, parameters);
        }
    }
 
    public ActionSpecification[] getClassActions(ActionSpecification.Type type) {
        return getActions(classActions, type, -1);
    }
    
    public ActionSpecification[] getClassActions(ActionSpecification.Type type, int noParameters) {
        return getActions(classActions, type, noParameters);
    }

    public FieldSpecification getField(String name) {
        String searchName = searchName(name);
        
        for (int i = 0; i < fields.length; i++) {
            if (fields[i].getName().equals(searchName)) {
                return fields[i];
            }
        }

        throw new NakedObjectSpecificationException("No field called '" + name + "' in '" +
            getSingularName() + "'");
    }

    private String searchName(String name) {
        return NameConvertor.simpleName(name);
    }

    public FieldSpecification[] getFields() {
       	return fields;
    }

    /**
       Returns the name of the NakedClass.  This is the fully qualified name of the Class object that
       this object represents (i.e. it includes the package name).
     */
    public String getFullName() {
        return reflector.fullName();
    }
    
    public ActionSpecification getObjectAction(ActionSpecification.Type type, String name) {
        return getObjectAction(type, name, new NakedObjectSpecification[0]);
    }
    
    public ActionSpecification getObjectAction(ActionSpecification.Type type, String name, NakedObjectSpecification[] parameters) {
        if(name == null) {
            return getDefaultAction(objectActions, type, parameters);
        } else {
            return getAction(objectActions, type, name, parameters);
        }
    }

    public ActionSpecification[] getObjectActions(ActionSpecification.Type type) {
        return getActions(objectActions, type, -1);
    }
    
    public ActionSpecification[] getObjectActions(ActionSpecification.Type type, int noParameters) {
    	return getActions(objectActions, type, noParameters);
    }

    /**
    Returns the short name (with spacing) for this object in a pluralised form.  The plural from is obtained from the defining classes
    pluralName method, if it exists, or by adding 's', 'es', or 'ies dependending of the name's ending.
    */
    public final String getPluralName() {
        String pluralName = reflector.pluralName();

        if (pluralName != null) {
            return pluralName;
        } else {
            return NameConvertor.pluralName(getSingularName());
        }
    }

    /**
       Returns the class name without the package. Removes the text up to, and including the last period (".").
     */
    public String getShortName() {
        return reflector.shortName();
    }

    /**
       Returns the short name (with spacing) of this NakedClass object.  This is the objects name with package name removed.

       <p>Removes the text up to, and including the last period (".").</p>
     */
    public String getSingularName() {
        String singularName = reflector.singularName();

        return singularName != null ? singularName: NameConvertor.naturalName(getShortName());
    }

    public FieldSpecification[] getVisibleFields(NakedObject object, Session session) {
        if (this.viewFields != null) {
            return viewFields;
        }
        
        FieldSpecification[] viewFields = new FieldSpecification[fields.length];
        int v = 0;
        for (int i = 0; i < fields.length; i++) {
            boolean useField = fields[i].canAccess(session, object);

            if (useField) {
                viewFields[v++] = fields[i];
            }
        }

        FieldSpecification[] selectedFields = new FieldSpecification[v];
        for (int i = 0; i < selectedFields.length; i++) {
            selectedFields[i] = viewFields[i];
        }
        this.viewFields = selectedFields;

        return selectedFields;
    }

    public boolean hasSubclasses() {
        return subclasses != null;
    }

    void init(Reflector reflector, String superclass, FieldSpecification[] fields, ActionSpecification[] objectActions, ActionSpecification[] classActions) {
     	if(reflector == null) {
     	    throw new NullPointerException("No reflector specified");
     	}
        LOG.debug("NakedClass " + this);
    	this.reflector = reflector;
    	if(superclass != null) {
	    	this.superclass = NakedObjectSpecification.getSpecification(superclass);
	    	LOG.debug("  Superclass " + superclass);
	    	this.superclass.subclasses.addSubclass(this);
    	}
    	if(isValue() && fields.length > 0) {
    	    LOG.warn("Naked values cannot have fields, they will be ignored");
    	} else {
    	    this.fields = fields;
    	}
    	this.objectActions = objectActions;
    	this.classActions = classActions;
    }

    /**
    Returns true if this NakedClass represents a collection -  of, or subclassed from, NakedCollection.
    */
    public boolean isCollection() {
        return reflector.isCollection();
    }

    /**
     * Determines if this class respresents the same class, or a subclass, of the specified class. 
     */
	public boolean isOfType(NakedObjectSpecification cls) {
	    if(cls == this) {
		    return true;
		} else {
		    if(superclass != null) {
		        return superclass.isOfType(cls);
		    }
		}
		return false;
	}

    public NakedObjectSpecification superclass() {
        return superclass;
    }
    
    public NakedObjectSpecification[] subclasses() {
        return subclasses.toArray();
    }
     
    public String toString() {
        StringBuffer s = new StringBuffer();

        s.append("NakedObjectSpecification");
        if(reflector != null) {
            s.append(" [name=");
            s.append(getFullName());
            s.append(",fields=");
            s.append(fields.length);
            s.append(",object methods=");
            s.append(objectActions.length);
            s.append(",class methods=");
            s.append(classActions.length);
            s.append("]");
        } else {
            s.append("[no relector set up]");
        }
       
        s.append("  " + Long.toHexString(super.hashCode()).toUpperCase());

        return s.toString();
    }

    

    private class ClassList {
        private Vector classes = new Vector();

        public void addSubclass(NakedObjectSpecification cls) {
            classes.addElement(cls);
        }

        public NakedObjectSpecification[] toArray() {
            NakedObjectSpecification[] classesArray = new NakedObjectSpecification[classes.size()];
            classes.copyInto(classesArray);
            return classesArray;
        }
    }


    public boolean isAbstract() {
        return reflector.isAbstract();
    }

    public boolean isPartOf() {
        return reflector.isPartOf();
    }

    public boolean isValue() {
        return reflector.isValue();
    }

    public boolean isObject() {
        return reflector.isObject();
    }

    public static NakedObjectSpecification getSpecification(Class cls) {
        return getSpecification(cls.getName());
    }	
    
    public static NakedObjectSpecification getSpecification(String className) {
        if(classes.containsKey(className)) {
            return (NakedObjectSpecification) classes.get(className);
        } else {
            LOG.info("Initialising NakedClass for " + className);
            NakedObjectSpecification nakedClass = new NakedObjectSpecification();
            classes.put(className, nakedClass);
            nakedClass.reflect(className);
            //nakedClass.reflector = classLoader.getReflector(className);
	        return nakedClass;
        }
    }    

    public static NakedObjectSpecification[] getAllSpecifications() {
        int size = classes.size();
        NakedObjectSpecification[] cls = new NakedObjectSpecification[size];
        Enumeration e = classes.elements();
        int i = 0;
        while (e.hasMoreElements()) {
            cls[i++] = (NakedObjectSpecification) e.nextElement();
        }
        return cls;
    }

    private void reflect( String nakedObjectClassName) {
        NakedObjectSpecification cls = this;
        
        LOG.debug("reflecting on " + cls);

        String reflectorClassName = JavaReflector.class.getName();

        Reflector reflector;
        Class reflectorClass;
        Constructor cons = null;
        try {
            reflectorClass = Class.forName(reflectorClassName);
            cons = reflectorClass.getConstructor(new Class[] { String.class });
            try {
                reflector = (Reflector) cons.newInstance(new Object[] { nakedObjectClassName });
            } catch (InvocationTargetException e) {               
                if (e.getTargetException() instanceof ClassNotFoundException) {
                    throw (ClassNotFoundException) e.getTargetException();
                } else {
                    throw new NakedObjectRuntimeException("failed to create reflector " + reflectorClassName + " for "
                            + nakedObjectClassName + ": " + e.getTargetException().getMessage(), e.getTargetException());
                }
            }
        } catch (InstantiationException e) {
            throw new NakedObjectRuntimeException("Failed to instantiate class: " + cls, e);
        } catch (IllegalAccessException e) {
            throw new NakedObjectRuntimeException("Unable to access class or constructor: " + cls, e);
        } catch (SecurityException e) {
            throw new NakedObjectRuntimeException("", e);
        } catch (NoSuchMethodException e) {
            throw new NakedObjectRuntimeException("No default constructor in " + cls, e);
        } catch (IllegalArgumentException e) {
            throw new NakedObjectRuntimeException(e.getMessage(), e);
        } catch (ClassNotFoundException e) {
            throw new NakedObjectRuntimeException("Class not found: " + cls, e);
        }

        String nakedClassName = nakedObjectClassName;

        Action delegates[];

        delegates = reflector.actions(Reflector.OBJECT);
        String[] order = reflector.actionSortOrder();
        ActionSpecification[] objectActions = createActions(reflector, nakedClassName, delegates, order);

        delegates = reflector.actions(Reflector.CLASS);
        order = reflector.classActionSortOrder();
        ActionSpecification[] classActions = createActions( reflector, nakedClassName, delegates, order);

        Member fieldDelegates[] = reflector.fields();
        FieldSpecification[] fieldVector = createFields(fieldDelegates);
        FieldSpecification[] fields = (FieldSpecification[]) orderArray(FieldSpecification.class, fieldVector, reflector.fieldSortOrder(), nakedClassName);

        String superclass = reflector.getSuperclass();
        
        cls.init(reflector, superclass, fields, objectActions, classActions);

    }
  
    
    private ActionSpecification[] createActions(Action[] actions) {
        ActionSpecification actionChains[] = new ActionSpecification[actions.length];

        for (int i = 0; i < actions.length; i++) {
            actionChains[i] = reflectionFactory.createAction(actions[i]);
        }

        return actionChains;
    }


    private FieldSpecification[] createFields(Member fields[]) {
        FieldSpecification[] fieldChains = new FieldSpecification[fields.length];

        for (int i = 0; i < fields.length; i++) {

            Object object = fields[i];

            if (object instanceof ValueField) {
                fieldChains[i] = reflectionFactory.createField((ValueField) object);

            } else if (object instanceof OneToOneAssociation) {
                fieldChains[i] = reflectionFactory.createField((OneToOneAssociation) object);

            } else if (object instanceof OneToManyAssociation) {
                fieldChains[i] = reflectionFactory.createField((OneToManyAssociation) object);

            } else {
                throw new NakedObjectRuntimeException();
            }
        }

        return fieldChains;
    }

    

    private ActionSpecification[] createActions(Reflector reflector, String nakedClassName, Action[] delegates,
            String[] order) {
        ActionSpecification[] actions = createActions(delegates);
        ActionSpecification[] objectActions = (ActionSpecification[]) orderArray(ActionSpecification.class, actions, order, nakedClassName);
        return objectActions;
    }

    private MemberSpecification[] orderArray(Class memberType, MemberSpecification[] original, String[] order, String nakedClassName) {
        if (order == null) {
            return original;

        } else {
            for (int i = 0; i < order.length; i++) {
                order[i] = NameConvertor.simpleName(order[i]);
            }
            
	        MemberSpecification[] ordered = (MemberSpecification[]) Array.newInstance(memberType, original.length);

	        // work through each order element and find, if there is one, a
            // matching member.
            int orderedIndex = 0;
            ordering: for (int orderIndex = 0; orderIndex < order.length; orderIndex++) {
                for (int memberIndex = 0; memberIndex < original.length; memberIndex++) {
                    MemberSpecification member = original[memberIndex];
                    if (member == null) {
                        continue;
                    }
                    if (member.getName().equalsIgnoreCase(order[orderIndex])) {
                        ordered[orderedIndex++] = original[memberIndex];
                        original[memberIndex] = null;

                        continue ordering;
                    }
                }

                LOG.error("Invalid ordering element '" + order[orderIndex] + "' in " + nakedClassName);
            }

            MemberSpecification[] results = (MemberSpecification[]) Array.newInstance(memberType, original.length);
            int index = 0;
            for (int i = 0; i < ordered.length; i++) {
                MemberSpecification member = ordered[i];
                if (member != null) {
                    results[index++] = member;
                }
            }
            for (int i = 0; i < original.length; i++) {
                MemberSpecification member = original[i];
                if (member != null) {
                    results[index++] = member;
                }
            }

            return results;
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
