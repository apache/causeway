package org.nakedobjects.viewer.skylark.metal;

import org.nakedobjects.object.control.Consent;
import org.nakedobjects.object.control.Veto;
import org.nakedobjects.viewer.skylark.Canvas;
import org.nakedobjects.viewer.skylark.Location;
import org.nakedobjects.viewer.skylark.Style;
import org.nakedobjects.viewer.skylark.UserAction;
import org.nakedobjects.viewer.skylark.View;
import org.nakedobjects.viewer.skylark.Workspace;

public class ResizeWindowControl extends WindowControl {

    public ResizeWindowControl(View target) {
        super(new UserAction() {

            public Consent disabled(View view) {
                //return Allow.DEFAULT;
                return Veto.DEFAULT;
            }

            public void execute(Workspace workspace, View view, Location at) {
            }

            public String getDescription(View view) {
                return "Show this object as an icon on the workspace";
            }

            public String getName(View view) {
                return "Iconize";
            }}, target);
    }

    public void draw(Canvas canvas) {
        int x  = 0;
        int y = 0;
        
        canvas.drawRectangle(x + 1, y + 1, WIDTH - 1, HEIGHT - 1, Style.WHITE);
        canvas.drawRectangle(x, y, WIDTH - 1, HEIGHT - 1, Style.SECONDARY1);
        canvas.drawRectangle(x + 3, y + 2, 8, 8, Style.SECONDARY2);
        canvas.drawLine(x + 3, y + 3, x + 10, y + 3, Style.SECONDARY2);
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