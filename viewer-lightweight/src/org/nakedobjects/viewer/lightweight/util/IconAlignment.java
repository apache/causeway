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
package org.nakedobjects.viewer.lightweight.util;

import org.nakedobjects.viewer.lightweight.ClassView;
import org.nakedobjects.viewer.lightweight.Location;
import org.nakedobjects.viewer.lightweight.ObjectView;
import org.nakedobjects.viewer.lightweight.View;
import org.nakedobjects.viewer.lightweight.Workspace;
import org.nakedobjects.viewer.lightweight.WorkspaceAlignment;


public class IconAlignment implements WorkspaceAlignment {
    public void align(View[] icons, Workspace workspace) {
		int x = 10;
		int y = 30;

		int maxY = workspace.getRequiredSize().getHeight();
		
		int maxWidth = 0;
        for (int i = 0; i < icons.length; i++) {
            if (icons[i] instanceof ClassView) {
            	int iconHeight = icons[i].getSize().getHeight();
            	if(y + iconHeight > maxY) {
            		y = 10;
            		x += maxWidth + 8;
            	}
                icons[i].setLocation(new Location(x, y));
                maxWidth = Math.max(maxWidth, icons[i].getSize().getWidth());
                y += iconHeight + 8;
            }

            ((Workspace)workspace).limitBounds((ObjectView) icons[i]);
        }
    }
}
