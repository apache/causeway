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

import org.nakedobjects.viewer.lightweight.DesktopView;
import org.nakedobjects.viewer.lightweight.DragSource;
import org.nakedobjects.viewer.lightweight.DragTarget;
import org.nakedobjects.viewer.lightweight.DragView;
import org.nakedobjects.viewer.lightweight.InternalView;
import org.nakedobjects.viewer.lightweight.MenuOptionSet;
import org.nakedobjects.viewer.lightweight.ObjectIconView;
import org.nakedobjects.viewer.lightweight.Size;
import org.nakedobjects.viewer.lightweight.Style;
import org.nakedobjects.viewer.lightweight.Style.Text;
import org.nakedobjects.viewer.lightweight.options.RemoveAssociationOption;


public class ObjectIcon extends ObjectIconView implements DesktopView, DragSource, DragView,
    DragTarget, InternalView {
    private static Style.Text style = Style.NORMAL;

	public ObjectIcon() {
		setBorder(new IconBorder());
	}
	
   public String getName() {
        return "Icon";
    }

    public Text getTitleTextStyle() {
    	return style;
    }

    /**
	 * Returns true to show that this view is an icon. 
	 */	
	public boolean isOpen() {
		return false;
	}

    public Size getRequiredSize() {
    	Size size = super.getRequiredSize();
    	size.extend(titleSize());
        return size;
    }

 	public void objectMenuOptions(MenuOptionSet options) {
		super.objectMenuOptions(options);
		if(getFieldOf() != null) {
			options.add(MenuOptionSet.OBJECT, new RemoveAssociationOption());
		}
	 }  	

	 protected boolean transparentBackground() {
		 return true;
	 }
}
