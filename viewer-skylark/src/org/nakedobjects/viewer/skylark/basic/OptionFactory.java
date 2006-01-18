package org.nakedobjects.viewer.skylark.basic;

import org.nakedobjects.object.Action;
import org.nakedobjects.object.NakedClass;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.NakedObjects;
import org.nakedobjects.object.defaults.FastFinder;
import org.nakedobjects.object.defaults.InstanceCollectionVector;
import org.nakedobjects.viewer.skylark.UserAction;
import org.nakedobjects.viewer.skylark.UserActionSet;


public class OptionFactory {
    // TODO options should be available for NakedReference objects not just NakedObjects
    // TODO update hierachy to bring NakedCollection into line with NakedObject

    public static void addClassMenuOptions(NakedObjectSpecification specificaton, UserActionSet menuOptionSet) {
        NakedClass nakedClass = NakedObjects.getObjectPersistor().getNakedClass(specificaton);
        NakedObject classAdapter = NakedObjects.getObjectLoader().getAdapterForElseCreateAdapterForTransient(nakedClass);

        Action[] actions;
        menuOptions(classAdapter, menuOptionSet, Action.USER);
        actions = specificaton.getClassActions(Action.USER);
        menuOptions(actions, classAdapter, menuOptionSet);

        menuOptions(classAdapter, menuOptionSet, Action.EXPLORATION);
        actions = specificaton.getClassActions(Action.EXPLORATION);
        menuOptions(actions, classAdapter, menuOptionSet);

        menuOptions(classAdapter, menuOptionSet, Action.DEBUG);
        actions = specificaton.getClassActions(Action.DEBUG);
        menuOptions(actions, classAdapter, menuOptionSet);
    }

    public static void addObjectMenuOptions(final NakedObject object, UserActionSet options) {
        if (object != null) {
            if (object.getObject() instanceof FastFinder) {
                options.add(new FindFirstOption());
                options.add(new FindAllOption());
            } else {
          /*      menuOptions(object, options, Action.USER);
                menuOptions(object, options, Action.EXPLORATION);
                menuOptions(object, options, Action.DEBUG);
             */   
                Action[] actions1 = object.getSpecification().getObjectActions(Action.USER);
                Action[] actions2 = object.getSpecification().getObjectActions(Action.EXPLORATION);
                Action[] actions3 = object.getSpecification().getObjectActions(Action.DEBUG);
                Action[] actions = new Action[actions1.length + actions2.length + actions3.length];
                System.arraycopy(actions1, 0, actions, 0, actions1.length);
                System.arraycopy(actions2, 0, actions, actions1.length, actions2.length);
                System.arraycopy(actions3, 0, actions, actions1.length + actions2.length, actions3.length);
                menuOptions(actions, object, options);

            }

            boolean isPersistent = object.getOid() != null;

            if (!(object.getObject() instanceof NakedClass) && !(object.getObject() instanceof InstanceCollectionVector)
                    && isPersistent) {
                options.add(new DestroyObjectOption());
            }
        }
    }

    private static void menuOptions(Action[] actions, NakedObject object, UserActionSet menuOptionSet) {
        for (int i = 0; i < actions.length; i++) {
            UserAction option = null;
            if (actions[i].getActions().length > 0) {
                    option = new UserActionSet(actions[i].getName(), menuOptionSet);
                    menuOptions(actions[i].getActions(), object, (UserActionSet) option);

            } else if (actions[i].getParameterTypes().length == 0) {
                option = ImmediateObjectOption.createOption(actions[i], object);

            } else {
                option = DialogedObjectOption.createOption(actions[i], object);
            }
            if (option != null) {
                menuOptionSet.add(option);
            }
        }
    }

    private static void menuOptions(NakedObject object, UserActionSet menuOptionSet, Action.Type actionType) {
        Action[] actions = object.getSpecification().getObjectActions(actionType);
        menuOptions(actions, object, menuOptionSet);
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