package org.nakedobjects.viewer.skylark.special;

import org.nakedobjects.viewer.skylark.CompositeViewBuilder;
import org.nakedobjects.viewer.skylark.Location;
import org.nakedobjects.viewer.skylark.Size;
import org.nakedobjects.viewer.skylark.View;
import org.nakedobjects.viewer.skylark.core.AbstractBuilderDecorator;
import org.nakedobjects.viewer.skylark.metal.TextFieldSpecification;

public class StackLayout extends AbstractBuilderDecorator {
	private boolean fixedWidth;

    public StackLayout(CompositeViewBuilder design) {
		super(design);
		this.fixedWidth = false;
	}
	
	public StackLayout(CompositeViewBuilder design, boolean fixedWidth) {
		super(design);
		this.fixedWidth = fixedWidth;
	}
	
	public Size getRequiredSize(View view) {
		int height = 0;
		int width = 0;
        View views[] = view.getSubviews();

        for (int i = 0; i < views.length; i++) {
            View v = views[i];
			Size s = v.getRequiredSize();
			width = Math.max(width, s.getWidth());
			height += s.getHeight();
		}

		return new Size(width, height);
	}    
    
    public boolean isOpen() {
		return true;
	}

    public void layout(View view) {
		int x = 0, y = 0;
        View subviews[] = view.getSubviews();

        int maxWidth = 0;
            for (int i = 0; i < subviews.length; i++) {
                View v = subviews[i];
    			Size s = v.getRequiredSize();
    			maxWidth = Math.max(maxWidth, s.getWidth());
    		}
        
        for (int i = 0; i < subviews.length; i++) {
            View v = subviews[i];
			Size s = v.getRequiredSize();
		    if(fixedWidth || v.getSpecification() instanceof TextFieldSpecification) {
		        s.ensureWidth(maxWidth);
		    }
			v.setSize(s);
			v.setLocation(new Location(x, y));
			y += s.getHeight();
		}
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