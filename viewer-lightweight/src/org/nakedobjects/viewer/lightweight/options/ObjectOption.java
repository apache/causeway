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

package org.nakedobjects.viewer.lightweight.options;

import org.nakedobjects.object.NakedClass;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.control.About;
import org.nakedobjects.object.control.Allow;
import org.nakedobjects.object.control.Permission;
import org.nakedobjects.object.reflect.Action;
import org.nakedobjects.security.Session;
import org.nakedobjects.viewer.lightweight.Location;
import org.nakedobjects.viewer.lightweight.MenuOptionSet;
import org.nakedobjects.viewer.lightweight.ObjectView;
import org.nakedobjects.viewer.lightweight.Workspace;


/**
   Options for an underlying object determined dynamically by looking for methods starting with action, veto and option for
   specifying the action, vetoing the option and giving the option an name respectively.
 */
public class ObjectOption extends AbstractObjectOption {
    private Action action;

	
	private ObjectOption(String name) {
		super(name);
	}
	
	protected static ObjectOption createOption(Action action, NakedObject object) {
		About about = action.getAbout(Session.getSession().getSecurityContext(), object);

		if(about.canAccess().isVetoed()) {
			return null;
		}
		
		
		String labelName =  action.getLabel(Session.getSession().getSecurityContext(), object);
		ObjectOption option = new ObjectOption(labelName);
		option.action = action;

		// if method returns something then add ... to indicate a new window is shown
		if (action.hasReturn()) {
			option.setName(labelName + "...");
		}
		return option;
	}

    public void execute(Workspace workspace, ObjectView view, Location at) {
        NakedObject object = view.getObject();
        NakedObject returnedObject = action.execute(object);

        if (returnedObject != null) {
        	view.objectMenuReturn(returnedObject, at);
        	/*
        	ViewFactory vf = ViewFactory.getViewFactory();
        	if(vf.hasRootViews(returned)) {
            	RootView newView = vf.createRootView(returned);
				newView.setLocation(at);
				workspace.addRootView(newView);
        	} else {
				DesktopView newView = vf.createIconView(returned, null);
				at.setX(view.topView().getAbsoluteLocation().getX() - newView.getSize().getWidth() / 2);
				newView.setLocation(at);
				workspace.addIcon(newView);
        	}
        	*/
        }
    }

    /**
     * Queries the ObjectInterface class for the ObjectInterface for the specified
       object and looks for Options to offer to the user adding them to the
       MenuOptionSet.
     */
    public static void menuOptions(NakedObject object, MenuOptionSet menuOptionSet) {
        Action[] actions = object.getNakedClass().getObjectActions(Action.USER);

        for (int i = 0; i < actions.length; i++) {
            ObjectOption option = createOption(actions[i], object);
            if(option != null) {
            	menuOptionSet.add(MenuOptionSet.OBJECT, option);
            }
        }

        actions = object.getNakedClass().getObjectActions(Action.EXPLORATION);

        if (actions.length > 0) {
//            menuOptionSet.add(MenuOptionSet.OBJECT, null);

            for (int i = 0; i < actions.length; i++) {
                ObjectOption option =createOption(actions[i], object);
                if(option != null) {
                	menuOptionSet.add(MenuOptionSet.EXPLORATION, option);
                }
            }
        }
    }

    public static void menuOptions(NakedClass cls, MenuOptionSet menuOptionSet) {
        Action[] actions = cls.getClassActions(Action.USER);

        for (int i = 0; i < actions.length; i++) {
            ObjectOption option = createOption(actions[i], cls);
            if(option != null) {
            	menuOptionSet.add(MenuOptionSet.OBJECT, option);
            }
        }

        actions = cls.getClassActions(Action.EXPLORATION);

        if (actions.length > 0) {
            menuOptionSet.add(MenuOptionSet.OBJECT, null);

            for (int i = 0; i < actions.length; i++) {
                ObjectOption option = createOption(actions[i], cls);
                if(option != null) {
                	menuOptionSet.add(MenuOptionSet.EXPLORATION, option);
                }
            }
        }
    }

    public Permission disabled(Workspace frame, ObjectView view, Location location) {
        NakedObject object = view.getObject();
        
		About about = action.getAbout(Session.getSession().getSecurityContext(), object);
		if(about.canUse().isAllowed()) {
			String description = about.getDescription();
			if(action.hasReturn()) {
				description += " returns a " + action.returns();
			}
			return new Allow(description);
		} else {
			return about.canUse();
		}
    }

    public String toString() {
        return "ObjectOption for " + action;
    }
}
