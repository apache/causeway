package org.nakedobjects.viewer.skylark.value;


import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

import org.nakedobjects.object.NakedValue;
import org.nakedobjects.object.control.Permission;
import org.nakedobjects.object.control.defaults.Allow;
import org.nakedobjects.object.control.defaults.Veto;
import org.nakedobjects.viewer.skylark.Location;
import org.nakedobjects.viewer.skylark.View;
import org.nakedobjects.viewer.skylark.Workspace;


public class CopyValueOption extends AbstractValueOption {
    public CopyValueOption() {
        super("Copy");
    }

    public Permission disabled(View view) {
    	NakedValue value = getValue(view);
    	
    	if(value.isEmpty() || value.titleString().equals("")) {
    		return new Veto("Field is empty");
    	} else {
			return new Allow("Copy value " + value.titleString() + " to clipboard");
    	}
    }

    public void execute(Workspace frame, View view, Location at) {
		String clip = getValue(view).titleString();
    	
		Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
		cb.setContents(new StringSelection(clip), null);
    }

    public String toString() {
        return "CopyValueOption";
    }
}

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
