package org.nakedobjects.object.reflect;

import org.nakedobjects.object.Action;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectField;
import org.nakedobjects.object.NakedObjectMember;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.NakedObjectSpecificationException;
import org.nakedobjects.object.NakedObjectSpecificationLoader;
import org.nakedobjects.object.NakedObjects;
import org.nakedobjects.object.control.Hint;
import org.nakedobjects.utility.NakedObjectRuntimeException;

import java.lang.reflect.Array;
import java.util.Vector;

import org.apache.log4j.Logger;


public abstract class AbstractNakedObjectSpecification implements NakedObjectSpecification {
    private final static Logger LOG = Logger.getLogger(AbstractNakedObjectSpecification.class);
    
    private class SubclassList {
        private Vector classes = new Vector();

        public void addSubclass(AbstractNakedObjectSpecification cls) {
            classes.addElement(cls);
        }

        public NakedObjectSpecification[] toArray() {
            NakedObjectSpecification[] classesArray = new NakedObjectSpecification[classes.size()];
            classes.copyInto(classesArray);
            return classesArray;
        }
    }

    private Action[] classActions = new Action[0];
    private NakedObjectField[] fields = new NakedObjectField[0];
    private AbstractNakedObjectSpecification[] interfaces;
    private Action[] objectActions = new Action[0];
    private SubclassList subclasses = new SubclassList();
    private AbstractNakedObjectSpecification superclass;
    private ObjectTitle title;
   // private NakedObjectField[] viewFields;
    private String shortName;

    private Action[] createActions(ReflectionPeerBuilder builder, ActionPeer[] actions) {
        Action actionChains[] = new Action[actions.length];

        for (int i = 0; i < actions.length; i++) {
            actionChains[i] = builder.createAction(getFullName(), actions[i]);
        }

        return actionChains;
    }

    protected Action[] createActions(ReflectionPeerBuilder builder, ActionPeer[] delegates, String[] order) {
        Action[] actions = createActions(builder, delegates);
        Action[] objectActions = (Action[]) orderArray(Action.class, actions, order);
        return objectActions;
    }

    protected NakedObjectField[] createFields(ReflectionPeerBuilder builder, FieldPeer fieldPeers[]) {
        NakedObjectField[] fields = new NakedObjectField[fieldPeers.length];

        for (int i = 0; i < fieldPeers.length; i++) {

            Object object = fieldPeers[i];

            if (object instanceof OneToOnePeer) {
                fields[i] = builder.createField(getFullName(), (OneToOnePeer) object);

            } else if (object instanceof OneToManyPeer) {
                fields[i] = builder.createField(getFullName(), (OneToManyPeer) object);

            } else {
                throw new NakedObjectRuntimeException();
            }
        }

        return fields;
    }
    
    private Action getAction(Action[] availableActions, Action.Type type, String name, NakedObjectSpecification[] parameters) {
        if (name == null) {
            return null;
        }

        String searchName = searchName(name);
        outer: for (int i = 0; i < availableActions.length; i++) {
            Action action = availableActions[i];
            if (action.getActionType().equals(type)) {
                if (action.getId().equals(searchName)) {
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

    public Hint getClassHint() {
        Hint hint = null;
        if (superclass != null) {
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
    
    public Object getFieldExtension(String name, Class cls) {
        String searchName = searchName(name);

        for (int i = 0; i < fields.length; i++) {
            if (fields[i].getId().equals(searchName)) {
                return fields[i].getExtension(cls);
            }
        }

        throw new NakedObjectSpecificationException("No field called '" + name + "' in '" + getSingularName() + "'");
    }
    
    public Class[] getFieldExtensions(String fieldName) {
        return getField(fieldName).getExtensions() ;
    }

    public NakedObjectField getField(String name) {
        String searchName = searchName(name);

        for (int i = 0; i < fields.length; i++) {
            if (fields[i].getId().equals(searchName)) {
                return fields[i];
            }
        }

        throw new NakedObjectSpecificationException("No field called '" + name + "' in '" + getSingularName() + "'");
    }

    public NakedObjectField[] getFields() {
        return fields;
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
    public String getPluralName() {
        return NameConvertor.pluralName(getSingularName());
    }

    /**
     * Returns the class name without the package. Removes the text up to, and
     * including the last period (".").
     */
    public String getShortName() {
        return shortName;
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
        return NameConvertor.naturalName(getShortName());
    }

    public String getTitle(NakedObject object) {
        return title.title(object);
    }

    public NakedObjectField[] getVisibleFields(NakedObject object) {
        // TODO don't cache fields, to allow the visibility to change on the fly - comment out the following if
      /*  if (this.viewFields != null) {
            return viewFields;
        }
*/
        NakedObjectField[] viewFields = new NakedObjectField[fields.length];
        int v = 0;
        for (int i = 0; i < fields.length; i++) {
            boolean useField = fields[i].isAccessible()  && object.isVisible(fields[i]).isAllowed();            
            if (useField) {
                viewFields[v++] = fields[i];
            }
        }

        NakedObjectField[] selectedFields = new NakedObjectField[v];
        for (int i = 0; i < selectedFields.length; i++) {
            selectedFields[i] = viewFields[i];
        }
        //this.viewFields = selectedFields;

        return selectedFields;
    }

    public boolean hasSubclasses() {
        return subclasses != null;
    }

    protected void init(ObjectTitle title) {
         this.title = title;
    }

    protected void init(String superclass, String[] interfaces, NakedObjectField[] fields,
            Action[] objectActions, Action[] classActions) {
        LOG.debug("init specification " + this);
        NakedObjectSpecificationLoader loader = NakedObjects.getSpecificationLoader();
        if (superclass != null) {
            this.superclass = (AbstractNakedObjectSpecification) loader.loadSpecification(superclass);
            if (this.superclass != null) {
                LOG.debug("  Superclass " + superclass);
                this.superclass.subclasses.addSubclass(this);
            }
        }

        this.interfaces = new AbstractNakedObjectSpecification[interfaces.length];
        for (int i = 0; i < interfaces.length; i++) {
            this.interfaces[i] = (AbstractNakedObjectSpecification) loader.loadSpecification(interfaces[i]);
            this.interfaces[i].subclasses.addSubclass(this);
        }

        if (isValue() && fields.length > 0) {
            LOG.warn("naked values cannot have fields, they will be ignored");
        } else {
            this.fields = fields;
        }
        this.objectActions = objectActions;
        this.classActions = classActions;
	}

    public NakedObjectSpecification[] interfaces() {
        return interfaces;
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

    protected NakedObjectMember[] orderArray(Class memberType, NakedObjectMember[] original, String[] order) {
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
                    if (member.getId().equalsIgnoreCase(order[orderIndex])) {
                        ordered[orderedIndex++] = original[memberIndex];
                        original[memberIndex] = null;

                        continue ordering;
                    }
                }

                if (!order[orderIndex].trim().equals("")) {
                    LOG.warn("invalid ordering element '" + order[orderIndex] + "' in " + getClassName());
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
/*
    public void reflect(String className, Reflector reflector) {
        LOG.debug("creating reflector for " + className + " using " + reflector);
        this.reflector = reflector;

        ActionPeer delegates[] = reflector.actionPeers(Reflector.OBJECT);
        String[] order = reflector.actionSortOrder();
        Action[] objectActions = createActions(reflector, className, delegates, order);

        delegates = reflector.actionPeers(Reflector.CLASS);
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

*/    
    private String searchName(String name) {
        return NameConvertor.simpleName(name);
    }

    public NakedObjectSpecification[] subclasses() {
        return subclasses.toArray();
    }

    public NakedObjectSpecification superclass() {
        return superclass;
    }
/*
    private String asString;
    public String toString() {
		if (asString == null) {
			asString = lazyToString();
		}

		return asString;
    }
    
	/**
	 * Performance tuning.
	 * /
	private String lazyToString() {
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
*/
    
    protected abstract String getClassName();
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
