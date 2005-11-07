package org.nakedobjects.viewer.skylark.value;

import org.nakedobjects.object.InvalidEntryException;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.NakedValue;
import org.nakedobjects.object.Oid;
import org.nakedobjects.object.control.Consent;
import org.nakedobjects.utility.DebugString;
import org.nakedobjects.utility.ToString;
import org.nakedobjects.viewer.skylark.Content;
import org.nakedobjects.viewer.skylark.Location;
import org.nakedobjects.viewer.skylark.ValueContent;
import org.nakedobjects.viewer.skylark.View;
import org.nakedobjects.viewer.skylark.ViewAxis;
import org.nakedobjects.viewer.skylark.ViewSpecification;
import org.nakedobjects.viewer.skylark.Workspace;
import org.nakedobjects.viewer.skylark.example.ExampleViewSpecification;
import org.nakedobjects.viewer.skylark.example.TestViews;


public class TextFieldExample extends TestViews {
    private static final String LONG_TEXT = "Naked Objects - a framework that exposes behaviourally complete business\n"
            + "objects directly to the user. Copyright (C) 2000 - 2005 Naked Objects Group  Ltd\n" + "\n"
            + "This program is free software; you can redistribute it and/or modify it under\n"
            + "the terms of the GNU General Public License as published by the Free Software\n"
            + "Foundation; either version 2 of the License, or (at your option) any later " + "version.";

    private static final String SHORT_TEXT = "Short length of text for small field";

    public static void main(String[] args) {
        new TextFieldExample();
    }

    protected void views(Workspace workspace) {
        View parent = new ParentView();

        Content content = new DummyValue(SHORT_TEXT);
        ViewSpecification specification = new ExampleViewSpecification();
        ViewAxis axis = null;

        SingleLineTextField textField = new SingleLineTextField(content, specification, axis, true, 20);
        textField.setParent(parent);
        textField.setMaxWidth(200);
        textField.setLocation(new Location(50, 20));
        textField.setSize(textField.getRequiredSize());
        workspace.addView(textField);

        textField = new SingleLineTextField(content, specification, axis, false, 20);
        textField.setParent(parent);
        textField.setMaxWidth(80);
        textField.setLocation(new Location(50, 80));
        textField.setSize(textField.getRequiredSize());
        workspace.addView(textField);

        content = new DummyValue(LONG_TEXT);
        WrappedTextField view = new WrappedTextField(content, specification, axis, false, 20);
        view.setParent(parent);
        view.setNoLines(5);
        view.setMaxWidth(200);
        view.setWrapping(false);
        view.setLocation(new Location(50, 140));
        view.setSize(view.getRequiredSize());
        workspace.addView(view);

        view = new WrappedTextField(content, specification, axis, true, 20);
        view.setParent(parent);
        view.setNoLines(8);
        view.setMaxWidth(500);
        view.setWrapping(false);
        view.setLocation(new Location(50, 250));
        view.setSize(view.getRequiredSize());
        workspace.addView(view);
    }
}

class DummyValue extends ValueContent {
    private String text;
    private NakedValue object = new NakedValue() {

        public String toString() {
            ToString str = new ToString(this);
            str.append("text", text);
            return str.toString();
        }
        
        public byte[] asEncodedString() {
            return null;
        }

        public String getIconName() {
            return null;
        }

        public int getMaximumLength() {
            return 0;
        }

        public int getMinumumLength() {
            return 0;
        }

        public Object getObject() {
            return null;
        }

        public Oid getOid() {
            return null;
        }

        public NakedObjectSpecification getSpecification() {
            return null;
        }

        public void parseTextEntry(String text) throws InvalidEntryException {
            DummyValue.this.text = text;
        }

        public void restoreFromEncodedString(byte[] data) {}

        public String titleString() {
            return text;
        }

        public void clear() {}

        public boolean canClear() {
            return false;
        }

        public boolean isEmpty() {
            return false;
        }
    };

    public DummyValue(String text) {
        this.text = text;
    }

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
        return getObject();
    }

    public NakedValue getObject() {
        return object;
    }

    public NakedObjectSpecification getSpecification() {
        return null;
    }

    public boolean isTransient() {
        return false;
    }

    public String title() {
        return null;
    }

    public boolean isEmpty() {
        return false;
    }

    public void clear() {}
    

    public void entryComplete() {}

    public String getDescription() {
        return null;
    }

    public String getName() {
        return null;
    }

    public void parseTextEntry(String entryText) throws InvalidEntryException {}

    public Consent isEditable() {
        return null;
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