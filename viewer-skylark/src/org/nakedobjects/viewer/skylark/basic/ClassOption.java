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

package org.nakedobjects.viewer.skylark.basic;

import org.nakedobjects.object.NakedClass;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.control.About;
import org.nakedobjects.object.control.Permission;
import org.nakedobjects.object.reflect.Action;
import org.nakedobjects.security.Session;
import org.nakedobjects.viewer.skylark.Location;
import org.nakedobjects.viewer.skylark.MenuOption;
import org.nakedobjects.viewer.skylark.MenuOptionSet;
import org.nakedobjects.viewer.skylark.View;
import org.nakedobjects.viewer.skylark.Workspace;


public class ClassOption extends MenuOption {
	private Action action;
	private NakedClass cls;
	
	public static void menuOptions(NakedClass cls, MenuOptionSet menuOptionSet) {
		Action[] actions;
		// TODO only need to display the object methods for the naked class (and not class method of the class
		
		actions = cls.getClassActions(Action.USER, 0);

		for (int i = 0; i < actions.length; i++) {
			addOption(cls, menuOptionSet, actions[i], MenuOptionSet.OBJECT);
		}

		actions = cls.getNakedClass().getObjectActions(Action.USER, 0);

		for (int i = 0; i < actions.length; i++) {
			addOption(cls, menuOptionSet, actions[i], MenuOptionSet.OBJECT);
		}

	actions = cls.getClassActions(Action.EXPLORATION, 0);

		if (actions.length > 0) {
			for (int i = 0; i < actions.length; i++) {
				addOption(cls, menuOptionSet, actions[i], MenuOptionSet.EXPLORATION);
			}
		}
		
		actions = cls.getNakedClass().getObjectActions(Action.EXPLORATION, 0);

		if (actions.length > 0) {
			for (int i = 0; i < actions.length; i++) {
				addOption(cls, menuOptionSet, actions[i], MenuOptionSet.EXPLORATION);
			}
		}
	}

	private static void addOption(NakedClass cls, MenuOptionSet menuOptionSet, Action action, int type) {
		About about = action.getAbout(Session.getSession().getSecurityContext(), cls);

		if(about.canAccess().isAllowed()) {
			String labelName =  action.getLabel(Session.getSession().getSecurityContext(), cls);
			ClassOption option = new ClassOption(cls, labelName, action);
	
			// if method returns something then add ... to indicate a new window is shown
			if (action.hasReturn()) {
				option.setName(labelName + "...");
			}
			
			menuOptionSet.add(type, option);
		}
	}
		

	public ClassOption(NakedClass cls, String name, Action action) {
		super(name);
		this.cls = cls;
		this.action = action;
	}
	
	public final Permission disabled(View view) {
			About about = action.getAbout(Session.getSession().getSecurityContext(), cls);
			return about.canUse();
	}
	
	public final void execute(Workspace frame, View view, Location at) {
			NakedObject returnedObject = action.execute(cls);

			if (returnedObject != null) {
				view.objectActionResult(returnedObject, at);
			}
	}
}
