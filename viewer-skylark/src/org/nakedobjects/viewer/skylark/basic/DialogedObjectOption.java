package org.nakedobjects.viewer.skylark.basic;

import org.nakedobjects.object.Action;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.Action.Type;
import org.nakedobjects.object.control.Allow;
import org.nakedobjects.object.control.Consent;
import org.nakedobjects.utility.Assert;
import org.nakedobjects.viewer.skylark.Location;
import org.nakedobjects.viewer.skylark.AbstractUserAction;
import org.nakedobjects.viewer.skylark.View;
import org.nakedobjects.viewer.skylark.Workspace;

/**
   Options for an underlying object determined dynamically by looking for methods starting with action, veto and option for
   specifying the action, vetoing the option and giving the option an name respectively.
 */
class DialogedObjectOption extends AbstractUserAction {
    private ActionDialogSpecification dialogSpec = new ActionDialogSpecification();
    
    public static DialogedObjectOption createOption(Action action, NakedObject object) {
        int paramCount = action.getParameterCount();
        Assert.assertTrue("Only for actions taking one or more params", paramCount > 0);
    	if(! action.isAuthorised() || object.isVisible(action).isVetoed()) {
    		return null;
    	}

    	DialogedObjectOption option = new DialogedObjectOption(action, object);
    	return option;
    }
	
	private final Action action;
	private final NakedObject target ;
	
	private DialogedObjectOption(final Action action, NakedObject object) {
		super(action.getName() + "...");
		this.action = action;
		this.target = object;
	}

    public Type getType() {
        return action.getType();
    }
    
    public Consent disabled(View view) {
        // ignore the details from the About about useablility this will be
        // checked in the dialog
        String description = getName(view) + ": " + action.getDescription();
        if (action.hasReturn()) {
            description += " returns a " + action.getReturnType().getSingularName();
        }
        return new Allow(description);
    }

    public void execute(Workspace workspace, View view, Location at) {
        ActionHelper ai = ActionHelper.createInstance(target, action);
        ActionContent content = new ActionContent(ai);
        View dialog = dialogSpec.createView(content, null);
        dialog.setLocation(at);
        workspace.addView(dialog);
    }

    public String toString() {
        return "DialogedObjectOption for " + action;
    }
}


/*
Naked Objects - a framework that exposes behaviourally complete
business objects directly to the user.
Copyright (C) 2000 - 2005  Naked Objects Group Ltd

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