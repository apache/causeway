package org.nakedobjects.viewer.skylark.basic;

import org.nakedobjects.viewer.skylark.FieldContent;
import org.nakedobjects.viewer.skylark.Style;
import org.nakedobjects.viewer.skylark.Text;
import org.nakedobjects.viewer.skylark.View;

public class FieldLabel extends LabelBorder {

    public static FieldLabel createInstance(View wrappedView) {
        FieldContent fieldContent = (FieldContent) wrappedView.getContent();
        String name = fieldContent.getFieldName();
        boolean mandatory = fieldContent.isMandatory();
        Text style = mandatory ? Style.TITLE : Style.LABEL;
        return new FieldLabel(name, style, wrappedView);
    }
    
    public FieldLabel(String name, Text style, View wrappedView) {
        super(name, style, wrappedView);
    }
    
	public FieldLabel(View wrappedView) {
		super(((FieldContent) wrappedView.getContent()).getFieldName(), wrappedView);
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