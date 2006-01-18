package org.nakedobjects.viewer.skylark.basic;

import org.nakedobjects.viewer.skylark.Click;
import org.nakedobjects.viewer.skylark.Content;
import org.nakedobjects.viewer.skylark.Location;
import org.nakedobjects.viewer.skylark.AbstractUserAction;
import org.nakedobjects.viewer.skylark.UserActionSet;
import org.nakedobjects.viewer.skylark.Style;
import org.nakedobjects.viewer.skylark.View;
import org.nakedobjects.viewer.skylark.ViewAxis;
import org.nakedobjects.viewer.skylark.Workspace;
import org.nakedobjects.viewer.skylark.core.AbstractViewDecorator;


class IconOpenAction extends AbstractViewDecorator {
    protected IconOpenAction(View wrappedView) {
        super(wrappedView);
    }

    private void closeIcon() {
        getView().dispose();
    }

    public void viewMenuOptions(UserActionSet menuOptions) {
        super.viewMenuOptions(menuOptions);

        menuOptions.add(new AbstractUserAction("Open") {
            public void execute(Workspace workspace, View view, Location at) {
                openIcon();
            }
        });

        menuOptions.add(new AbstractUserAction("Close") {
            public void execute(Workspace workspace, View view, Location at) {
                closeIcon();
            }
        });
    }

    private void openIcon() {
        closeIcon();
        getWorkspace().addOpenViewFor(getContent().getNaked(), getLocation());
    }

    public void secondClick(Click click) {
        openIcon();
    }
}

public class RootIconSpecification extends IconSpecification {
    public View createView(Content content, ViewAxis axis) {
        return new IconOpenAction(new IconView(content, this, axis, Style.NORMAL));
    }
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business
 * objects directly to the user. Copyright (C) 2000 - 2005 Naked Objects Group
 * Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address
 * of Naked Objects Group is Kingsway House, 123 Goldworth Road, Woking GU21
 * 1NR, UK).
 */