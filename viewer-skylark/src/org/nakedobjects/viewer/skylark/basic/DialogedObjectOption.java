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

import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.control.About;
import org.nakedobjects.object.control.Allow;
import org.nakedobjects.object.control.Permission;
import org.nakedobjects.object.reflect.Action;
import org.nakedobjects.security.Session;
import org.nakedobjects.utility.Assert;
import org.nakedobjects.viewer.skylark.Location;
import org.nakedobjects.viewer.skylark.MenuOption;
import org.nakedobjects.viewer.skylark.ObjectContent;
import org.nakedobjects.viewer.skylark.View;
import org.nakedobjects.viewer.skylark.Workspace;

/**
   Options for an underlying object determined dynamically by looking for methods starting with action, veto and option for
   specifying the action, vetoing the option and giving the option an name respectively.
 */
public class DialogedObjectOption extends MenuOption {
    private ActionDialogSpecification dialogSpec = new ActionDialogSpecification();
    
    public static DialogedObjectOption createOption(Action action, NakedObject object) {
        int paramCount = action.getParameterCount();
        Assert.assertTrue("Only for actions taking one or more params", paramCount > 0);
    	About about = action.getAbout(Session.getSession().getSecurityContext(), object, new NakedObject[paramCount]);
    
    	if(about.canAccess().isVetoed()) {
    		return null;
    	}
    	
    	String labelName =  action.getLabel(Session.getSession().getSecurityContext(), object);
    	DialogedObjectOption option = new DialogedObjectOption(labelName);
    	option.action = action;
    	option.setName(labelName + "... (dialog)");
    	return option;
    }
	
	private Action action;


	private DialogedObjectOption(String name) {
		super(name);
	}

    public Permission disabled(View view) {
        NakedObject object = ((ObjectContent) view.getContent()).getObject();
        
		About about = action.getAbout(Session.getSession().getSecurityContext(), object, new NakedObject[action.getParameterCount()]);
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

    public void execute(Workspace workspace, View view, Location at) {
        NakedObject object = ((ObjectContent) view.getContent()).getObject();
        ActionContent content = new ActionContent(object, action);
        View dialog = dialogSpec.createView(content, null);
        workspace.addView(dialog);
    }

    public String toString() {
        return "DialogedObjectOption for " + action;
    }
}
