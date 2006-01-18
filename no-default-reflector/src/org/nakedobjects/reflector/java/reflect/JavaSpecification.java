package org.nakedobjects.reflector.java.reflect;

import org.nakedobjects.object.Action;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectField;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.NakedObjectSpecificationException;
import org.nakedobjects.object.NakedObjectSpecificationLoader;
import org.nakedobjects.object.NakedObjects;
import org.nakedobjects.object.Persistable;
import org.nakedobjects.object.Action.Type;
import org.nakedobjects.object.control.Hint;
import org.nakedobjects.object.reflect.NameConvertor;
import org.nakedobjects.object.reflect.ObjectTitle;
import org.nakedobjects.object.reflect.ReflectionException;
import org.nakedobjects.object.reflect.ReflectionPeerBuilder;
import org.nakedobjects.utility.NakedObjectRuntimeException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Vector;

import org.apache.log4j.Logger;


public class JavaSpecification implements NakedObjectSpecification {
    private class SubclassList {
        private Vector classes = new Vector();

        public void addSubclass(NakedObjectSpecification subclass) {
            classes.addElement(subclass);
        }

        public boolean hasSubclasses() {
            return !classes.isEmpty();
        }

        public NakedObjectSpecification[] toArray() {
            NakedObjectSpecification[] classesArray = new NakedObjectSpecification[classes.size()];
            classes.copyInto(classesArray);
            return classesArray;
        }
    }

    private final static Logger LOG = Logger.getLogger(JavaSpecification.class);

    private static final Object[] NO_PARAMETERS = new Object[0];
    private Action[] classActions;
    private Hint classHint;
    private Method clearDirtyMethod;
    private NakedObjectField[] fields;
    private String fullName;
    private NakedObjectSpecification[] interfaces;
    private JavaIntrospector introspector;
    private boolean isAbstract;
    private boolean isCollection;
    private Method isDirtyMethod;
    private boolean isLookup;
    private boolean isObject;
    private boolean isValue;
    private Method markDirtyMethod;
    private Action[] objectActions;
    private Persistable persistable;
    private String pluralName;
    private String shortName;
    private String singularName;
    private SubclassList subclasses;
    private NakedObjectSpecification superclass;

    private ObjectTitle title;

    private NakedObjectField[] accessibleFields;

    public JavaSpecification(Class cls, ReflectionPeerBuilder builder) {
        introspector = new JavaIntrospector(cls, builder);
        subclasses = new SubclassList();
    }

    public void addSubclass(NakedObjectSpecification subclass) {
        subclasses.addSubclass(subclass);
    }

    public void clearDirty(NakedObject object) {
        if (clearDirtyMethod == null) {
            return;
        }

        try {
            clearDirtyMethod.invoke(object.getObject(), NO_PARAMETERS);
        } catch (IllegalArgumentException e) {
            throw new NakedObjectRuntimeException(e);
        } catch (IllegalAccessException e) {
            LOG.error("illegal access of " + clearDirtyMethod, e);
        } catch (InvocationTargetException e) {
            JavaMember.invocationException("Exception executing " + clearDirtyMethod, e);
        }
    }

    public void deleted(NakedObject object) {
    // TODO not supported yet
    }

    private Action getAction(Action[] availableActions, Action.Type type, String name, NakedObjectSpecification[] parameters) {
        if (name == null) {
            return null;
        }

        String searchName = searchName(name);
        outer: for (int i = 0; i < availableActions.length; i++) {
            Action action = availableActions[i];
            if (action.getType().equals(type)) {
                if (action.getId().equals(searchName)) {
                    if (action.getParameterTypes().length == parameters.length) {
                        for (int j = 0; j < parameters.length; j++) {
                            if (!parameters[j].isOfType(action.getParameterTypes()[j])) {
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
            Type type2 = action.getType();
            if (type2 == Action.SET || type2.equals(type) && (noParameters == -1 || action.getParameterTypes().length == noParameters)) {
                actions.addElement(action);
            }
        }

        Action[] results = new Action[actions.size()];
        actions.copyInto(results);
        return results;
    }

    public Action getClassAction(Action.Type type, String name) {
        return getClassAction(type, name, new NakedObjectSpecification[0]);
    }

    public Action getClassAction(Action.Type type, String name, NakedObjectSpecification[] parameters) {
        if (name == null) {
            return getDefaultAction(classActions, type, parameters);
        } else {
            return getAction(classActions, type, name, parameters);
        }
    }

    public Action[] getClassActions(Action.Type type) {
        return getActions(classActions, type, -1);
    }

    public Hint getClassHint() {
        if (classHint == null) {
            Hint hint = null;
            if (superclass != null) {
                hint = superclass.getClassHint();
            }
            return hint;
        } else {
            return classHint;
        }
    }

    private Action getDefaultAction(Action[] availableActions, Action.Type type, NakedObjectSpecification[] parameters) {
        outer: for (int i = 0; i < availableActions.length; i++) {
            Action action = availableActions[i];
            if (action.getType().equals(type)) {
                if (action.getParameterTypes().length == parameters.length) {
                    for (int j = 0; j < parameters.length; j++) {
                        if (!parameters[j].isOfType(action.getParameterTypes()[j])) {
                            continue outer;
                        }
                    }
                    return action;
                }
            }
        }
        return null;
    }

    public Object getExtension(Class cls) {
        return null;
    }

    public Class[] getExtensions() {
        return new Class[0];
    }

    public NakedObjectField getField(String name) {
        // TODO put fields into hash
        String searchName = searchName(name);
        for (int i = 0; i < fields.length; i++) {
            if (fields[i].getId().equals(searchName)) {
                return fields[i];
            }
        }
        throw new NakedObjectSpecificationException("No field called '" + name + "' in '" + getSingularName() + "'");
    }

    public Object getFieldExtension(String name, Class cls) {
        return null;
    }

    public Class[] getFieldExtensions(String name) {
        return new Class[0];
    }

    public NakedObjectField[] getFields() {
        return fields;
    }

    public String getFullName() {
        return fullName;
    }

    public Action getObjectAction(Action.Type type, String name) {
        return getObjectAction(type, name, new NakedObjectSpecification[0]);
    }

    public Action getObjectAction(Action.Type type, String name, NakedObjectSpecification[] parameters) {
        if (name == null) {
            return getDefaultAction(objectActions, type, parameters);
        } else {
            return getAction(objectActions, type, name, parameters);
        }
    }

    public Action[] getObjectActions(Action.Type type) {
        return getActions(objectActions, type, -1);
    }

    public String getPluralName() {
        return pluralName;
    }

    public String getShortName() {
        return shortName;
    }

    public String getSingularName() {
        return singularName;
    }

    public String getTitle(NakedObject object) {
        return title.title(object);
    }

    public NakedObjectField[] getVisibleFields(NakedObject object) {
        getAccessibleFields();
        NakedObjectField[] selectedFields = new NakedObjectField[accessibleFields.length];
        int v = 0;
        for (int i = 0; i < accessibleFields.length; i++) {
            if (object.isVisible(accessibleFields[i]).isAllowed()) {
                selectedFields[v++] = accessibleFields[i];
            }
        }

        NakedObjectField[] visibleFields = new NakedObjectField[v];
        System.arraycopy(selectedFields, 0, visibleFields, 0, v);
        return visibleFields;
    }

    public NakedObjectField[] getAccessibleFields() {
        if (accessibleFields == null) {
            NakedObjectField[] selectedFields = new NakedObjectField[fields.length];
            int v = 0;
            for (int i = 0; i < fields.length; i++) {
                boolean isAccessible = !fields[i].isHidden() && fields[i].isAuthorised();
                if (isAccessible) {
                    selectedFields[v++] = fields[i];
                }
            }

            accessibleFields = new NakedObjectField[v];
            System.arraycopy(selectedFields, 0, accessibleFields, 0, v);
        }
        return accessibleFields;
    }

    public boolean hasSubclasses() {
        return subclasses.hasSubclasses();
    }

    public NakedObjectSpecification[] interfaces() {
        return interfaces;
    }

    public void introspect() {
        if(introspector == null) {
            throw new  ReflectionException("Introspection already taken place, cannot introspect again");
        }
        
        introspector.introspect();

        classHint = introspector.classHint();
        clearDirtyMethod = introspector.getClearDirtyMethod();
        isDirtyMethod = introspector.getIsDirtyMethod();
        markDirtyMethod = introspector.getMarkDirtyMethod();

        classActions = introspector.getClassActions();
        fields = introspector.getFields();
        objectActions = introspector.getObjectActions();

        fullName = introspector.getFullName();
        pluralName = introspector.pluralName();
        singularName = introspector.singularName();
        shortName = introspector.shortName();

        title = introspector.title();

        isAbstract = introspector.isAbstract();
        isCollection = introspector.isCollection();
        isLookup = introspector.isLookup();
        isObject = introspector.isObject();
        isValue = introspector.isValue();

        persistable = introspector.persistable();
        
        String superclassName = introspector.getSuperclass();
        String[] interfaceNames = introspector.getInterfaces();

        NakedObjectSpecificationLoader loader = NakedObjects.getSpecificationLoader();
        if (superclassName != null) {
            superclass = loader.loadSpecification(superclassName);
            if (superclass != null) {
                LOG.debug("  Superclass " + superclassName);
                // superclass.subclasses.addSubclass(this);
                superclass.addSubclass(this);
            }
        }

        interfaces = new NakedObjectSpecification[interfaceNames.length];
        for (int i = 0; i < interfaceNames.length; i++) {
            interfaces[i] = loader.loadSpecification(interfaceNames[i]);
            // interfaces[i].subclasses.addSubclass(this);
            interfaces[i].addSubclass(this);
        }

        introspector = null;
    }

    public boolean isAbstract() {
        return isAbstract;
    }

    public boolean isCollection() {
        return isCollection;
    }

    public boolean isDirty(NakedObject object) {
        if (isDirtyMethod == null) {
            return false;
        }

        try {
            Boolean isDirty = (Boolean) isDirtyMethod.invoke(object.getObject(), NO_PARAMETERS);
            return isDirty.booleanValue();
        } catch (IllegalArgumentException e) {
            throw new NakedObjectRuntimeException(e);
        } catch (IllegalAccessException e) {
            LOG.error("illegal access of " + isDirtyMethod, e);
            return false;
        } catch (InvocationTargetException e) {
            JavaMember.invocationException("Exception executing " + isDirtyMethod, e);
            return false;
        }
    }

    public boolean isLookup() {
        return isLookup;
    }

    public boolean isObject() {
        return isObject;
    }

    /**
     * Determines if this class respresents the same class, or a subclass, of the specified class.
     */
    public boolean isOfType(NakedObjectSpecification specification) {
        if (specification == this) {
            return true;
        } else {
            if (interfaces != null) {
                for (int i = 0, len = interfaces.length; i < len; i++) {
                    if (interfaces[i].isOfType(specification)) {
                        return true;
                    }
                }
            }
            if (superclass != null) {
                return superclass.isOfType(specification);
            }
        }
        return false;
    }

    public boolean isValue() {
        return isValue;
    }

    public void markDirty(NakedObject object) {
        if (markDirtyMethod == null) {
            return;
        }

        try {
            markDirtyMethod.invoke(object.getObject(), NO_PARAMETERS);
        } catch (IllegalArgumentException e) {
            throw new NakedObjectRuntimeException(e);
        } catch (IllegalAccessException e) {
            LOG.error("illegal access of " + isDirtyMethod, e);
        } catch (InvocationTargetException e) {
            JavaMember.invocationException("Exception executing " + isDirtyMethod, e);
        }
    }

    public Persistable persistable() {
        return persistable;
    }

    private String searchName(String name) {
        return NameConvertor.simpleName(name);
    }

    public NakedObjectSpecification[] subclasses() {
        return subclasses.toArray();
    }

    public NakedObjectSpecification superclass() {
        return superclass;
    }

}

/*
 * Naked Objects - a framework that exposes behaviourally complete business objects directly to the user.
 * Copyright (C) 2000 - 2005 Naked Objects Group Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation; either version 2 of the License, or (at your
 * option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to
 * the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address of Naked Objects Group is
 * Kingsway House, 123 Goldworth Road, Woking GU21 1NR, UK).
 */