package org.nakedobjects.viewer.skylark.basic;

import org.nakedobjects.object.Action;
import org.nakedobjects.object.NakedClass;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.defaults.FastFinder;
import org.nakedobjects.object.defaults.InstanceCollectionVector;
import org.nakedobjects.viewer.skylark.MenuOption;
import org.nakedobjects.viewer.skylark.MenuOptionSet;


public class ObjectOption {
    // TODO options should be available for NakedReference objects not just NakedObjects
    // TODO update hierachy to bring NakedCollection into line with NakedObject
    
    public static void menuOptions(final NakedObject object, MenuOptionSet options) {
        if (object != null) {
            if (object.getObject() instanceof FastFinder) {
                options.add(MenuOptionSet.OBJECT, new FindFirstOption());
                options.add(MenuOptionSet.OBJECT, new FindAllOption());
            } else {
                menuOption(object, options, Action.USER, MenuOptionSet.OBJECT);
                menuOption(object, options, Action.EXPLORATION, MenuOptionSet.EXPLORATION);
                menuOption(object, options, Action.DEBUG, MenuOptionSet.DEBUG);
            }

            boolean isPersistent = object.getOid() != null;
/*
    TODO this is something that the object should offer if needed
                    if (!isPersistent) {
                options.add(MenuOptionSet.OBJECT, new MenuOption("Make Persistent") {
                    public void execute(Workspace workspace, View view, Location at) {
                        object.getContext().getObjectManager().makePersistent(object);
                    }
                });
            }
*/

            if (!(object.getObject() instanceof NakedClass) && !(object.getObject() instanceof InstanceCollectionVector) && isPersistent) {
                options.add(MenuOptionSet.DEBUG, new DestroyObjectOption());
            }
        }
    }
            
    private static void menuOption(NakedObject object, MenuOptionSet menuOptionSet, Action.Type actionType, int menuSection) {
        Action[] actions = object.getSpecification().getObjectActions(actionType);

        for (int i = 0; i < actions.length; i++) {
            MenuOption option;
            if (actions[i].parameterTypes().length == 0) {
                option = ImmediateObjectOption.createOption(actions[i], object);
            } else {
                option = DialogedObjectOption.createOption(actions[i], object);
            }
            if (option != null) {
                menuOptionSet.add(menuSection, option);
            }
        }
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