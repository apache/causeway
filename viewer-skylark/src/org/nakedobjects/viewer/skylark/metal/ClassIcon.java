package org.nakedobjects.viewer.skylark.metal;

import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedClass;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.collection.InstanceCollection;
import org.nakedobjects.object.control.Permission;
import org.nakedobjects.object.reflect.Action;
import org.nakedobjects.viewer.skylark.Canvas;
import org.nakedobjects.viewer.skylark.Click;
import org.nakedobjects.viewer.skylark.Content;
import org.nakedobjects.viewer.skylark.Location;
import org.nakedobjects.viewer.skylark.MenuOptionSet;
import org.nakedobjects.viewer.skylark.ObjectContent;
import org.nakedobjects.viewer.skylark.Size;
import org.nakedobjects.viewer.skylark.UserAction;
import org.nakedobjects.viewer.skylark.View;
import org.nakedobjects.viewer.skylark.ViewAxis;
import org.nakedobjects.viewer.skylark.ViewSpecification;
import org.nakedobjects.viewer.skylark.Workspace;
import org.nakedobjects.viewer.skylark.basic.ClassOption;
import org.nakedobjects.viewer.skylark.basic.IconGraphic;
import org.nakedobjects.viewer.skylark.core.ObjectView;
import org.nakedobjects.viewer.skylark.util.ViewFactory;


public class ClassIcon extends ObjectView {
    
    public static class Specification implements ViewSpecification {

		public boolean canDisplay(Naked object) {
			return object instanceof NakedClass;
		}

        public View createView(Content content, ViewAxis axis) {
    		return new ClassIcon(content, this, axis);
        }

		public String getName() {
			return "class icon";
		}

		public boolean isOpen() {
			return false;
		}

		public boolean isReplaceable() {
			return false;
		}

		public boolean isSubView() {
			return false;
		}
    }
    private IconGraphic iconUnselected;
    private IconGraphic iconSelected;
    private IconGraphic icon;

    public ClassIcon(Content content, ViewSpecification specification, ViewAxis axis) {
        super(content, specification, axis);

        iconSelected = new IconGraphic(this, 85) {
        	protected String iconName(NakedObject object) {
				return super.iconName(object) + "_class_selected";
			}
        };
        
        iconUnselected = new IconGraphic(this, 85) {
        	protected String iconName(NakedObject object) {
				return super.iconName(object) + "_class";
			}
        };

        icon = iconUnselected;
    }

    public void exited() {
        icon = iconUnselected;
        markDamaged();
        super.exited();
    }
    
    public void menuOptions(MenuOptionSet options) {
    	// see ClassOption and use?
    //	ClassOption.menuOptions((NakedClass) ((ObjectContent) getContent()).getObject(), options);
    	
//    	NakedClass cls = (NakedClass) ((ObjectContent) getContent()).getObject();
//		Action[] actions = cls.getClassActions(Action.USER, 0);
    	
        icon = iconSelected;
        markDamaged();
        super.menuOptions(options);
    }

    public void draw(Canvas canvas) {
        super.draw(canvas);

        int x = 0;
        int y = icon.getBaseline();
        icon.draw(canvas, x, y);
        x += iconUnselected.getSize().getWidth();
    }

    public int getBaseline() {
        return iconUnselected.getBaseline();
    }

    public Size getRequiredSize() {
        Size size = iconUnselected.getSize();

        return size;
    }

    public boolean isOpen() {
        return false;
    }
    
    public void secondClick(Click click) {
		NakedObject object = ((ObjectContent) getContent()).getObject();
		NakedClass nc = ((NakedClass) object);
		Action action = nc.getNakedClass().getObjectAction(Action.USER, "Instances");
		InstanceCollection instances = (InstanceCollection) action.execute(object);
		View view = ViewFactory.getViewFactory().createOpenRootView(instances);
		view.setLocation(click.getLocation());
		getWorkspace().addView(view);
	}

    public String toString() {
        return "MetalClassIcon" + getId();
    }
}


/*
Naked Objects - a framework that exposes behaviourally complete
business objects directly to the user.
Copyright (C) 2000 - 2004  Naked Objects Group Ltd

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
