package org.nakedobjects.object;

import org.nakedobjects.object.reflect.Action;
import org.nakedobjects.object.reflect.ActionDelegate;
import org.nakedobjects.object.reflect.Field;
import org.nakedobjects.object.reflect.Member;
import org.nakedobjects.object.reflect.MemberIf;
import org.nakedobjects.object.reflect.OneToManyAssociation;
import org.nakedobjects.object.reflect.OneToManyAssociationIF;
import org.nakedobjects.object.reflect.OneToOneAssociation;
import org.nakedobjects.object.reflect.OneToOneAssociationIF;
import org.nakedobjects.object.reflect.Reflector;
import org.nakedobjects.object.reflect.Value;
import org.nakedobjects.object.reflect.ValueIf;
import org.nakedobjects.object.reflect.simple.JavaReflector;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.apache.log4j.Logger;


public class NakedClassLoader {
    private static final Logger LOG = Logger.getLogger(NakedClassLoader.class);

    String findType(String name) {
        // TODO hard coded for our java spec - need to work through all
        // registered reflectors and find one that works
        return JavaReflector.class.getName();
    }

    /**
     * Called when the NakedClass is resolved. The class should be reflected on
     * and the action and fields added to the Naked Class.
     */
    void reflect(NakedClass cls, boolean isRemote) {
        LOG.debug("reflecting on " + cls);

        String reflectorClassName = cls.getReflector().stringValue();
        String nakedObjectClassName = cls.getName().stringValue();

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
                            + nakedObjectClassName, e);
                }
            }
        } catch (InstantiationException e) {
            throw new NakedObjectRuntimeException("", e);
        } catch (IllegalAccessException e) {
            throw new NakedObjectRuntimeException("", e);
        } catch (SecurityException e) {
            throw new NakedObjectRuntimeException("", e);
        } catch (NoSuchMethodException e) {
            throw new NakedObjectRuntimeException("", e);
        } catch (IllegalArgumentException e) {
            throw new NakedObjectRuntimeException("", e);
        } catch (ClassNotFoundException e) {
            throw new NakedObjectRuntimeException("", e);
        }

        String nakedClassName = nakedObjectClassName;

        ActionDelegate delegates[];

        delegates = reflector.actions(Reflector.OBJECT);
        String[] order = reflector.actionSortOrder();
        Action[] objectActions = createActions(isRemote, reflector, nakedClassName, delegates, order);

        delegates = reflector.actions(Reflector.CLASS);
        order = reflector.classActionSortOrder();
        Action[] classActions = createActions(isRemote, reflector, nakedClassName, delegates, order);

        MemberIf fieldDelegates[] = reflector.fields();
        Field[] fieldVector = createFields(fieldDelegates, isRemote);
        Field[] fields = (Field[]) orderArray(Field.class, fieldVector, reflector.fieldSortOrder(), nakedClassName);

        String superclass = reflector.getSuperclass();
        
        cls.init(reflector, superclass, fields, objectActions, classActions);

    }

    private Action[] createActions(boolean isRemote, Reflector reflector, String nakedClassName, ActionDelegate[] delegates,
            String[] order) {
        Action[] actions = createActions(delegates, isRemote);
        Action[] objectActions = (Action[]) orderArray(Action.class, actions, order, nakedClassName);
        return objectActions;
    }

    private Member[] orderArray(Class memberType, Member[] original, String[] order, String nakedClassName) {
        if (order == null) {
            return original;

        } else {
	        Member[] ordered = (Member[]) Array.newInstance(memberType, original.length);

	        // work through each order element and find, if there is one, a
            // matching member.
            int orderedIndex = 0;
            ordering: for (int orderIndex = 0; orderIndex < order.length; orderIndex++) {
                for (int memberIndex = 0; memberIndex < original.length; memberIndex++) {
                    Member member = original[memberIndex];
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

            Member[] results = (Member[]) Array.newInstance(memberType, original.length);
            int index = 0;
            for (int i = 0; i < ordered.length; i++) {
                Member member = ordered[i];
                if (member != null) {
                    results[index++] = member;
                }
            }
            for (int i = 0; i < original.length; i++) {
                Member member = original[i];
                if (member != null) {
                    results[index++] = member;
                }
            }

            return results;
        }
    }

    private Action[] createActions(ActionDelegate[] actions, boolean useRemote) {
        Action actionChains[] = new Action[actions.length];

        for (int i = 0; i < actions.length; i++) {
            actionChains[i] = createAction(actions[i]);
        }

        return actionChains;
    }

    protected Action createAction(ActionDelegate action) {
        return new Action(action.getName(), action);
    }

    private Field[] createFields(MemberIf fields[], boolean useRemote) {
        Field[] fieldChains = new Field[fields.length];

        for (int i = 0; i < fields.length; i++) {

            Object object = fields[i];

            if (object instanceof ValueIf) {
                fieldChains[i] = createField((ValueIf) object);

            } else if (object instanceof OneToOneAssociationIF) {
                fieldChains[i] = createField((OneToOneAssociationIF) object);

            } else if (object instanceof OneToManyAssociationIF) {
                fieldChains[i] = createField((OneToManyAssociationIF) object);

            } else {
                throw new NakedObjectRuntimeException();
            }
        }

        return fieldChains;
    }

    protected Field createField(OneToManyAssociationIF local) {
        return new OneToManyAssociation(local.getName(), local.getType(), local);
    }

    protected Field createField(OneToOneAssociationIF local) {
        return new OneToOneAssociation(local.getName(), local.getType(), local);

    }

    protected Field createField(ValueIf local) {
        return new Value(local.getName(), local.getType(), local);
    }
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business
 * objects directly to the user. Copyright (C) 2000 - 2004 Naked Objects Group
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