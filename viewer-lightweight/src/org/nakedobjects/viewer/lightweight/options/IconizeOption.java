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
import org.nakedobjects.viewer.lightweight.DesktopView;
import org.nakedobjects.viewer.lightweight.Location;
import org.nakedobjects.viewer.lightweight.ObjectView;
import org.nakedobjects.viewer.lightweight.Workspace;
import org.nakedobjects.viewer.lightweight.util.ViewFactory;


public class IconizeOption extends AbstractObjectOption {
	public IconizeOption() {
		super("Iconize");
	}

	public void execute(Workspace frame, ObjectView view, Location at) {
		NakedObject object = view.getObject();
		DesktopView icon = ViewFactory.getViewFactory().createIconView(object, null);
		icon.setLocation(view.getAbsoluteLocation());
		frame.removeView(view);
		frame.addIcon(icon);
	}

	public Permission disabled(Workspace frame, ObjectView component, Location location) {
		return new Allow("Iconize this "  + component.getName().toLowerCase());
	}

	public String toString() {
		return "IconizeOption";
	}
}
