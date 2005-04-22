package org.nakedobjects.object.defaults;

import org.nakedobjects.NakedObjects;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectRuntimeException;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.NakedObjectSpecificationException;
import org.nakedobjects.object.NakedObjectSpecificationLoader;
import org.nakedobjects.object.Persistable;
import org.nakedobjects.object.ReflectionFactory;
import org.nakedobjects.object.control.Hint;
import org.nakedobjects.object.reflect.Action;
import org.nakedobjects.object.reflect.ActionPeer;
import org.nakedobjects.object.reflect.FieldPeer;
import org.nakedobjects.object.reflect.NakedObjectField;
import org.nakedobjects.object.reflect.NakedObjectMember;
import org.nakedobjects.object.reflect.NameConvertor;
import org.nakedobjects.object.reflect.ObjectTitle;
import org.nakedobjects.object.reflect.OneToManyAssociation;
import org.nakedobjects.object.reflect.OneToManyPeer;
import org.nakedobjects.object.reflect.OneToOneAssociation;
import org.nakedobjects.object.reflect.OneToOnePeer;
import org.nakedobjects.object.reflect.PojoAdapter;
import org.nakedobjects.object.reflect.Reflector;
import org.nakedobjects.object.security.Session;

import java.lang.reflect.Array;
import java.util.Vector;

import org.apache.log4j.Logger;


public final class NakedObjectSpecificationImpl implements NakedObjectSpecification {
    private class SubclassList {
        private Vector classes = new Vector();

        public void addSubclass(NakedObjectSpecificationImpl cls) {
            classes.addElement(cls);
        }

        public NakedObjectSpecification[] toArray() {
            NakedObjectSpecification[] classesArray = new NakedObjectSpecification[classes.size()];
            classes.copyInto(classesArray);
            return classesArray;
        }
    }

    private final static Logger LOG = Logger.getLogger(NakedObjectSpecificationImpl.class);

    private static ReflectionFactory reflectionFactory;

    /**
     * Expose as a .NET property
     * 
     * @property
     */
    public static void set_ReflectionFactory(ReflectionFactory reflectionFactory) {
        NakedObjectSpecificationImpl.reflectionFactory = reflectionFactory;
    }

    public static void setReflectionFactory(ReflectionFactory reflectionFactory) {
        NakedObjectSpecificationImpl.reflectionFactory = reflectionFactory;
    }

    private Action[] classActions = new Action[0];
    private NakedObjectField[] fields = new NakedObjectField[0];
    private NakedObjectSpecificationImpl[] interfaces;
    private Action[] objectActions = new Action[0];

    private Reflector reflector;
    private SubclassList subclasses = new SubclassList();
    private NakedObjectSpecificationImpl superclass;

    private ObjectTitle title;
    private NakedObjectField[] viewFields;

    public NakedObjectSpecificationImpl() {}

    /**
     * Creates an object of the type represented by this object. This method
     * only creates a java object (using newInstance on the Class object
     * returned using the getJavaType method).
     */
    public Naked acquireInstance() {
        LOG.debug("acquire instance of " + getShortName());
        return reflector.acquireInstance();
    }

    public void clearDirty(NakedObject object) {
        reflector.clearDirty(object);
    }

    private Action[] createActions(ActionPeer[] actions) {
        Action actionChains[] = new Action[actions.length];

        for (int i = 0; i < actions.length; i++) {
            actionChains[i] = reflectionFactory.createAction(getFullName(), actions[i]);
        }

        return actionChains;
    }

    private Action[] createActions(Reflector reflector, String nakedClassName, ActionPeer[] delegates, String[] order) {
        Action[] actions = createActions(delegates);
        Action[] objectActions = (Action[]) orderArray(Action.class, actions, order, nakedClassName);
        return objectActions;
    }

    private NakedObjectField[] createFields(FieldPeer fieldPeers[]) {
        NakedObjectField[] fields = new NakedObjectField[fieldPeers.length];

        for (int i = 0; i < fieldPeers.length; i++) {

            Object object = fieldPeers[i];

            if (object instanceof OneToOnePeer) {
                fields[i] = reflectionFactory.createField(getFullName(), (OneToOnePeer) object);

            } else if (object instanceof OneToManyPeer) {
                fields[i] = reflectionFactory.createField(getFullName(), (OneToManyPeer) object);

            } else {
                throw new NakedObjectRuntimeException();
            }
        }

        return fields;
    }

    private void debugAboutDetail(StringBuffer text, NakedObjectMember member) {
        text.append("    " + member.toString() + "\n");
    }

    public String debugInterface() {
        StringBuffer text = new StringBuffer();

        // list fields
        NakedObjectField[] fields = getFields();

        text.append("  Fields" + "\n");

        if (fields.length == 0) {
            text.append("    none\n");
        }

        for (int i = 0; i < fields.length; i++) {
            //text.append(" " + attributes[i].toString() + "\n");
            if (fields[i] instanceof OneToManyAssociation) {
                OneToManyAssociation f = (OneToManyAssociation) fields[i];
                debugAboutDetail(text, f);
            } else if (fields[i] instanceof OneToOneAssociation) {
                OneToOneAssociation f = (OneToOneAssociation) fields[i];
                debugAboutDetail(text, f);
            }
        }

        text.append("\n  Object Actions" + "\n");

        if (objectActions.length == 0) {
            text.append("    none\n");
        }

        for (int i = 0; i < objectActions.length; i++) {
            Action action = objectActions[i];
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

    private Action getAction(Action[] availableActions, Action.Type type, String name, NakedObjectSpecification[] parameters) {
        if (name == null) {
            return null;
        }

        String searchName = searchName(name);
        outer: for (int i = 0; i < availableActions.length; i++) {
            Action action = availableActions[i];
            if (action.getActionType().equals(type)) {
                if (action.getName().equals(searchName)) {
                    if (action.parameters().length == parameters.length) {
                        for (int j = 0; j < parameters.length; j++) {
                            if (! parameters[j].isOfType(action.parameters()[j])) {
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
            if (action.getActionType().equals(type) && (noParameters == -1 || action.parameters().length == noParameters)) {
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

    public final Hint getClassHint() {
        Hint hint = reflector.classHint();
        if (hint == null && superclass != null) {
            hint = superclass.getClassHint();
        }
        return hint;
    }

    private Action getDefaultAction(Action[] availableActions, Action.Type type, NakedObjectSpecification[] parameters) {
        outer: for (int i = 0; i < availableActions.length; i++) {
            Action action = availableActions[i];
            if (action.getActionType().equals(type)) {
                if (action.parameters().length == parameters.length) {
                    for (int j = 0; j < parameters.length; j++) {
                        if (!parameters[j].isOfType(action.parameters()[j])) {
                            continue outer;
                        }
                    }
                    return action;
                }

            }
        }

        return null;
    }

    public NakedObjectField getField(String name) {
        String searchName = searchName(name);

        for (int i = 0; i < fields.length; i++) {
            if (fields[i].getName().equals(searchName)) {
                return fields[i];
            }
        }

        throw new NakedObjectSpecificationException("No field called '" + name + "' in '" + getSingularName() + "'");
    }

    public NakedObjectField[] getFields() {
        return fields;
    }

    /**
     * Returns the name of the NakedClass. This is the fully qualified name of
     * the Class object that this object represents (i.e. it includes the
     * package name).
     */
    public String getFullName() {
        return reflector.fullName();
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

    /**
     * Returns the short name (with spacing) for this object in a pluralised
     * form. The plural from is obtained from the defining classes pluralName
     * method, if it exists, or by adding 's', 'es', or 'ies dependending of the
     * name's ending.
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
     * Returns the class name without the package. Removes the text up to, and
     * including the last period (".").
     */
    public String getShortName() {
        return reflector.shortName();
    }

    /**
     * Returns the short name (with spacing) of this NakedClass object. This is
     * the objects name with package name removed.
     * 
     * <p>
     * Removes the text up to, and including the last period (".").
     * </p>
     */
    public String getSingularName() {
        String singularName = reflector.singularName();

        return singularName != null ? singularName : NameConvertor.naturalName(getShortName());
    }

    public ObjectTitle getTitle() {
        return title;
    }

    public NakedObjectField[] getVisibleFields(NakedObject object, Session session) {
        if (this.viewFields != null) {
            return viewFields;
        }

        NakedObjectField[] viewFields = new NakedObjectField[fields.length];
        int v = 0;
        for (int i = 0; i < fields.length; i++) {
           // boolean useField = object.canAccess(session, fields[i]);
            boolean useField = object.getHint(session, fields[i], null).canAccess().isAllowed();
            
            if (useField) {
                viewFields[v++] = fields[i];
            }
        }

        NakedObjectField[] selectedFields = new NakedObjectField[v];
        for (int i = 0; i < selectedFields.length; i++) {
            selectedFields[i] = viewFields[i];
        }
        this.viewFields = selectedFields;

        return selectedFields;
    }

    public boolean hasSubclasses() {
        return subclasses != null;
    }

    private void init(Reflector reflector, String superclass, String[] interfaces, NakedObjectField[] fields,
            Action[] objectActions, Action[] classActions, ObjectTitle title) {
        if (reflector == null) {
            throw new NullPointerException("No reflector specified");
        }
        LOG.debug("NakedClass " + this);
        this.reflector = reflector;
        NakedObjectSpecificationLoader loader = NakedObjects.getSpecificationLoader();
        if (superclass != null) {
            this.superclass = (NakedObjectSpecificationImpl) loader.loadSpecification(superclass);
            if (this.superclass != null) {
                LOG.debug("  Superclass " + superclass);
                this.superclass.subclasses.addSubclass(this);
            }
        }

        this.interfaces = new NakedObjectSpecificationImpl[interfaces.length];
        for (int i = 0; i < interfaces.length; i++) {
            this.interfaces[i] = (NakedObjectSpecificationImpl) loader.loadSpecification(interfaces[i]);
            this.interfaces[i].subclasses.addSubclass(this);
        }

        if (isValue() && fields.length > 0) {
            LOG.warn("Naked values cannot have fields, they will be ignored");
        } else {
            this.fields = fields;
        }
        this.objectActions = objectActions;
        this.classActions = classActions;

        this.title = title;
    }

    public NakedObjectSpecification[] interfaces() {
        return interfaces;
    }

    public boolean isAbstract() {
        return reflector.isAbstract();
    }

    public boolean isDirty(NakedObject object) {
        return reflector.isDirty(object);
    }

    public boolean isLookup() {
        return reflector.isLookup();
    }

    public boolean isObject() {
        return reflector.isObject();
    }

    /**
     * Determines if this class respresents the same class, or a subclass, of
     * the specified class.
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

    /** TODO implement */
    public boolean isParsable() {
        return true;
    }

   /** @deprecated */
    public boolean isPartOf() {
        return reflector.isPartOf();
    }

     public boolean isPersistable() {
        return reflector.isPersistable();
    }
    
    public Persistable persistable() {
        return reflector.persistable();
    }

    public boolean isValue() {
        return reflector.isValue();
    }

    public void markDirty(NakedObject object) {
        reflector.markDirty(object);
    }

    public void nonReflect(String className) {
        reflector = new PrimitiveReflector(className);
        title = reflector.title();
    }

    private NakedObjectMember[] orderArray(Class memberType, NakedObjectMember[] original, String[] order, String nakedClassName) {
        if (order == null) {
            return original;

        } else {
            for (int i = 0; i < order.length; i++) {
                order[i] = NameConvertor.simpleName(order[i]);
            }

            NakedObjectMember[] ordered = (NakedObjectMember[]) Array.newInstance(memberType, original.length);

            // work through each order element and find, if there is one, a
            // matching member.
            int orderedIndex = 0;
            ordering: for (int orderIndex = 0; orderIndex < order.length; orderIndex++) {
                for (int memberIndex = 0; memberIndex < original.length; memberIndex++) {
                    NakedObjectMember member = original[memberIndex];
                    if (member == null) {
                        continue;
                    }
                    if (member.getName().equalsIgnoreCase(order[orderIndex])) {
                        ordered[orderedIndex++] = original[memberIndex];
                        original[memberIndex] = null;

                        continue ordering;
                    }
                }

                if (!order[orderIndex].trim().equals("")) {
                    LOG.warn("Invalid ordering element '" + order[orderIndex] + "' in " + nakedClassName);
                }
            }

            NakedObjectMember[] results = (NakedObjectMember[]) Array.newInstance(memberType, original.length);
            int index = 0;
            for (int i = 0; i < ordered.length; i++) {
                NakedObjectMember member = ordered[i];
                if (member != null) {
                    results[index++] = member;
                }
            }
            for (int i = 0; i < original.length; i++) {
                NakedObjectMember member = original[i];
                if (member != null) {
                    results[index++] = member;
                }
            }

            return results;
        }
    }

    public void reflect(String className, Reflector reflector) {
        LOG.debug("creating reflector for " + className + " using " + reflector);

        ActionPeer delegates[];

        this.reflector = reflector;
        
        delegates = reflector.actions(Reflector.OBJECT);
        String[] order = reflector.actionSortOrder();
        Action[] objectActions = createActions(reflector, className, delegates, order);

        delegates = reflector.actions(Reflector.CLASS);
        order = reflector.classActionSortOrder();
        Action[] classActions = createActions(reflector, className, delegates, order);

        FieldPeer fieldDelegates[] = reflector.fields();
        NakedObjectField[] fieldVector = createFields(fieldDelegates);
        NakedObjectField[] fields = (NakedObjectField[]) orderArray(NakedObjectField.class, fieldVector, reflector
                .fieldSortOrder(), className);

        String superclass = reflector.getSuperclass();
        String[] interfaces = reflector.getInterfaces();

        init(reflector, superclass, interfaces, fields, objectActions, classActions, reflector.title());
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

    public String unresolvedTitle(PojoAdapter pojo) {
        return reflector.unresolvedTitle(pojo);
    }
    
    public String toString() {
        StringBuffer s = new StringBuffer();

        s.append("NakedObjectSpecification");
        if (reflector != null) {
            s.append(" [name=");
            s.append(getFullName());
            s.append(",fields=");
            s.append(fields.length);
            s.append(",object methods=");
            s.append(objectActions.length);
            s.append(",class methods=");
            s.append(classActions.length);
            s.append(",reflector=");
            s.append(reflector);
            s.append("]");
        } else {
            s.append("[no relector set up]");
        }

        s.append("  " + Long.toHexString(super.hashCode()).toUpperCase());

        return s.toString();
    }
    

    protected void finalize() throws Throwable {
        super.finalize();
        LOG.info("finalizing specification " + this);
    }

}

/*
 * Naked Objects - a framework that exposes behaviourally complete business
 * objects directly to the user. Copyright (C) 2000 - 2005 Naked Objects Group
 * Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address
 * of Naked Objects Group is Kingsway House, 123 Goldworth Road, Woking GU21
 * 1NR, UK).
 */
