package org.nakedobjects.viewer.skylark.metal;

import org.nakedobjects.configuration.Configuration;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedClass;
import org.nakedobjects.object.NakedCollection;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.viewer.skylark.Canvas;
import org.nakedobjects.viewer.skylark.Click;
import org.nakedobjects.viewer.skylark.Color;
import org.nakedobjects.viewer.skylark.Content;
import org.nakedobjects.viewer.skylark.ObjectContent;
import org.nakedobjects.viewer.skylark.Size;
import org.nakedobjects.viewer.skylark.Style;
import org.nakedobjects.viewer.skylark.View;
import org.nakedobjects.viewer.skylark.ViewAxis;
import org.nakedobjects.viewer.skylark.ViewSpecification;
import org.nakedobjects.viewer.skylark.basic.ClassTitleText;
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
    private final ClassTitleText title;

    public ClassIcon(Content content, ViewSpecification specification, ViewAxis axis) {
        super(content, specification, axis);
        
        int iconSize = Configuration.getInstance().getInteger("viewer.skylark.class-icon-size", 85);

        iconUnselected = new IconGraphic(this, iconSize) {
        	protected String iconName(NakedObject object) {
				return super.iconName(object) + "_class";
			}
        };
        if(!iconUnselected.isImageAvailable()) {
            iconUnselected = new IconGraphic(this, iconSize);
        }
        
        iconSelected = new IconGraphic(this, iconSize) {
        	protected String iconName(NakedObject object) {
				return super.iconName(object) + "_class_selected";
			}
        };        
        if(!iconSelected.isImageAvailable()) {
            iconSelected = iconUnselected;
        }
        
       icon = iconUnselected;
       
       title = new ClassTitleText(this, Style.CLASS);
    }

    public void exited() {
        icon = iconUnselected;
        markDamaged();
        super.exited();
    }
    
    public void entered() {
        icon = iconSelected;
        markDamaged();
        super.entered();
    }

    public void draw(Canvas canvas) {
        super.draw(canvas);

        if(DEBUG) {
            canvas.drawRectangle(getSize(), Color.DEBUG1);
        }
        
        int x = 0;
        int y = icon.getBaseline();
        icon.draw(canvas, x, y);
 
		int w = title.getSize().getWidth();
		int x2 = (w > icon.getSize().getWidth()) ? x : getSize().getWidth() / 2 - w / 2;
		int y2 = icon.getSize().getHeight() + Style.CLASS.getAscent() + VPADDING;
		title.draw(canvas, x2, y2);
    }

    public int getBaseline() {
        return icon.getBaseline();
    }

    public Size getRequiredSize() {
        final Size iconSize = icon.getSize();
        final Size textSize = title.getSize();
        NakedObject object = ((ObjectContent) getContent()).getObject();
        
        iconSize.extendHeight(VPADDING + textSize.getHeight()  + VPADDING);
        iconSize.ensureWidth(textSize.getWidth());
        return iconSize;
    }

    public boolean isOpen() {
        return false;
    }
    
    public void secondClick(Click click) {
		NakedObject object = ((ObjectContent) getContent()).getObject();
		NakedClass nc = ((NakedClass) object);
		/*Action action = nc.getNakedClass().getObjectAction(Action.USER, "Instances");
		InstanceCollection instances = (InstanceCollection) action.execute(object);
		*/
		NakedCollection instances = nc.actionInstances();
		View view = ViewFactory.getViewFactory().createOpenRootView(instances);
		view.setLocation(click.getLocationWithinViewer());
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
