package org.nakedobjects.viewer.skylark.value;

import org.nakedobjects.object.InvalidEntryException;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.value.Option;
import org.nakedobjects.viewer.skylark.Content;
import org.nakedobjects.viewer.skylark.ValueField;
import org.nakedobjects.viewer.skylark.View;
import org.nakedobjects.viewer.skylark.ViewAxis;
import org.nakedobjects.viewer.skylark.ViewSpecification;
import org.nakedobjects.viewer.skylark.basic.SimpleIdentifier;
import org.nakedobjects.viewer.skylark.core.AbstractFieldSpecification;

public class OptionSelectionField extends TextField {

    private String selected;

    public static class Specification extends AbstractFieldSpecification {
        public boolean canDisplay(Naked object) {
            return object instanceof Option;
        }
        
        public View createView(Content content, ViewAxis axis) {
            return new SimpleIdentifier(new OptionSelectionFieldBorder(new OptionSelectionField(content, this, axis)));
        }

        public String getName() {
            return "Drop down list";
        }
    }

    public OptionSelectionField(Content content, ViewSpecification specification, ViewAxis axis) {
        super(content, specification, axis, false);
    }
    
    Option getOption() {
        ValueField content = ((ValueField) getContent());
        Option value = (Option) content.getValue();

        return value;
    }
    
    void set(String selected) {
        this.selected = selected;
        initiateSave();
    }
    
    protected void save() {
        try {
            parseEntry(selected);
        } catch (InvalidEntryException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business
 * objects directly to the user. Copyright (C) 2000 - 2004 Naked Objects Group
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
