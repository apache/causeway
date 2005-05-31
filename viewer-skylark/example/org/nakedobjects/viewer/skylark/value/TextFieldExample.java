package org.nakedobjects.viewer.skylark.value;

import org.nakedobjects.object.InvalidEntryException;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.NakedValue;
import org.nakedobjects.object.TextEntryParseException;
import org.nakedobjects.object.control.Consent;
import org.nakedobjects.object.control.DefaultHint;
import org.nakedobjects.object.control.Hint;
import org.nakedobjects.utility.DebugString;
import org.nakedobjects.viewer.skylark.Content;
import org.nakedobjects.viewer.skylark.Location;
import org.nakedobjects.viewer.skylark.Size;
import org.nakedobjects.viewer.skylark.ValueContent;
import org.nakedobjects.viewer.skylark.ViewAxis;
import org.nakedobjects.viewer.skylark.ViewSpecification;
import org.nakedobjects.viewer.skylark.Workspace;
import org.nakedobjects.viewer.skylark.example.ExampleViewSpecification;
import org.nakedobjects.viewer.skylark.example.TestViews;


public class TextFieldExample extends TestViews {

    public static void main(String[] args) {
        new TextFieldExample();
    }

    protected void views(Workspace workspace) {
        
        Content content = new Value();
        ViewSpecification specification = new ExampleViewSpecification();        
        ViewAxis axis = null;
        
        TextField view = new TextField(content, specification, axis, false);
        view.setNoLines(3);
        view.setMaxWidth(200);
        view.setLocation(new Location(50, 60));
        view.setSize(view.getRequiredSize());
        workspace.addView(view);

        view = new TextField(content, specification, axis, false);
        view.setNoLines(1);
        view.setMaxWidth(80);
        view.setLocation(new Location(50, 150));
        view.setSize(view.getRequiredSize());
        workspace.addView(view);
        
        view = new TextField(content, specification, axis, false);
        view.setNoLines(3);
        view.setMaxWidth(200);
        view.setWrapping(false);
        view.setLocation(new Location(50, 60));
        view.setSize(view.getRequiredSize());
        workspace.addView(view);

        

    }
}



class Value extends ValueContent {

    public NakedValue getObject() {
        return null;
    }

    public Hint getValueHint(String entryText) {
        return new DefaultHint();
    }

    public void parseEntry(String entryText) throws TextEntryParseException, InvalidEntryException {}

    public Consent canDrop(Content sourceContent) {
        return null;
    }

    public void debugDetails(DebugString debug) {}

    public Naked drop(Content sourceContent) {
        return null;
    }

    public String getIconName() {
        return null;
    }

    public Naked getNaked() {
        return null;
    }

    public NakedObjectSpecification getSpecification() {
        return null;
    }

    public boolean isTransient() {
        return false;
    }

    public String title() {
        return "a really long string that should wrap when displayed.";
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