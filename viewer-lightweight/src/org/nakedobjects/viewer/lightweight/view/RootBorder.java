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
package org.nakedobjects.viewer.lightweight.view;

import org.nakedobjects.viewer.lightweight.Color;
import org.nakedobjects.viewer.lightweight.MenuOptionSet;
import org.nakedobjects.viewer.lightweight.PrintableView;
import org.nakedobjects.viewer.lightweight.Style;
import org.nakedobjects.viewer.lightweight.UserAction;
import org.nakedobjects.viewer.lightweight.View;
import org.nakedobjects.viewer.lightweight.options.CloseButton;
import org.nakedobjects.viewer.lightweight.options.CloseOtherViewsOption;
import org.nakedobjects.viewer.lightweight.options.IconizeButton;
import org.nakedobjects.viewer.lightweight.options.IconizeOption;
import org.nakedobjects.viewer.lightweight.options.PrintOption;


public class RootBorder extends SimpleBorder {
    private static final UserAction PRINT_OPTION = new PrintOption();
    private static final UserAction ICONIZE_OPTION = new IconizeOption();

    public RootBorder() {
    	super(3);
        addControl(new IconizeButton());
		addControl(new CloseButton());
    }

	public void viewMenuOptions(View view, MenuOptionSet options) {
        options.add(MenuOptionSet.WINDOW, new CloseOtherViewsOption());
        options.add(MenuOptionSet.WINDOW, ICONIZE_OPTION);
        if(view instanceof PrintableView) {
        	options.add(MenuOptionSet.WINDOW, PRINT_OPTION);
        }
    }
	
	protected Color getInBackground() {
		return Style.IN_BACKGROUND;
	}

	protected Color getRootViewIdentified() {
		return Style.IDENTIFIED;
	}


}
