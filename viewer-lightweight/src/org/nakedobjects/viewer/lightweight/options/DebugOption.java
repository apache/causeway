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


import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.control.Allow;
import org.nakedobjects.object.control.Permission;
import org.nakedobjects.viewer.lightweight.DebugFrame;
import org.nakedobjects.viewer.lightweight.DebugObjectView;
import org.nakedobjects.viewer.lightweight.Location;
import org.nakedobjects.viewer.lightweight.ObjectView;
import org.nakedobjects.viewer.lightweight.Workspace;


/**
   Display debug window
 */
public class DebugOption extends AbstractObjectOption {
	public DebugOption() {
		super("Debug Info...");
	}

	public void execute(Workspace workspace, ObjectView view, Location at) {
		DebugFrame f = new DebugFrame();
		f.setInfo(new DebugObjectView(view));
		f.show(at.getX() + 50, workspace.getBounds().getY() + 6);
	}

	public Permission disabled(Workspace workspace, ObjectView view, Location location) {
		String status = "";
		NakedObject object = view.getObject();
		if (object != null) {
			String objectId = object.toString();
			status = "Debug " + objectId;
		}

		return new Allow(status);
	}
}
