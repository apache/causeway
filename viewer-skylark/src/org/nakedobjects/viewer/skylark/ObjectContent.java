package org.nakedobjects.viewer.skylark;

import org.nakedobjects.object.NakedClass;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.collection.InstanceCollection;
import org.nakedobjects.object.reflect.Action;
import org.nakedobjects.object.reflect.Action.Type;
import org.nakedobjects.viewer.skylark.basic.DestroyObjectOption;
import org.nakedobjects.viewer.skylark.basic.DialogedObjectOption;
import org.nakedobjects.viewer.skylark.basic.FindAllOption;
import org.nakedobjects.viewer.skylark.basic.FindFirstOption;
import org.nakedobjects.viewer.skylark.basic.ImmediateObjectOption;

public class ObjectContent implements Content {
    private final NakedObject object;

    public ObjectContent(NakedObject object) {
        this.object = object;
    }

    public String debugDetails() {
        String type = getClass().getName();
        type = type.substring(type.lastIndexOf('.') + 1);
        return type + "\n" + "  object: " + object + "\n";
    }

    public NakedObject getObject() {
        return object;
    }

    public void menuOptions(MenuOptionSet options) {
        NakedObject object = getObject();

        if (object != null) {
            if (object.isFinder()) {
                options.add(MenuOptionSet.OBJECT, new FindFirstOption());
                options.add(MenuOptionSet.OBJECT, new FindAllOption());
            } else {
                menuOption(object, options, Action.USER, MenuOptionSet.OBJECT);
                menuOption(object, options, Action.EXPLORATION, MenuOptionSet.EXPLORATION);
            }

            if (!(object instanceof NakedClass) && !(object instanceof InstanceCollection) && object.isPersistent()) {
                options.add(MenuOptionSet.EXPLORATION, new DestroyObjectOption());
            }
        }
    }
/*
    private void menuOptions(NakedClass cls, MenuOptionSet menuOptionSet) {
        Action[] actions = cls.getClassActions(Action.USER);

        for (int i = 0; i < actions.length; i++) {
            ImmediateObjectOption option = ImmediateObjectOption.createOption(actions[i], cls);
            if (option != null) {
                menuOptionSet.add(MenuOptionSet.OBJECT, option);
            }
        }

        actions = cls.getClassActions(Action.EXPLORATION);

        if (actions.length > 0) {
            menuOptionSet.add(MenuOptionSet.OBJECT, null);

            for (int i = 0; i < actions.length; i++) {
                ImmediateObjectOption option = ImmediateObjectOption.createOption(actions[i], cls);
                if (option != null) {
                    menuOptionSet.add(MenuOptionSet.EXPLORATION, option);
                }
            }
        }
    }
*/

    private void menuOption(NakedObject object, MenuOptionSet menuOptionSet, Type actionType, int menuSection) {
        Action[] actions = object.getNakedClass().getObjectActions(actionType);

        for (int i = 0; i < actions.length; i++) {
            MenuOption option;
            if(actions[i].parameters().length == 0) {
                option = ImmediateObjectOption.createOption(actions[i], object);
            } else {
                option = DialogedObjectOption.createOption(actions[i], object);
            }
            if (option != null) {
                menuOptionSet.add(menuSection, option);
            }
        }
    }

    public String toString() {
        return "" + object;
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