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


import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;

import org.apache.log4j.Logger;
import org.nakedobjects.object.ValueParseException;
import org.nakedobjects.object.control.Allow;
import org.nakedobjects.object.control.Permission;
import org.nakedobjects.object.control.Veto;
import org.nakedobjects.viewer.lightweight.AbstractValueView;
import org.nakedobjects.viewer.lightweight.Location;
import org.nakedobjects.viewer.lightweight.MenuOption;
import org.nakedobjects.viewer.lightweight.View;
import org.nakedobjects.viewer.lightweight.Workspace;


public class PasteValueOption extends MenuOption {
	private static final Logger LOG = Logger.getLogger(PasteValueOption.class);
	
    public PasteValueOption() {
        super("Replace with clipboard value");
    }

    public Permission disabled(Workspace frame, View view, Location location) {
		AbstractValueView valueView = (AbstractValueView) view;
//		NakedValue value = ((ValueView) view).getValue();

		if (!valueView.canChangeValue()) {
			return new Veto("Field cannot be edited");
		} else {
			return new Allow("Replace field content with '" + getClipboard() + "' from clipboard");
		}
    }

    public void execute(Workspace frame, View view, Location at) {
		String value = getClipboard();
    	try {
			AbstractValueView v = (AbstractValueView) view;
			v.getValue().parse(value);
			v.getContainedBy().objectChanged();
		} catch (ValueParseException e) {
			LOG.error("Invalid paste value " + e);
		}
    }

	private String getClipboard() {
		Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
		 Transferable content = cb.getContents(this);
		
		String value = "illegal value";
		 try {
			 value = ((String) content.getTransferData(DataFlavor.stringFlavor));
		 } catch (Throwable e) {
			 LOG.error("Invalid clipboard operation " + e);
		 }
		return value;
	}

    public String toString() {
        return "PasteValueOption";
    }
}
